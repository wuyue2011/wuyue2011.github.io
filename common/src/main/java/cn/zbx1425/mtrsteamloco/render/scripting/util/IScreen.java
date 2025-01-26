package cn.zbx1425.mtrsteamloco.render.scripting.util;

import net.minecraft.client.gui.screens.Screen;

public interface IScreen {
    public static class WithTextrue extends Screen {
        public GraphicsTexture texture;

        protected Screen(Component title, GraphicsTexture texture) {
            super(title);
            this.texture = texture;
        }

        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
            texture.bind();
            super.render(matrices, mouseX, mouseY, delta);
        }
    }
}