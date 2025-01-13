package cn.zbx1425.mtrsteamloco.data;

import mtr.mappings.Text;
import net.minecraft.network.chat.Component;
import me.shedaniel.clothconfig2.impl.builders.StringFieldBuilder;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.TextFieldBuilder;

import java.util.*;
import java.util.function.Function;
import java.util.function.Consumer;

public class ConfigResponder {
    public Function<String, String> transformer = str -> str;
    public Function<String, Optional<Component>> errorSupplier = str -> Optional.empty();
    public Consumer<String> saveConsumer = str -> {};
    public final String key;
    public String defaultValue;
    public Component name;
    public Function<String, Optional<Component[]>> tooltipSupplier = str -> Optional.empty();
    public boolean requireRestart = false;

    public ConfigResponder(String key, Component name, String defaultValue) {
        this.key = key;
        setName(name);
        setDefaultValue(defaultValue);
    }

    public ConfigResponder(String key, Component name, String defaultValue, Function<String, String> transformer,Function<String, Optional<Component>> errorSupplier, Consumer<String> saveConsumer, Function<String, Optional<List<Component>>> tooltipSupplier, boolean requireRestart) {
        this.key = key;
        setName(name);
        setDefaultValue(defaultValue);
        setTransformer(transformer);
        setErrorSupplier(errorSupplier);
        setSaveConsumer(saveConsumer);
        setTooltipSupplier(tooltipSupplier);
        setRequireRestart(requireRestart);
    }

    public StringListEntry getListEntry(Map<String, String> map, ConfigEntryBuilder builder) {
        TextFieldBuilder textFieldbuilder = builder.startTextField(name, transformer.apply(map.getOrDefault(key, defaultValue))).setSaveConsumer((str) -> {saveConsumer.accept(str); map.put(key, str);}).setDefaultValue(defaultValue).setErrorSupplier(errorSupplier).setTooltipSupplier(tooltipSupplier);
        if (requireRestart) {
            textFieldbuilder.requireRestart();
        }
        return textFieldbuilder.build();
    }

    public ConfigResponder setTransformer(Function<String, String> transformer) {
        this.transformer = transformer;
        return this;
    }

    public ConfigResponder setErrorSupplier(Function<String, Optional<Component>> errorSupplier) {
        this.errorSupplier = errorSupplier;
        return this;
    }

    public ConfigResponder setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public ConfigResponder setName(Component name) {
        this.name = name;
        return this;
    }

    public ConfigResponder setSaveConsumer(Consumer<String> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }

    public ConfigResponder setTooltipSupplier(Function<String, Optional<List<Component>>> tooltipSupplier) {
        this.tooltipSupplier = str -> tooltipSupplier.apply(str).map(list -> list.toArray(new Component[0]));
        return this;
    }

    public ConfigResponder setRequireRestart(boolean requireRestart) {
        this.requireRestart = requireRestart;
        return this;
    }
}