package org.compunet1.common;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 2L;

    private String fromUser;
    private String toUser;
    private String text;
    private boolean isVoiceNote;
    private long voiceNoteSize;

    public Message(String fromUser, String toUser, String text) {
        this(fromUser, toUser, text, false, 0);
    }

    public Message(String fromUser, String toUser, String text, boolean isVoiceNote, long voiceNoteSize) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.text = text;
        this.isVoiceNote = isVoiceNote;
        this.voiceNoteSize = voiceNoteSize;
    }

    // Getters
    public String getFromUser() { return fromUser; }
    public String getToUser() { return toUser; }
    public String getText() { return text; }
    public boolean isVoiceNote() { return isVoiceNote; }
    public long getVoiceNoteSize() { return voiceNoteSize; }

    @Override
    public String toString() {
        if (isVoiceNote) {
            return fromUser + " sent a voice note to " + toUser + " (Size: " + voiceNoteSize + " bytes)";
        }
        return fromUser + " to " + toUser + ": " + text;
    }
}