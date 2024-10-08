package org.compunet1.client;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioRecorder {
    private File wavFile;
    private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    private TargetDataLine line;
    private Thread recordingThread;

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public void startRecording() {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            wavFile = new File("recorded_audio_" + timestamp + ".wav"); // Archivo único

            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                return;
            }

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            System.out.println("Recording...");

            AudioInputStream ais = new AudioInputStream(line);

            // Grabación en un hilo separado con manejo adecuado
            recordingThread = new Thread(() -> {
                try {
                    AudioSystem.write(ais, fileType, wavFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            recordingThread.start();

        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }

    public void finishRecording() {
        if (line != null && line.isRunning()) {
            line.stop();
            line.close();
            System.out.println("Finished recording.");
        }
        if (recordingThread != null && recordingThread.isAlive()) {
            try {
                recordingThread.join(); // Espera a que el hilo termine
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public File getRecordedFile() {
        return wavFile;
    }
}
