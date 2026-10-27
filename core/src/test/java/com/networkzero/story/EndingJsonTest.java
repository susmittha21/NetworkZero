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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EndingJsonTest {

    private File findEndingJson() {
        File f = new File("assets/data/ending.json");
        if (f.exists()) return f;
        f = new File("../assets/data/ending.json");
        if (f.exists()) return f;
        throw new IllegalStateException("Could not locate ending.json");
    }

    private File findAssetFile(String relativePath) {
        File f = new File("assets", relativePath);
        if (f.exists()) return f;
        f = new File("../assets", relativePath);
        if (f.exists()) return f;
        return null;
    }

    @Test
    void testEndingJsonLoader() throws Exception {
        File endingFile = findEndingJson();
        assertTrue(endingFile.exists(), "ending.json file must exist");

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
                    if (obj.has("point")) c.point = ctx.deserialize(obj.get("point"), float[].class);
                    if (obj.has("points")) c.points = ctx.deserialize(obj.get("points"), float[][].class);
                    if (obj.has("intensity")) c.intensity = obj.get("intensity").getAsFloat();
                    return c;
                }
                return new StoryPanel.EffectConfig();
            })
            .create();

        StoryData data;
        try (Reader reader = new FileReader(endingFile, StandardCharsets.UTF_8)) {
            data = gson.fromJson(reader, StoryData.class);
        }

        assertNotNull(data, "StoryData must not be null");
        assertNotNull(data.panels, "Panels must not be null");
        assertEquals(4, data.panels.size(), "Ending sequence must have exactly 4 panels");

        // Verify all 4 panels referenced
        String[] expectedImages = {
            "end_panel_1.png",
            "end_panel_2.png",
            "end_panel_3.png",
            "end_panel_5.png"
        };

        for (int i = 0; i < data.panels.size(); i++) {
            StoryPanel panel = data.panels.get(i);
            assertNotNull(panel.id, "Panel id must be present");
            assertNotNull(panel.image, "Panel image must be present");
            assertTrue(panel.image.contains(expectedImages[i]),
                "Panel " + i + " should reference " + expectedImages[i] + " but was " + panel.image);

            // Verify file exists on disk
            File imageFile = findAssetFile(panel.image);
            if (imageFile == null) {
                // Try inside story/ or story/end_panel/
                imageFile = findAssetFile("story/" + panel.image);
                if (imageFile == null) imageFile = findAssetFile("story/end_panel/" + panel.image);
            }
            assertNotNull(imageFile, "Image file must exist on disk: " + panel.image);
            assertTrue(imageFile.exists(), "Image file must exist on disk: " + imageFile.getPath());

            // Verify text lines present
            assertNotNull(panel.text, "Panel text must not be null");
            assertFalse(panel.text.isEmpty(), "Panel text lines must be present");
            for (String line : panel.text) {
                assertFalse(line.isBlank(), "Text line must not be blank");
            }

            // Verify camera parameters
            if (panel.camera != null) {
                assertTrue(panel.camera.startZoom >= 1.00f && panel.camera.startZoom <= 1.12f,
                    "startZoom must be between 1.00 and 1.12");
                assertTrue(panel.camera.endZoom >= 1.00f && panel.camera.endZoom <= 1.12f,
                    "endZoom must be between 1.00 and 1.12");
                assertTrue(panel.camera.duration > 0, "Camera duration must be positive");
            }

            // Verify normalised effect regions if any are present
            if (panel.effects != null && !panel.effects.isEmpty()) {
                for (StoryPanel.EffectConfig eff : panel.effects) {
                    assertNotNull(eff.name, "Effect name must not be null");
                    if (eff.region != null) {
                        for (int k = 0; k < eff.region.length; k++) {
                            assertTrue(eff.region[k] >= 0.0f && eff.region[k] <= 1.0f,
                                "Region coordinate " + k + " (" + eff.region[k] + ") in " + eff.name + " must be in [0, 1]");
                        }
                    }
                    if (eff.point != null) {
                        for (int k = 0; k < eff.point.length; k++) {
                            assertTrue(eff.point[k] >= 0.0f && eff.point[k] <= 1.0f,
                                "Point coordinate " + k + " (" + eff.point[k] + ") in " + eff.name + " must be in [0, 1]");
                        }
                    }
                    if (eff.points != null) {
                        for (float[] pt : eff.points) {
                            for (int k = 0; k < pt.length; k++) {
                                assertTrue(pt[k] >= 0.0f && pt[k] <= 1.0f,
                                    "Point coordinate " + k + " (" + pt[k] + ") in " + eff.name + " must be in [0, 1]");
                            }
                        }
                    }
                }
            }
        }

        // Verify title card
        assertNotNull(data.titleCard, "titleCard must be present");
        assertNotNull(data.titleCard.text, "titleCard text must be present");
        assertTrue(data.titleCard.text.size() >= 2, "titleCard must have at least 2 lines");
        assertEquals("NETWORK RESTORED.", data.titleCard.text.get(0));
        assertEquals("THE LAST LAB SESSION IS OVER.", data.titleCard.text.get(1));
        assertEquals("PRESS SPACE TO CONTINUE", data.titleCard.prompt);
    }
}
