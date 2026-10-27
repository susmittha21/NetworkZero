package com.networkzero.tools;

import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AudioGeneratorTest {
    @Test
    void generateAudioAssets() throws Exception {
        AudioGenerator.main(new String[0]);
        File dir = new File("assets/audio");
        if (!dir.exists()) dir = new File("../assets/audio");
        assertTrue(new File(dir, "ending_music.wav").exists());
        assertTrue(new File(dir, "door_open.wav").exists());
        assertTrue(new File(dir, "power_up.wav").exists());
    }
}
