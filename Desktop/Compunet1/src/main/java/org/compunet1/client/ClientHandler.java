package org.compunet1.client;

import org.compunet1.common.Message;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private ObjectInputStream in;
    private Socket socket;

    public ClientHandler(ObjectInputStream in, Socket socket) {
        this.in = in;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            while (true) {
                try {
                    // Lee los objetos de mensaje desde el flujo de entrada
                    Object obj = in.readObject();

                    if (obj instanceof Message) {
                        Message message = (Message) obj;

                        if (message.getText().equals("SENDING_VOICE_NOTE")) {
                            System.out.println("Receiving voice note...");
                            receiveFile();  // Llama al método para recibir el archivo
                        }
                        // Verifica si es una confirmación de recepción del servidor
                        else if (message.getText().equals("Voice note received")) {
                            System.out.println("Server confirmed receipt of the voice note.");
                        }
                        // Otros tipos de mensajes
                        else {
                            System.out.println("[" + message.getFromUser() + "]: " + message.getText());
                        }
                    } else {
                        System.out.println("Unexpected object type received: " + obj.getClass());
                    }
                } catch (StreamCorruptedException e) {
                    System.out.println("StreamCorruptedException: " + e.getMessage());
                    break;  // Rompe el ciclo si el flujo está corrompido
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Método para recibir un archivo
    private void receiveFile() {
        try {
            // Leer primero el tamaño del archivo
            long fileSize = in.readLong();
            System.out.println("Expecting file of size: " + fileSize + " bytes");

            File receivedFile = new File("received_voice_note.wav");
            try (FileOutputStream fos = new FileOutputStream(receivedFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize && (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                    bos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    System.out.println("Received " + totalBytesRead + " of " + fileSize + " bytes");
                }

                System.out.println("Voice note received successfully. Total size: " + totalBytesRead + " bytes.");
            }

            // Esperar mensaje de finalización de archivo
            Message endMessage = (Message) in.readObject();
            if (endMessage.getText().equals("FILE_TRANSFER_COMPLETE")) {
                System.out.println("File transfer complete.");
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error receiving voice note.");
            e.printStackTrace();
        }
    }
}