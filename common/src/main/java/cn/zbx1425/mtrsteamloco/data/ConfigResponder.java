package cn.zbx1425.mtrsteamloco.data;

import mtr.mappings.Text;
import net.minecraft.network.chat.Component;
import me.shedaniel.clothconfig2.impl.builders.StringFieldBuilder;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.TextFieldBuilder;

import java.util.function.Function;
import java.util.*;
import java.util.function.Consumer;

public class ConfigResponder {
    // public static final Component resetButtonKey = Text.translatable("text.cloth-config.reset_value");
    public Function<String, String> transformer;
    public Function<String, Optional<Component>> errorSupplier;
    public Consumer<String> saveConsumer;
    public final String key;
    public String defaultValue;
    public Component name;
    Consumer<TextFieldBuilder> consumer;
    public ConfigResponder(String key, Component name, String defaultValue, Function<String, String> transformer,Function<String, Optional<Component>> errorSupplier, Consumer<String> saveConsumer, Consumer<TextFieldBuilder> consumer) {
        this.transformer = transformer;
        this.errorSupplier =  errorSupplier;
        this.saveConsumer = saveConsumer;
        this.key = key;
        this.defaultValue = defaultValue;
        this.name = name;
        this.consumer = consumer;
    }

    public StringListEntry getListEntry(Map<String, String> map, ConfigEntryBuilder builder) {
        TextFieldBuilder textFieldbuilder = builder.startTextField(name, transformer.apply(map.getOrDefault(key, defaultValue))).setSaveConsumer((str) -> {saveConsumer.accept(str); map.put(key, str);}).setDefaultValue(defaultValue).setErrorSupplier(errorSupplier);
        consumer.accept(textFieldbuilder);
        return textFieldbuilder.build();
    }
}