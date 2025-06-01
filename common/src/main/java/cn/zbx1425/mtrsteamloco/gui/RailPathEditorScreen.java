package cn.zbx1425.mtrsteamloco.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.screens.Screen;
import mtr.data.Rail;
import mtr.mappings.Text;

public class RailPathEditorScreen extends Screen {

    public static Screen createScreen(BlockPos posStart, BlockPos posEnd, Rail rail, Screen parent) {
        return new RailPathEditorScreen(posStart, posEnd, rail, parent);
    }

    private final BlockPos posStart;
    private final BlockPos posEnd;
    private final Rail rail;
    private Screen parent;

    protected RailPathEditorScreen(BlockPos posStart, BlockPos posEnd, Rail rail, Screen parent) {
        super(Text.literal("Rail Path Editor"));
        this.posStart = posStart;
        this.posEnd = posEnd;
        this.rail = rail;
        this.parent = parent;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}