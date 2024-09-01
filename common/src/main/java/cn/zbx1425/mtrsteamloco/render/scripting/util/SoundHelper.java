package cn.zbx1425.mtrsteamloco.render.scripting.util;

import cn.zbx1425.sowcer.math.Vector3f;
import com.mojang.text2speech.Narrator;
import mtr.mappings.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.commands.synchronization.brigadier.StringArgumentSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.client.multiplayer.ClientLevel;

public class SoundHelper {

    public static void play(SoundInstance sound) {
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getSoundManager().play(sound);            
        });
    }

    public static void play(SoundInstance sound, int delay) {
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getSoundManager().playDelayed(sound, delay);
        });
    }

    public static void play(SoundEvent sound, Vector3f pos, SoundSource source, float volume, float pitch) {
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().level.playLocalSound(pos.x(), pos.y(), pos.z(), sound, source, volume, pitch, false);
        });
    }

    public static void play(SoundEvent sound, Vector3f pos, float volume, float pitch) {
        this.play(sound, pos, SoundSource.BLOCKS, volume, pitch);
    }

    public static void stop() {
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getSoundManager().stop();
        });
    }

    public static void stop(SoundInstance sound) {
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getSoundManager().stop(sound);
        });
    }

    public static void stop(ResourceLocation sound, SoundSource source) {
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getSoundManager().stop(sound, source);
        });
    }

    public static void stop(ResourceLocation sound){
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getSoundManager().stop(sound, SoundSource.BLOCKS);
        });
    }
}
