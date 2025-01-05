package cn.zbx1425.mtrsteamloco.render.scripting.util;

import mtr.mappings.Text;
import net.minecraft.network.chat.Component;

public interface ComponentUtil extends Text{

    static String getString(Component component) {
        return component.getString();
    }
}