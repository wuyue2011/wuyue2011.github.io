package cn.zbx1425.mtrsteamloco.render.scripting.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import cn.zbx1425.mtrsteamloco.Main;
import com.mojang.blaze3d.systems.RenderSystem;
import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.Scriptable;
import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.NativeObject;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public interface IScreen {
    public class WithTextrue extends Screen {
        public GraphicsTexture texture;
        public Scriptable state;

        public InitFunction initFunction = (screen, width, height) -> {};
        public KeyPressResponder keyPressResponder = (screen, p_96552_, p_96553_, p_96554_) -> false;
        public RenderFunction renderFunction = (screen, matrices, mouseX, mouseY, delta) -> {};
        public Consumer<WithTextrue> onClose = screen -> Minecraft.getInstance().setScreen(null);
        public boolean isPauseScreen = false;

        public WithTextrue(Component title) {
            super(title);
        }

        public WithTextrue(Component title, InitFunction initFunction, KeyPressResponder keyPressResponder, RenderFunction renderFunction, Consumer<WithTextrue> onClose) {
            super(title);
            this.initFunction = initFunction;
            this.keyPressResponder = keyPressResponder;
            this.renderFunction = renderFunction;
            this.onClose = onClose;
            state = new NativeObject();
        }

        @Override
        protected void init() {
            super.init();

            if (texture != null) texture.close();
            texture = new GraphicsTexture(width, height);
            texture.upload();
            try {
                initFunction.init(this, width, height);
            } catch (Exception e) {
                print ("initFunction error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
            boolean flag = false;
            try {
                flag = keyPressResponder.keyPressed(this, p_96552_, p_96553_, p_96554_);
            } catch (Exception e) {
                print ("keyPressResponder error: " + e.getMessage());
                e.printStackTrace();
            }

            return flag || super.keyPressed(p_96552_, p_96553_, p_96554_);
        }

        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);

            try {
                if (texture != null && !texture.isClosed()) {
                    RenderSystem.setShaderTexture(0, texture.identifier);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    blit(matrices, 0, 0, 0f, 0f, texture.width, texture.height, texture.width, texture.height);
                }
            } catch (Exception e) {
                print ("blit error: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                renderFunction.render(this, matrices, mouseX, mouseY, delta);
            } catch (Exception e) {
                print ("renderFunction error: " + e.getMessage());
                e.printStackTrace();
            }

        }

        @Override
        public void onClose() {
            try {
                onClose.accept(this);
            } catch (Exception e) {
                print ("onClose error: " + e.getMessage());
                e.printStackTrace();
            }
            if (texture != null) texture.close();
        }

        void print(String s) {
            Main.LOGGER.info(s);
        }

        @Override
        public boolean isPauseScreen() {
            return isPauseScreen;
        }

        public interface InitFunction {
            void init(WithTextrue screen, int width, int height);
        }

        public interface KeyPressResponder {
            boolean keyPressed(WithTextrue screen, int p_96552_, int p_96553_, int p_96554_);
        }

        public interface RenderFunction {
            void render(WithTextrue screen, PoseStack matrices, int mouseX, int mouseY, float delta);
        }

        public interface InsertTextFunction {
            void insertText(String text, boolean p_96588_);
        }
    }
}