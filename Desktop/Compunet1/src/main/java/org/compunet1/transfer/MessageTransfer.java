package org.compunet1.transfer;

import org.compunet1.common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MessageTransfer {
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;

    public MessageTransfer(ObjectOutputStream objectOut, ObjectInputStream objectIn) {
        this.objectOut = objectOut;
        this.objectIn = objectIn;
    }

    // Métodos para obtener los streams
    public ObjectOutputStream getObjectOutputStream() {
        return objectOut;
    }

    public ObjectInputStream getObjectInputStream() {
        return objectIn;
    }

    // Método para enviar un mensaje
    public void sendMessage(Message message) throws IOException {
        objectOut.writeObject(message);
        objectOut.flush(); // Forzar el envío del mensaje inmediatamente
    }

    // Método para recibir un mensaje
    public Message receiveMessage() throws IOException, ClassNotFoundException {
        return (Message) objectIn.readObject();
    }
}
