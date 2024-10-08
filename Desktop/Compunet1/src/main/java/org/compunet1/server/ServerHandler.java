package org.compunet1.server;

import org.compunet1.common.Message;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Arrays;

public class ServerHandler implements Runnable {
    private Socket socket;
    private GroupManager groupManager;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;

    public ServerHandler(Socket socket, ChatServer server, GroupManager groupManager) throws IOException {
        this.socket = socket;
        this.groupManager = groupManager;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message message = (Message) in.readObject();

                // Registrar usuario en el servidor
                if (message.getToUser().equals("Server") && message.getText().equals("REGISTER")) {
                    this.username = message.getFromUser();
                    groupManager.registerClient(username, this);
                    System.out.println(username + " has joined the chat.");
                }

                // Procesar envío de archivo de voz
                else if (message.getText().equals("SENDING_VOICE_NOTE")) {
                    receiveFile(message.getFromUser(), message.getToUser());
                }

                // Procesar comandos (incluye creación de grupos y llamadas)
                else if (message.getText().startsWith("/")) {
                    processCommand(message);
                }

                // Procesar mensajes regulares entre usuarios
                else {
                    processRegularMessage(message);
                }
            }
        } catch (EOFException e) {
            System.out.println("Connection closed by client.");
        } catch (SocketException e) {
            System.out.println("Socket closed: " + e.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeConnections();
        }
    }

    private void receiveFile(String fromUser, String toUser) throws IOException {
        long fileSize = in.readLong(); // Leer el tamaño del archivo primero
        System.out.println("Expecting file of size: " + fileSize + " bytes from " + fromUser);

        byte[] buffer = new byte[4096];
        File receivedFile = new File("received_voice_note_" + fromUser + ".wav");

        try (FileOutputStream fos = new FileOutputStream(receivedFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            long totalBytesRead = 0;
            int bytesRead;
            while (totalBytesRead < fileSize && (bytesRead = in.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                bos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                System.out.println("Received " + totalBytesRead + " of " + fileSize + " bytes");
            }

            System.out.println("Voice note successfully received from " + fromUser + ". Total size: " + totalBytesRead + " bytes.");

            // Confirmar la recepción
            sendMessage(new Message("Server", fromUser, "FILE_TRANSFER_COMPLETE"));

            // Reenviar la nota de voz
            if (groupManager.isGroup(toUser)) {
                groupManager.sendVoiceNoteToGroup(toUser, receivedFile);
            } else {
                groupManager.sendVoiceNoteToUser(toUser, receivedFile);
            }

            // Enviar confirmación de recepción
            sendMessage(new Message("Server", fromUser, "Voice note received"));

        } catch (IOException e) {
            System.out.println("Error receiving voice note from " + fromUser);
            e.printStackTrace();
            throw e; // Re-lanzar la excepción para manejarla en el método run()
        }
    }

    // Método para enviar un archivo de voz al cliente
    public void sendFile(File file) throws IOException {
        if (socket.isClosed()) {
            System.out.println("Socket is closed, cannot send file.");
            return;
        }
        byte[] buffer = new byte[4096];
        try (FileInputStream fis = new FileInputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream())) {

            int bytesRead;
            while ((bytesRead = fis.read(buffer)) > 0) {
                bos.write(buffer, 0, bytesRead);
            }
            bos.flush();
            System.out.println("Voice note sent to client.");

            // Enviar confirmación de finalización
            out.writeObject(new Message("Server", username, "FILE_TRANSFER_COMPLETE"));
            out.flush();

        } catch (SocketException e) {
            System.out.println("Error while sending voice note: " + e.getMessage());
        }
    }


    // Método para procesar comandos
    private void processCommand(Message message) {
        String[] parts = message.getText().split(" ", 3);
        if (parts.length < 2) {
            System.out.println("Invalid command format.");
            return;
        }

        String command = parts[0];
        String target = parts[1];

        switch (command) {
            case "/group":
                if (parts.length == 3) {
                    processGroupCommand(target, parts[2].split(","));
                } else {
                    System.out.println("Invalid group command format. Use: /group <groupname> <member1,member2,...>");
                }
                break;
            case "/call":
                processCallCommand(target);
                break;
            case "/accept":
                try {
                    sendMessage(new Message(username, target, "CALL_ACCEPTED"));
                    System.out.println(username + " accepted the call from " + target);
                } catch (IOException e) {
                    System.out.println("Error sending CALL_ACCEPTED message: " + e.getMessage());
                    e.printStackTrace();
                }
                break;
            case "/deny":
                try {
                    sendMessage(new Message(username, target, "CALL_REJECTED"));
                    System.out.println(username + " denied the call from " + target);
                } catch (IOException e) {
                    System.out.println("Error sending CALL_REJECTED message: " + e.getMessage());
                    e.printStackTrace();
                }
                break;
            case "/cancel":
                try {
                    sendMessage(new Message(username, target, "CALL_ENDED"));
                    System.out.println(username + " canceled the call with " + target);
                } catch (IOException e) {
                    System.out.println("Error sending CALL_ENDED message: " + e.getMessage());
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Unknown command: " + command);
                break;
        }
    }

    // Método para procesar el comando de creación de grupos
    private void processGroupCommand(String groupName, String[] membersArray) {
        List<String> members = Arrays.asList(membersArray);
        boolean allUsersExist = true;
        for (String member : members) {
            if (!groupManager.isUserRegistered(member)) {
                allUsersExist = false;
                System.out.println("User not found: " + member);
                break;
            }
        }

        if (allUsersExist) {
            groupManager.createGroup(groupName, members);
            System.out.println(username + " created group " + groupName + " with members: " + members);
        }
    }

    // Método para procesar llamadas
    private void processCallCommand(String target) {
        try {
            if (groupManager.isUserRegistered(target)) {
                groupManager.callUserOrGroup(target);
                System.out.println(username + " is calling " + target);
            } else {
                System.out.println("User not found: " + target);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para procesar mensajes regulares
    private void processRegularMessage(Message message) {
        try {
            if (groupManager.isGroup(message.getToUser())) {
                groupManager.sendMessageToGroup(message.getToUser(), message);
            } else if (groupManager.isUserRegistered(message.getToUser())) {
                groupManager.sendMessageToUser(message.getToUser(), message);
            } else {
                System.out.println("User or group not found: " + message.getToUser());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Cerrar conexiones
    private void closeConnections() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendMessage(Message message) throws IOException {
        out.writeObject(message);
    }
}
