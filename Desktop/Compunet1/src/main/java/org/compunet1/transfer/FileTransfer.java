package org.compunet1.transfer;

import java.io.*;

public class FileTransfer {
    private DataOutputStream dataOut;
    private DataInputStream dataIn;

    public FileTransfer(DataOutputStream dataOut, DataInputStream dataIn) {
        this.dataOut = dataOut;
        this.dataIn = dataIn;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOut;
    }

    public DataInputStream getDataInputStream() {
        return dataIn;
    }

    // Método para enviar el archivo
    public void sendFile(File file, long fileSize) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesSent = 0;

            // Escribir el tamaño del archivo primero
            dataOut.writeLong(fileSize);

            // Enviar archivo en bloques
            while ((bytesRead = bis.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
                totalBytesSent += bytesRead;
                System.out.println("Sent " + totalBytesSent + " of " + fileSize + " bytes");
            }
            dataOut.flush();
        }
    }

    // Método para recibir un archivo
    public void receiveFile(File destination, long fileSize) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(destination);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesReceived = 0;

            while (totalBytesReceived < fileSize && (bytesRead = dataIn.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
                totalBytesReceived += bytesRead;
                System.out.println("Received " + totalBytesReceived + " of " + fileSize + " bytes");
            }
        }
    }
}
