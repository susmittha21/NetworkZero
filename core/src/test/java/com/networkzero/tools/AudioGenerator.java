package com.networkzero.tools;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class AudioGenerator {
    private static final float SAMPLE_RATE = 44100f;

    public static void main(String[] args) throws Exception {
        File dir = new File("assets/audio");
        if (!dir.exists()) dir = new File("../assets/audio");
        dir.mkdirs();
        generateEndingMusic(new File(dir, "ending_music.wav"));
        generateDoorOpen(new File(dir, "door_open.wav"));
        generatePowerUp(new File(dir, "power_up.wav"));
        System.out.println("Audio files generated successfully in " + dir.getAbsolutePath());
    }

    public static void generateEndingMusic(File outFile) throws IOException {
        int seconds = 16;
        int totalSamples = (int)(SAMPLE_RATE * seconds);
        byte[] buffer = new byte[totalSamples * 2];

        // Calm ambient arpeggio progression: C - G - Am - F
        // Chord roots: C4 (261.63), G3 (196.00), A3 (220.00), F3 (174.61)
        double[][] chords = {
            {261.63, 329.63, 392.00, 523.25}, // C - E - G - C
            {196.00, 246.94, 293.66, 392.00}, // G - B - D - G
            {220.00, 261.63, 329.63, 440.00}, // A - C - E - A
            {174.61, 220.00, 261.63, 349.23}  // F - A - C - F
        };

        for (int i = 0; i < totalSamples; i++) {
            double t = i / (double)SAMPLE_RATE;
            int chordIdx = ((int)(t / 4.0)) % 4;
            double chordTime = t % 4.0;
            double[] notes = chords[chordIdx];

            double sample = 0;
            // Warm bass drone
            sample += 0.25 * Math.sin(2 * Math.PI * (notes[0] / 2.0) * t);

            // Arpeggio note: 4 notes per chord (1 second each)
            int noteIdx = (int)(chordTime);
            double noteFreq = notes[noteIdx % notes.length];
            double noteTime = chordTime - noteIdx;
            // Envelope: soft attack, decaying sustain
            double env = Math.exp(-1.8 * noteTime) * (1.0 - Math.exp(-20.0 * noteTime));
            // Warm tone: fundamental + subtle octave harmonic
            double noteWave = 0.3 * Math.sin(2 * Math.PI * noteFreq * t)
                            + 0.1 * Math.sin(2 * Math.PI * (noteFreq * 2) * t);
            sample += env * noteWave;

            // Soft shimmer pad in background
            double pad = 0.08 * (Math.sin(2 * Math.PI * notes[1] * t) + Math.sin(2 * Math.PI * notes[2] * t));
            sample += pad;

            // Overall fade-in at start (0.5s) and fade-out at end (0.5s) for seamless looping
            double loopEnv = 1.0;
            if (t < 0.5) loopEnv = t / 0.5;
            else if (t > seconds - 0.5) loopEnv = (seconds - t) / 0.5;
            sample *= loopEnv;

            short s = (short)(Math.max(-1.0, Math.min(1.0, sample)) * 28000);
            buffer[i * 2] = (byte)(s & 0xff);
            buffer[i * 2 + 1] = (byte)((s >> 8) & 0xff);
        }

        writeWav(outFile, buffer);
    }

    public static void generateDoorOpen(File outFile) throws IOException {
        float seconds = 1.2f;
        int totalSamples = (int)(SAMPLE_RATE * seconds);
        byte[] buffer = new byte[totalSamples * 2];

        for (int i = 0; i < totalSamples; i++) {
            double t = i / (double)SAMPLE_RATE;
            double sample = 0;

            // Mechanical metallic latch click (0 to 0.15s)
            if (t < 0.15) {
                double clickEnv = Math.exp(-40.0 * t);
                double click = Math.sin(2 * Math.PI * 480 * t) + 0.5 * Math.sin(2 * Math.PI * 820 * t);
                sample += 0.5 * clickEnv * click;
            }

            // Low thud/resonance (0.05 to 0.4s)
            if (t >= 0.05 && t < 0.4) {
                double thudT = t - 0.05;
                double thudEnv = Math.exp(-15.0 * thudT);
                sample += 0.6 * thudEnv * Math.sin(2 * Math.PI * 85 * thudT);
            }

            // Door air release / sliding seal sound (0.1 to 1.1s)
            if (t >= 0.1) {
                double slideT = t - 0.1;
                double slideEnv = Math.sin(Math.PI * (slideT / 1.0)) * 0.2;
                // Filtered white noise approximation
                double noise = (Math.random() * 2.0 - 1.0) * slideEnv;
                sample += noise;
            }

            short s = (short)(Math.max(-1.0, Math.min(1.0, sample)) * 28000);
            buffer[i * 2] = (byte)(s & 0xff);
            buffer[i * 2 + 1] = (byte)((s >> 8) & 0xff);
        }

        writeWav(outFile, buffer);
    }

    public static void generatePowerUp(File outFile) throws IOException {
        float seconds = 1.8f;
        int totalSamples = (int)(SAMPLE_RATE * seconds);
        byte[] buffer = new byte[totalSamples * 2];

        for (int i = 0; i < totalSamples; i++) {
            double t = i / (double)SAMPLE_RATE;
            // Rising electrical frequency from 80Hz to 440Hz
            double progress = t / seconds;
            double freq = 80.0 + 360.0 * Math.pow(progress, 1.5);
            double phase = 2 * Math.PI * (80.0 * t + (360.0 / 2.5) * Math.pow(t, 2.5) / Math.pow(seconds, 1.5));

            double env = Math.min(1.0, t / 0.3) * (1.0 - Math.pow(progress, 3.0));
            double sample = 0.4 * env * Math.sin(phase);
            // 2nd harmonic
            sample += 0.15 * env * Math.sin(phase * 2.0);
            // High frequency shimmer when powered on
            if (progress > 0.4) {
                double shimmerT = (progress - 0.4) / 0.6;
                sample += 0.15 * Math.sin(Math.PI * shimmerT) * Math.sin(2 * Math.PI * 880 * t);
            }

            short s = (short)(Math.max(-1.0, Math.min(1.0, sample)) * 28000);
            buffer[i * 2] = (byte)(s & 0xff);
            buffer[i * 2 + 1] = (byte)((s >> 8) & 0xff);
        }

        writeWav(outFile, buffer);
    }

    private static void writeWav(File file, byte[] pcmData) throws IOException {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(pcmData);
             AudioInputStream ais = new AudioInputStream(bais, format, pcmData.length / 2)) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        }
    }
}
