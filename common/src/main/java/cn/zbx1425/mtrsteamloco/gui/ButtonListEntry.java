package cn.zbx1425.mtrsteamloco.gui;

import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.gui.components.Button;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.mappings.UtilitiesClient;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ButtonListEntry extends TooltipListEntry<Boolean>{
    private final Button buttonWidget;
    private final List<AbstractWidget> widgets;

    public ButtonListEntry(Component fieldName, Button.OnPress onPress) {
        super(fieldName, null);
        
        buttonWidget = UtilitiesClient.newButton(
                fieldName,
                onPress
        );

        this.widgets = Lists.newArrayList(buttonWidget);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return widgets;
    }
    
    @Override
    public List<? extends NarratableEntry> narratables() {
        return widgets;
    }

    @Override
    public boolean isEdited() {
        return false;
    }

    @Override
    public Boolean getValue() {
        return false;
    }

    @Override
    public Optional<Boolean> getDefaultValue() {
        return Optional.ofNullable(false);
    }

    @Override
    public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        Window window = Minecraft.getInstance().getWindow();
        this.buttonWidget.active = isEditable();
        this.buttonWidget.y = y;
        this.buttonWidget.setMessage(fieldName);
        this.buttonWidget.setWidth(entryWidth * 0.7f);
        buttonWidget.render(matrices, mouseX, mouseY, delta);
    }
}