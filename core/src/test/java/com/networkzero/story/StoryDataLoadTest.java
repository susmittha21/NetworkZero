package com.networkzero.story;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class StoryDataLoadTest {

    private File findStoryJson() {
        File f = new File("assets/data/story.json");
        if (f.exists()) return f;
        f = new File("../assets/data/story.json");
        if (f.exists()) return f;
        throw new IllegalStateException("Could not locate story.json");
    }

    private File findAssetFile(String relativePath) {
        File f = new File("assets", relativePath);
        if (f.exists()) return f;
        f = new File("../assets", relativePath);
        if (f.exists()) return f;
        return null;
    }

    @Test
    void testIntroLoadsFromStoryJsonAfterGeneralisation() throws Exception {
        File storyFile = findStoryJson();
        assertTrue(storyFile.exists(), "assets/data/story.json must exist");

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(StoryPanel.EffectConfig.class, (JsonDeserializer<StoryPanel.EffectConfig>) (json, type, ctx) -> {
                if (json.isJsonPrimitive()) {
                    return new StoryPanel.EffectConfig(json.getAsString());
                } else if (json.isJsonObject()) {
                    JsonObject obj = json.getAsJsonObject();
                    StoryPanel.EffectConfig c = new StoryPanel.EffectConfig();
                    if (obj.has("name")) c.name = obj.get("name").getAsString();
                    if (obj.has("type") && c.name == null) c.name = obj.get("type").getAsString();
                    if (obj.has("region")) c.region = ctx.deserialize(obj.get("region"), float[].class);
                    return c;
                }
                return new StoryPanel.EffectConfig();
            })
            .create();

        StoryData data;
        try (Reader reader = new FileReader(storyFile, StandardCharsets.UTF_8)) {
            data = gson.fromJson(reader, StoryData.class);
        }

        assertNotNull(data, "StoryData must not be null");
        assertNotNull(data.panels, "Panels must not be null");
        assertEquals(5, data.panels.size(), "Intro must have exactly 5 panels");

        for (int i = 0; i < data.panels.size(); i++) {
            StoryPanel panel = data.panels.get(i);
            assertNotNull(panel.id);
            assertNotNull(panel.image);
            assertNotNull(panel.text);
            assertFalse(panel.text.isEmpty(), "Panel text should not be empty");

            File img = findAssetFile(panel.image);
            assertNotNull(img, "Image asset must exist for panel " + panel.id + ": " + panel.image);
            assertTrue(img.exists());
        }

        assertNotNull(data.titleCard);
        assertNotNull(data.titleCard.text);
        assertFalse(data.titleCard.text.isEmpty());
        assertEquals("PRESS SPACE TO BEGIN", data.titleCard.prompt);
    }
}
