package org.compunet1.client;


import org.compunet1.common.Message;
import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.*;

public class ChatClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private AudioRecorder recorder;
    private DatagramSocket audioSocket;
    private InetAddress serverAddress;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> callTimeoutTask;

    public ChatClient(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        recorder = new AudioRecorder();
        this.serverAddress = InetAddress.getByName(serverAddress);
        audioSocket = new DatagramSocket();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter your username:");
            username = scanner.nextLine();
            out.writeObject(new Message(username, "Server", "REGISTER"));

            new Thread(new ClientHandler(in, socket)).start();

            while (true) {
                System.out.println("Enter message, command (/group, /call), or type '/voice <user/group>' to send a voice note:");
                String input = scanner.nextLine().trim();

                if (input.startsWith("/group") || input.startsWith("/call")) {
                    out.writeObject(new Message(username, "Server", input));

                    if (input.startsWith("/call")) {
                        out.writeObject(new Message(username, input.split(" ")[1], "CALL_REQUEST"));
                        startCallTimeout(input.split(" ")[1]);
                    }

                } else if (input.startsWith("/voice")) {
                    String[] parts = input.split(" ", 2);
                    if (parts.length == 2) {
                        String toUser = parts[1];
                        handleVoiceCommand(scanner, toUser);
                    } else {
                        System.out.println("Invalid voice command format. Use: /voice <user/group>");
                    }
                } else {
                    String toUser = input.split(" ")[0];
                    String text = input.substring(toUser.length()).trim();
                    out.writeObject(new Message(username, toUser, text));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    private void startCallTimeout(String toUser) {
        callTimeoutTask = scheduler.schedule(() -> {
            try {
                out.writeObject(new Message(username, toUser, "CALL_TIMEOUT"));
                System.out.println("Call to " + toUser + " timed out.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 10, TimeUnit.SECONDS);
    }

    private void cancelCallTimeout() {
        if (callTimeoutTask != null && !callTimeoutTask.isCancelled()) {
            callTimeoutTask.cancel(true);
            System.out.println("Call timeout cancelled.");
        }
    }

    private void handleVoiceCommand(Scanner scanner, String toUser) {
        recorder.startRecording();
        System.out.println("Recording started... Press 'p' to stop.");

        while (true) {
            String command = scanner.nextLine().trim();

            if (command.equalsIgnoreCase("p")) {  // Cambiado para aceptar 'p' o 'P'
                recorder.finishRecording();
                System.out.println("Recording stopped. Press Enter to send the audio.");
                scanner.nextLine();

                try {
                    File recordedFile = recorder.getRecordedFile();
                    if (recordedFile != null && recordedFile.exists()) {
                        out.writeObject(new Message(username, toUser, "SENDING_VOICE_NOTE"));

                        // Sincroniza el hilo para asegurarse de que el archivo se envía correctamente
                        Thread sendThread = new Thread(() -> {
                            try {
                                sendFile(recordedFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        sendThread.start();
                        sendThread.join(); // Espera a que el hilo termine antes de continuar
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void sendFile(File file) throws IOException {
        long fileSize = file.length();
        System.out.println("Sending file of size: " + fileSize + " bytes.");

        // Enviar primero el tamaño del archivo
        out.writeLong(fileSize);
        out.flush();

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesSent = 0;

            while ((bytesRead = bis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesSent += bytesRead;
                System.out.println("Sent " + totalBytesSent + " of " + fileSize + " bytes");
            }
            out.flush();

            System.out.println("Voice note sent successfully.");

            // Esperar confirmación del servidor
            Message confirmation = (Message) in.readObject();
            if (confirmation.getText().equals("FILE_TRANSFER_COMPLETE")) {
                System.out.println("Server confirmed file transfer completion.");
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error sending voice note.");
            e.printStackTrace();
            throw new IOException("Error sending voice note", e);
        }
    }



    private void closeResources() {
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
            if (audioSocket != null && !audioSocket.isClosed()) {
                audioSocket.close();
            }
            if (scheduler != null) {
                scheduler.shutdownNow();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 8080);
        client.start();
    }
}
