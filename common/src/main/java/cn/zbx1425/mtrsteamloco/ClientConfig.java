package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.render.ShadersModHandler;
import cn.zbx1425.mtrsteamloco.data.ConfigResponder;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import me.shedaniel.clothconfig2.impl.builders.StringFieldBuilder;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.impl.builders.TextDescriptionBuilder;
import net.minecraft.network.chat.Component;
import mtr.mappings.Text;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.*;
import java.util.function.Consumer;

public class ClientConfig {

    private static Path path;

    public static boolean enableOptimization = true;
    public static boolean enableBbModelPreload = false;
    public static boolean translucentSort = false;

    public static boolean enableScriptDebugOverlay = false;

    public static boolean enableRail3D = true;
    public static boolean enableRailRender = true;
    public static boolean enableTrainRender = true;
    public static boolean enableTrainSound = true;
    public static boolean enableSmoke = true;

    public static boolean hideRidingTrain = false;

    private static Map<String, String> customConfig = new HashMap<>();
    private static Map<String, ConfigResponder> customResponders = new HashMap<>();

    public static void load(Path path) {
        ClientConfig.path = path;
        if (!Files.exists(path)) {
            save();
        }
        try {
            JsonObject configObject = Main.JSON_PARSER.parse(Files.readString(path)).getAsJsonObject();
            enableOptimization = !getOrDefault(configObject, "shaderCompatMode", JsonElement::getAsBoolean, false);
            enableBbModelPreload = getOrDefault(configObject, "enableBbModelPreload", JsonElement::getAsBoolean, false);
            translucentSort = getOrDefault(configObject, "translucentSort", JsonElement::getAsBoolean, false);
            enableScriptDebugOverlay = getOrDefault(configObject, "enableScriptDebugOverlay", JsonElement::getAsBoolean, false);
            enableRail3D = getOrDefault(configObject, "enableRail3D", JsonElement::getAsBoolean, true);
            enableRailRender = getOrDefault(configObject, "enableRailRender", JsonElement::getAsBoolean, true);
            enableTrainRender = getOrDefault(configObject, "enableTrainRender", JsonElement::getAsBoolean, true);
            enableTrainSound = getOrDefault(configObject, "enableTrainSound", JsonElement::getAsBoolean, true);
            enableSmoke = getOrDefault(configObject, "enableSmoke", JsonElement::getAsBoolean, true);
            hideRidingTrain = getOrDefault(configObject, "hideRidingTrain", JsonElement::getAsBoolean, false);

            customConfig.clear();
            if (configObject.has("custom")) {
                JsonObject customObject = configObject.getAsJsonObject("custom");
                Set<String> keys = customObject.keySet();
                for (String key : keys) {
                    customConfig.put(key, customObject.get(key).getAsString());
                }
            }
        } catch (Exception ex) {
            Main.LOGGER.warn("Failed loading client config:", ex);
            save();
        }
    }

    private static <T> T getOrDefault(JsonObject jsonObject, String key, Function<JsonElement, T> getter, T defaultValue) {
        if (jsonObject.has(key)) {
            return getter.apply(jsonObject.get(key));
        } else {
            return defaultValue;
        }
    }

    public static int getRailRenderLevel() {
        if (!useRenderOptimization()) {
            return enableRailRender ? 1 : 0;
        } else {
            return enableRailRender
                    ? (enableRail3D ? (ShadersModHandler.canInstance() ? 3 : 2) : 1)
                    : 0;
        }
    }

    public static boolean useRenderOptimization() {
        return enableOptimization && ShadersModHandler.canDrawWithBuffer();
    }

    public static void save() {
        try {
            if (path == null) return;
            JsonObject configObject = new JsonObject();
            configObject.addProperty("shaderCompatMode", !enableOptimization);
            configObject.addProperty("enableBbModelPreload", enableBbModelPreload);
            configObject.addProperty("translucentSort", translucentSort);
            configObject.addProperty("enableScriptDebugOverlay", enableScriptDebugOverlay);
            configObject.addProperty("enableRail3D", enableRail3D);
            configObject.addProperty("enableRailRender", enableRailRender);
            configObject.addProperty("enableTrainRender", enableTrainRender);
            configObject.addProperty("enableTrainSound", enableTrainSound);
            configObject.addProperty("enableSmoke", enableSmoke);
            configObject.addProperty("hideRidingTrain", hideRidingTrain);

            JsonObject customObject = new JsonObject();
            for (Map.Entry<String, String> entry : customConfig.entrySet()) {
                customObject.addProperty(entry.getKey(), entry.getValue());
            }

            configObject.add("custom", customObject);

            Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(configObject));
        } catch (Exception ex) {
            Main.LOGGER.warn("Failed loading client config:", ex);
        }
    }

    public static void load() {
        load(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("mtrsteamloco.json"));
    }

    public static void register(ConfigResponder responder) {
        if (!customConfig.containsKey(responder.key)) {
            customConfig.put(responder.key, responder.defaultValue);
        }
        responder.bind(customConfig);
        customResponders.put(responder.key, responder);
    }

    public static String get(String key) {
        return customConfig.get(key);
    }

    public static List<AbstractConfigListEntry> getCustomConfigEntrys() {
        Set<String> keys = customConfig.keySet();
        List<String> usedKeys = new ArrayList<>();
        List<String> unusedKeys = new ArrayList<>();
        for (String key : keys) {
            if (customResponders.containsKey(key)) {
                usedKeys.add(key);
            } else {
                unusedKeys.add(key);
            }
        }
        List<AbstractConfigListEntry> entries = new ArrayList<>();
        if (!usedKeys.isEmpty()) {
            entries.add(new TextDescriptionBuilder(ConfigResponder.resetButtonKey, Text.literal(UUID.randomUUID().toString()), Text.literal("已使用的自定义配置")).build());
            for (String key : usedKeys) {
                entries.add(customResponders.get(key).getListEntry(customConfig.get(key)));
            }
        }
        if (!unusedKeys.isEmpty()) {
            entries.add(new TextDescriptionBuilder(ConfigResponder.resetButtonKey, Text.literal(UUID.randomUUID().toString()), Text.literal("未使用的自定义配置")).build());
            for (String key : unusedKeys) {
                entries.add(new StringFieldBuilder(ConfigResponder.resetButtonKey, Text.translatable(key), customConfig.get(key)).setSaveConsumer(str -> customConfig.put(key, str)).setDefaultValue(customConfig.get(key)).build());
            };
        }
        return entries;
    }
}
