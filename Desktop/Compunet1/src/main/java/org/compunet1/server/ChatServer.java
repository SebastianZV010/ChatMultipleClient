package org.compunet1.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import javax.sound.sampled.*;

public class ChatServer {
    private ServerSocket serverSocket;
    private GroupManager groupManager = new GroupManager();
    private DatagramSocket audioSocket;
    private volatile boolean isRunning;

    public ChatServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        audioSocket = new DatagramSocket(50005);  // Socket UDP para llamadas de voz
        isRunning = true;
    }

    public void start() {
        System.out.println("Server started...");
        new Thread(this::handleAudioStream).start();  // Hilo para manejar llamadas de voz

        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected.");
                ServerHandler handler = new ServerHandler(clientSocket, this, groupManager);
                new Thread(handler).start();
            } catch (IOException e) {
                if (isRunning) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Método para manejar las llamadas de audio en tiempo real
    private void handleAudioStream() {
        try {
            byte[] buffer = new byte[4096];
            AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(info);
            speakers.open(format);
            speakers.start();

            System.out.println("Server is listening for audio...");

            while (isRunning) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                audioSocket.receive(packet);
                speakers.write(packet.getData(), 0, packet.getLength());
            }
        } catch (IOException | LineUnavailableException e) {
            if (isRunning) {
                e.printStackTrace();
            }
        } finally {
            closeAudioSocket();
        }
    }

    private void closeAudioSocket() {
        if (audioSocket != null && !audioSocket.isClosed()) {
            audioSocket.close();
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeAudioSocket();
    }

    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer(8080);
        server.start();
    }
}
