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
import net.minecraft.world.entity.player.Player;

public class SoundHelper {

    public static void play(SoundInstance sound) {
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getSoundManager().play(sound);            
        });
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
