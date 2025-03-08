package cn.zbx1425.mtrsteamloco.render.scripting.util;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.mixin.DynamicImageAccessor;
import cn.zbx1425.mtrsteamloco.mixin.NativeImageAccessor;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.awt.image.*;
import java.io.Closeable;
import java.nio.IntBuffer;
import java.util.UUID;
import java.util.concurrent.*;

@SuppressWarnings("unused")
public class GraphicsTexture implements Closeable {

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final DynamicTexture dynamicTexture;
    public final ResourceLocation identifier;

    public BufferedImage bufferedImage;
    public Graphics2D graphics;

    public final int width, height;

    public GraphicsTexture(int width, int height) {
        this.width = width;
        this.height = height;

        Suit suit = createBuffer();
        this.bufferedImage = suit.bufferedImage;
        this.graphics = suit.graphics;

        dynamicTexture = new DynamicTexture(suit.nativeImage);
        identifier = new ResourceLocation(Main.MOD_ID, String.format("dynamic/graphics/%s", UUID.randomUUID()));
        Minecraft.getInstance().execute(() -> {
            int prevTextureBinding = GL33.glGetInteger(GL33.GL_TEXTURE_BINDING_2D);
            dynamicTexture.bind();
            GL33.glTexParameteriv(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_RGBA,
                    new int[] { GL33.GL_BLUE, GL33.GL_GREEN, GL33.GL_RED, GL33.GL_ALPHA });
            GlStateManager._bindTexture(prevTextureBinding);
            Minecraft.getInstance().getTextureManager().register(identifier, dynamicTexture);
        });
    }

    public static BufferedImage createArgbBufferedImage(BufferedImage src) {
        BufferedImage newImage = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = newImage.createGraphics();
        graphics.drawImage(src, 0, 0, null);
        graphics.dispose();
        return newImage;
    }

    public void upload() {
        RenderSystem.recordRenderCall(dynamicTexture::upload);
    }

    @Override
    public void close() {
        graphics.dispose();
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getTextureManager().release(identifier);
        });
    }

    public void closeLater(int delay) {
        Runnable task = () -> close();
        executor.schedule(task, delay, TimeUnit.MILLISECONDS);
    } 

    public void setPixels(Suit suit) {
        RenderSystem.recordRenderCall(() -> {
            dynamicTexture.setPixels(suit.nativeImage);
            dynamicTexture.upload();
        });
        this.bufferedImage = suit.bufferedImage;
        this.graphics.dispose();
        this.graphics = suit.graphics;
    }

    public Suit createBuffer() {
        return new Suit();
    }

    private static class IntBufDataBuffer extends DataBuffer {

        IntBuffer buffer;

        protected IntBufDataBuffer(IntBuffer buffer, int size) {
            super(DataBuffer.TYPE_INT, size);
            this.buffer = buffer;
        }

        @Override
        public int getElem(int bank, int i) {
            return buffer.get(i);
        }

        @Override
        public void setElem(int bank, int i, int val) {
            buffer.put(i, val);
        }
    }

    public class Suit {

        public NativeImage nativeImage;
        public BufferedImage bufferedImage;
        public Graphics2D graphics;

        public Suit() {
            nativeImage = new NativeImage(width, height, false);
            long pixelAddr = ((NativeImageAccessor)(Object)nativeImage).getPixels();
            IntBuffer target = MemoryUtil.memByteBuffer(pixelAddr, width * height * 4).asIntBuffer();
            DataBuffer dataBuffer = new IntBufDataBuffer(target, width * height);
            WritableRaster raster = Raster.createPackedRaster(dataBuffer, width, height, width,
                    new int[] { 0xFF0000, 0xFF00, 0xFF, 0xFF000000 }, new Point(0, 0));
            bufferedImage = new BufferedImage(ColorModel.getRGBdefault(), raster, false, null);
            graphics = bufferedImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        }

        public void close() {
            nativeImage.close();
            graphics.dispose();
        }

        public void closeLater(int delay) {
            Runnable task = () -> close();
            executor.schedule(task, delay, TimeUnit.MILLISECONDS);
        }
    }
}
