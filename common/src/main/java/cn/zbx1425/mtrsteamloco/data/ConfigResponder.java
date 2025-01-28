package cn.zbx1425.mtrsteamloco.data;

import mtr.mappings.Text;
import net.minecraft.network.chat.Component;
import me.shedaniel.clothconfig2.impl.builders.StringFieldBuilder;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.gui.screens.Screen;
import me.shedaniel.clothconfig2.impl.builders.TextFieldBuilder;

import java.util.*;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ConfigResponder {

    public abstract String key();
    public abstract void init(Map<String, String> map);
    public abstract List<AbstractConfigListEntry> getListEntries(Map<String, String> map, ConfigEntryBuilder builder, Supplier<Screen> screenSupplier);

    public static class TextField implements ConfigResponder {
        public Function<String, String> transformer = str -> str;
        public Function<String, Optional<Component>> errorSupplier = str -> Optional.empty();
        public Consumer<String> saveConsumer = str -> {};
        public String key;
        public String defaultValue;
        public Component name;
        public Function<String, Optional<Component[]>> tooltipSupplier = str -> Optional.empty();
        public boolean requireRestart = false;
        
        public TextField(String key, Component name, String defaultValue) {
            this.key = key;
            setName(name);
            setDefaultValue(defaultValue);
        }

        public TextField(String key, Component name, String defaultValue, Function<String, String> transformer, Function<String, Optional<Component>> errorSupplier, Consumer<String> saveConsumer, Function<String, Optional<Component[]>> tooltipSupplier, boolean requireRestart) {
            this.key = key;
            setName(name);
            setDefaultValue(defaultValue);
            setTransformer(transformer);
            setErrorSupplier(errorSupplier);
            setSaveConsumer(saveConsumer);
            setTooltipSupplier(tooltipSupplier);
            setRequireRestart(requireRestart);
        }
        @Override
        public void init(Map<String, String> map) {
            if (!map.containsKey(key)) {
                map.put(key, defaultValue);
            }
        }
        @Override
        public String key() {
            return key;
        }
        @Override
        public List<AbstractConfigListEntry> getListEntries(Map<String, String> map, ConfigEntryBuilder builder,Supplier<Screen> screenSupplier) {
            TextFieldBuilder textFieldbuilder = builder.startTextField(name, transformer.apply(map.getOrDefault(key,defaultValue))).setSaveConsumer((str) -> {saveConsumer.accept(str); map.put(key, str);}).setDefaultValue(defaultValue).setErrorSupplier(errorSupplier).setTooltipSupplier(tooltipSupplier);
            if (requireRestart) {
                textFieldbuilder.requireRestart();
            }
            return Collections.singletonList(textFieldbuilder.build());
        }
        public TextField setTransformer(Function<String, String> transformer) {
            this.transformer = transformer;
            return this;
        }
        public TextField setErrorSupplier(Function<String, Optional<Component>> errorSupplier) {
            this.errorSupplier = errorSupplier;
            return this;
        }
        public TextField setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        public TextField setName(Component name) {
            this.name = name;
            return this;
        }
        public TextField setSaveConsumer(Consumer<String> saveConsumer) {
            this.saveConsumer = saveConsumer;
            return this;
        }
        public TextField setTooltipSupplier(Function<String, Optional<Component[]>> tooltipSupplier) {
            this.tooltipSupplier = tooltipSupplier;
            return this;
        }
        public TextField setRequireRestart(boolean requireRestart) {
            this.requireRestart = requireRestart;
            return this;
        }
    }
    
}