package cn.zbx1425.mtrsteamloco.render.scripting.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.client.renderer.LevelRenderer;
import cn.zbx1425.sowcer.math.Vector3f;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

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

    public static void addParticle(ParticleOptions particle, boolean b1, boolean b2,Vector3f pos, Vector3f speed){
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().levelRenderer.addParticle(particle, b1, b2, pos.x(), pos.y(), pos.z(), speed.x(), speed.y(), speed.z());
        });
    }

    public static SimpleParticleType getParticleType(String particleName) {
        switch (particleName) {
            case "FALLING_HONEY":
                return ParticleTypes.FALLING_HONEY;
            case "LANDING_HONEY":
                return ParticleTypes.LANDING_HONEY;
            case "FALLING_NECTAR":
                return ParticleTypes.FALLING_NECTAR;
            case "FALLING_SPORE_BLOSSOM":
                return ParticleTypes.FALLING_SPORE_BLOSSOM;
            case "ASH":
                return ParticleTypes.ASH;
            case "CRIMSON_SPORE":
                return ParticleTypes.CRIMSON_SPORE;
            case "WARPED_SPORE":
                return ParticleTypes.WARPED_SPORE;
            case "SPORE_BLOSSOM_AIR":
                return ParticleTypes.SPORE_BLOSSOM_AIR;
            case "DRIPPING_OBSIDIAN_TEAR":
                return ParticleTypes.DRIPPING_OBSIDIAN_TEAR;
            case "FALLING_OBSIDIAN_TEAR":
                return ParticleTypes.FALLING_OBSIDIAN_TEAR;
            case "LANDING_OBSIDIAN_TEAR":
                return ParticleTypes.LANDING_OBSIDIAN_TEAR;
            case "REVERSE_PORTAL":
                return ParticleTypes.REVERSE_PORTAL;
            case "WHITE_ASH":
                return ParticleTypes.WHITE_ASH;
            case "SMALL_FLAME":
                return ParticleTypes.SMALL_FLAME;
            case "SNOWFLAKE":
                return ParticleTypes.SNOWFLAKE;
            case "DRIPPING_DRIPSTONE_LAVA":
                return ParticleTypes.DRIPPING_DRIPSTONE_LAVA;
            case "FALLING_DRIPSTONE_LAVA":
                return ParticleTypes.FALLING_DRIPSTONE_LAVA;
            case "DRIPPING_DRIPSTONE_WATER":
                return ParticleTypes.DRIPPING_DRIPSTONE_WATER;
            case "FALLING_DRIPSTONE_WATER":
                return ParticleTypes.FALLING_DRIPSTONE_WATER;
            case "GLOW_SQUID_INK":
                return ParticleTypes.GLOW_SQUID_INK;
            case "GLOW":
                return ParticleTypes.GLOW;
            default:
                throw new IllegalArgumentException("Unknown particle type: " + particleName);
        }
    }
}