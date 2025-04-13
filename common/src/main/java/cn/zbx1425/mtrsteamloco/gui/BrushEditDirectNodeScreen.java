package cn.zbx1425.mtrsteamloco.gui;

import mtr.mappings.Text;
import mtr.mappings.UtilitiesClient;
import net.minecraft.core.BlockPos;
import mtr.client.IDrawing;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import cn.zbx1425.mtrsteamloco.block.BlockDirectNode.BlockEntityDirectNode;
import mtr.screen.WidgetBetterTextField;
import mtr.data.IGui;
#if MC_VERSION >= "12000"
import net.minecraft.client.gui.GuiGraphics;
#endif
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrushEditDirectNodeScreen extends Screen {
    public static Screen createScreen(Level world, BlockPos pos, Screen parent) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity == null) return parent;
        if (entity instanceof BlockEntityDirectNode e) {
            return new BrushEditDirectNodeScreen(e, parent);
        } else {
            return parent;
        }
    }

    private BlockEntityDirectNode entity;
    private Screen parent;
    #if MC_VERSION >= "12000"
    private GuiGraphics matrices;
#else 
    private PoseStack matrices;
#endif
    private int mouseX, mouseY;
    private float partialTick;

    Button btnReturn = UtilitiesClient.newButton(Text.literal("X"), btn -> onClose());
    static int mode = 0;
    Button btnCirculateMode = UtilitiesClient.newButton(Text.literal("⇄"), btn -> switchMode(mode + 1));
    Pattern pattern;

    Button btnUnbind = UtilitiesClient.newButton(Text.translatable("gui.mtrsteamloco.direct_node.unbind"), btn -> entity.unbind());
    
    BrushEditDirectNodeScreen(BlockEntityDirectNode entity, Screen parent) {
        super(Text.literal("Brush Edit Direct Node Screen"));
        this.entity = entity;
        this.parent = parent;
        switchMode(mode);
    }

    private void switchMode(int mode) {
        mode %= 2;
        BrushEditDirectNodeScreen.mode = mode;
        pattern = mode == 0 ? new DegreeSlider() : new DegreeTextField();
    }

    @Override
#if MC_VERSION >= "12000"
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
#else
    public void render(PoseStack guiGraphics, int mouseX, int mouseY, float partialTick) {
#endif
        this.matrices = guiGraphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTick = partialTick;
        IDrawing.setPositionAndWidth(btnReturn, 20, 20, 20);
        int w = Math.min(width - 40, 380);
        IDrawing.setPositionAndWidth(btnCirculateMode, width / 2 + w / 2 - 40, height - 80, 40);
        IDrawing.setPositionAndWidth(btnUnbind, width / 2 - w / 2, height - 80, 60);
        btnUnbind.active = entity.isBound();
        btnUnbind.render(matrices, mouseX, mouseY, partialTick);
        btnReturn.render(matrices, mouseX, mouseY, partialTick);
        btnCirculateMode.render(matrices, mouseX, mouseY, partialTick);
        pattern.render();
    }

    private void bindAngle(float degree) {
        entity.bind(degree);
    }

    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> children = new ArrayList<>();
        children.addAll(super.children());
        children.add(btnReturn);
        children.add(btnCirculateMode);
        children.add(btnUnbind);
        children.addAll(pattern.children());
        return children;
    }

    public interface Pattern {
        public void render();

        public List<? extends GuiEventListener> children();
    }

    public class DegreeSlider implements Pattern {
        WidgetSlider slider;
        int now = (int) Math.round(entity.getAngleDegrees() + 180F);

        public DegreeSlider() {
            slider = new WidgetSlider(360, now, integer -> {
                int p = getNow();
                if (integer != p) {
                    setNow(integer);
                }
                return "Degrees: " + (integer - 180F);
            }); 
        }

        public int getNow() {
            return now;
        }

        public void setNow(int now) {
            this.now = now;
            bindAngle(now - 180F);
        }

        @Override
        public void render() {
            int w = Math.min(width - 40, 380);
            IDrawing.setPositionAndWidth(slider, width / 2 - w / 2, BrushEditDirectNodeScreen.this.height - 50, width / 2 + w / 2);
            slider.render(matrices, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.singletonList(slider);
        }
    }

    public class DegreeTextField implements Pattern {
        WidgetBetterTextField textField;
        float now = entity.getAngleDegrees();

        public DegreeTextField() {
            textField = new WidgetBetterTextField(String.format("%.1f", now), 10);
            textField.setResponder(s -> {
                float p = getNow();
                try {
                    float f = Float.parseFloat(s);
                    if (f != p) {
                        setNow(f);
                    }
                    textField.setTextColor(0xFFFFFFFF);
                } catch (NumberFormatException e) {
                    textField.setTextColor(0xFFFF0000);
                }
            });
            textField.setValue(String.format("%.1f", now));
            textField.moveCursorToStart();
        }


        @Override
        public void render() {
            int w = Math.min(width - 40, 380 - IGui.TEXT_FIELD_PADDING);
            IDrawing.setPositionAndWidth(textField, width / 2 - w / 2, BrushEditDirectNodeScreen.this.height - 50, width / 2 + w / 2);
            textField.render(matrices, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.singletonList(textField);
        }

        public float getNow() {
            return now;
        }

        public void setNow(float now) {
            this.now = now;
            bindAngle(now);
        }
    }
}