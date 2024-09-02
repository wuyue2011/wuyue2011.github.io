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
import net.minecraft.client.renderer.LevelRenderer;

public class MinecraftClientUtil {

    public static boolean worldIsRaining() {
        return Minecraft.getInstance().level != null
                && Minecraft.getInstance().level.isRaining();
    }

    public static boolean worldIsRainingAt(Vector3f pos) {
        return Minecraft.getInstance().level != null
                && Minecraft.getInstance().level.isRainingAt(pos.toBlockPos());
    }

    public static int worldDayTime() {
        return Minecraft.getInstance().level != null
                ? (int) Minecraft.getInstance().level.getDayTime() : 0;
    }

    public static void narrate(String message) {
        Minecraft.getInstance().execute(() -> {
            Narrator.getNarrator().say(message, true);
        });
    }

    public static void displayMessage(String message, boolean actionBar) {
        final Player player = Minecraft.getInstance().player;
        if (player != null) {
            Minecraft.getInstance().execute(() -> {
                player.displayClientMessage(Text.literal(message), actionBar);
            });
        }
    }
    
    public static void levelEvent(int p_109534_, Vector3f p_109535_, int p_109536_){
        final Player player = Minecraft.getInstance().player;
        if (player != null) {
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().level.levelEvent(player, p_109534_, p_109535_.toBlockPos(), p_109536_);
            });
        }
    }
}
