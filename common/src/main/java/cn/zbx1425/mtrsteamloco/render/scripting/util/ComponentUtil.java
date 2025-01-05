package cn.zbx1425.mtrsteamloco.render.scripting.util;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.Component;

public interface ComponentUtil {

	static MutableComponent translatable(String text, Object... objects) {
		return new TranslatableComponent(text, objects);
	}

	static MutableComponent literal(String text) {
		return new TextComponent(text);
	}

    static String getString(Component component) {
        return component.getString();
    }
}