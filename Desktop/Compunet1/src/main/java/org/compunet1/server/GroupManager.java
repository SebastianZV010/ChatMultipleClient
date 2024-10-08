package org.compunet1.server;

import org.compunet1.common.Message;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class GroupManager {
    private final Map<String, ServerHandler> clients = new HashMap<>();
    private final Map<String, List<String>> groups = new HashMap<>();

    // Registrar un cliente en el servidor
    public synchronized void registerClient(String username, ServerHandler handler) {
        clients.put(username, handler);
        System.out.println("User registered: " + username);
    }

    // Verificar si un usuario está registrado
    public synchronized boolean isUserRegistered(String username) {
        return clients.containsKey(username);
    }

    // Crear un grupo de chat
    public synchronized void createGroup(String groupName, List<String> members) {
        if (!groups.containsKey(groupName)) {
            boolean allUsersExist = true;
            for (String member : members) {
                if (!isUserRegistered(member)) {
                    System.out.println("User not found: " + member);
                    allUsersExist = false;
                    break;
                }
            }

            if (allUsersExist) {
                groups.put(groupName, new ArrayList<>(members));
                System.out.println("Group " + groupName + " created with members: " + members);
            }
        } else {
            System.out.println("Group " + groupName + " already exists.");
        }
    }

    // Enviar un mensaje a un usuario específico
    public synchronized void sendMessageToUser(String toUser, Message message) throws IOException {
        ServerHandler handler = clients.get(toUser);
        if (handler != null) {
            handler.sendMessage(message);
        } else {
            System.out.println("User not found: " + toUser);
        }
    }

    // Enviar un mensaje a todos los miembros de un grupo
    public synchronized void sendMessageToGroup(String groupName, Message message) throws IOException {
        List<String> members = groups.get(groupName);
        if (members != null) {
            for (String member : members) {
                sendMessageToUser(member, message);
            }
        } else {
            System.out.println("Group not found: " + groupName);
        }
    }

    // Verificar si un grupo existe
    public synchronized boolean isGroup(String groupName) {
        return groups.containsKey(groupName);
    }

    // Realizar una llamada a un usuario o grupo
    public synchronized void callUserOrGroup(String target) throws IOException {
        if (isUserRegistered(target)) {
            System.out.println("Calling user: " + target);
            sendMessageToUser(target, new Message("Server", target, "You have a call!"));
        } else if (isGroup(target)) {
            System.out.println("Calling group: " + target);
            for (String member : groups.get(target)) {
                sendMessageToUser(member, new Message("Server", member, "You have a call from group " + target + "!"));
            }
        } else {
            System.out.println("User or group not found: " + target);
        }
    }

    // Enviar nota de voz a un usuario
    public synchronized void sendVoiceNoteToUser(String toUser, File voiceNote) throws IOException {
        ServerHandler handler = clients.get(toUser);
        if (handler != null) {
            handler.sendFile(voiceNote);
        } else {
            System.out.println("User not found: " + toUser);
        }
    }

    // Enviar nota de voz a un grupo
    public synchronized void sendVoiceNoteToGroup(String groupName, File voiceNote) throws IOException {
        List<String> members = groups.get(groupName);
        if (members != null) {
            for (String member : members) {
                sendVoiceNoteToUser(member, voiceNote);
            }
        } else {
            System.out.println("Group not found: " + groupName);
        }
    }
}
