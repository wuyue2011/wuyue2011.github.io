package cn.zbx1425.mtrsteamloco.render.scripting.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import cn.zbx1425.sowcer.math.Vector3f;

public class ParticleHelper {
    public static void addParticle(ParticleOptions particle, Vector3f pos, Vector3f speed){
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().level.addParticle(particle, pos.x(), pos.y(), pos.z(), speed.x(), speed.y(), speed.z());
        });
    }

    public static void addParticle(ParticleOptions particle, boolean overrideLimiter, Vector3f pos, Vector3f speed){
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().level.addParticle(particle, overrideLimiter, pos.x(), pos.y(), pos.z(), speed.x(), speed.y(), speed.z());
        });
    }

    public static void addParticle(ParticleOptions particle, boolean overrideLimiter, boolean overrideLimiter,Vector3f pos, Vector3f speed){
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().level.addParticle(particle, overrideLimiter, pos.x(), pos.y(), pos.z(), speed.x(), speed.y(), speed.z());
        });
    }

    public static void addParticle(ParticleOptions particle, boolean b1, boolean b2,Vector3f pos, Vector3f speed){
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().levelRenderer.addParticle(particle, b1, b2, pos.x(), pos.y(), pos.z(), speed.x(), speed.y(), speed.z());
        });
    }
}