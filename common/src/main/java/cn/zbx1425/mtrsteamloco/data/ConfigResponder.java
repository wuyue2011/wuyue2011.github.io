package cn.zbx1425.mtrsteamloco.data;

import mtr.mappings.Text;
import net.minecraft.network.chat.Component;
import me.shedaniel.clothconfig2.impl.builders.StringFieldBuilder;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;

import java.util.function.Function;
import java.util.*;
import java.util.function.Consumer;

public class ConfigResponder {
    public static final Component resetButtonKey = Text.translatable("text.cloth-config.reset_value");
    public Function<String, String> transformer;
    public Function<String, Optional<Component>> errorSupplier;
    public Consumer<String> saveConsumer;
    public final String key;
    public String defaultValue;
    public Component name;
    public ConfigResponder(String key, Component name, String defaultValue, Function<String, String> transformer,Function<String, Optional<Component>> errorSupplier, Consumer<String> saveConsumer) {
        this.transformer = transformer;
        this.errorSupplier =  errorSupplier;
        this.saveConsumer = str -> {
            saveConsumer.accept(str);
        };
        this.key = key;
        this.defaultValue = defaultValue;
        this.name = name;
    }

    public void bind(Map<String, String> customConfig) {
        saveConsumer = str -> {
            saveConsumer.accept(str);
            customConfig.put(key, str);
        };
    }

    public StringListEntry getListEntry(String value) {
        StringFieldBuilder builder = new StringFieldBuilder(resetButtonKey, name, transformer.apply(value));
        builder.setErrorSupplier(errorSupplier).setSaveConsumer(saveConsumer).setDefaultValue(defaultValue);
        return builder.build();
    }
}