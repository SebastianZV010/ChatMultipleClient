package org.compunet1.common;

import java.io.*;
import java.util.*;

public class History {
    private List<Message> messageHistory = new ArrayList<>();

    public void addMessage(Message message) {
        messageHistory.add(message);
        saveToFile(message);
    }

    // Métodos adicionales para registrar llamadas y notas de voz
    public void addCallRecord(String fromUser, String toUser) {
        String record = fromUser + " called " + toUser;
        messageHistory.add(new Message(fromUser, toUser, "CALL_LOG"));
        saveToFile(new Message(fromUser, toUser, "CALL_LOG"));
    }

    public void addVoiceNoteRecord(String fromUser, String toUser) {
        String record = fromUser + " sent a voice note to " + toUser;
        messageHistory.add(new Message(fromUser, toUser, "VOICE_NOTE_LOG"));
        saveToFile(new Message(fromUser, toUser, "VOICE_NOTE_LOG"));
    }

    private void saveToFile(Message message) {
        try (FileWriter fw = new FileWriter("chat_history.txt", true)) {
            fw.write(message.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
