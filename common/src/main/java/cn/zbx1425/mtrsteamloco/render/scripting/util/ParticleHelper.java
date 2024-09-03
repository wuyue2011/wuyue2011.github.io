package cn.zbx1425.mtrsteamloco.render.scripting.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.client.renderer.LevelRenderer;
import cn.zbx1425.sowcer.math.Vector3f;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import java.lang.reflect.Field;

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

    public static <T> T getParticleType(String particleName) {
        switch (particleName.toUpperCase()) {
        case "AMBIENT_ENTITY_EFFECT":
            return ParticleTypes.AMBIENT_ENTITY_EFFECT;
        case "ANGRY_VILLAGER":
            return ParticleTypes.ANGRY_VILLAGER;
        case "BLOCK":
            return ParticleTypes.BLOCK;
        case "BLOCK_MARKER":
            return ParticleTypes.BLOCK_MARKER;
        case "BUBBLE":
            return ParticleTypes.BUBBLE;
        case "CLOUD":
            return ParticleTypes.CLOUD;
        case "CRIT":
            return ParticleTypes.CRIT;
        case "DAMAGE_INDICATOR":
            return ParticleTypes.DAMAGE_INDICATOR;
        case "DRAGON_BREATH":
            return ParticleTypes.DRAGON_BREATH;
        case "DRIPPING_LAVA":
            return ParticleTypes.DRIPPING_LAVA;
        case "FALLING_LAVA":
            return ParticleTypes.FALLING_LAVA;
        case "LANDING_LAVA":
            return ParticleTypes.LANDING_LAVA;
        case "DRIPPING_WATER":
            return ParticleTypes.DRIPPING_WATER;
        case "FALLING_WATER":
            return ParticleTypes.FALLING_WATER;
        case "DUST":
            return ParticleTypes.DUST;
        case "DUST_COLOR_TRANSITION":
            return ParticleTypes.DUST_COLOR_TRANSITION;
        case "EFFECT":
            return ParticleTypes.EFFECT;
        case "ELDER_GUARDIAN":
            return ParticleTypes.ELDER_GUARDIAN;
        case "ENCHANTED_HIT":
            return ParticleTypes.ENCHANTED_HIT;
        case "ENCHANT":
            return ParticleTypes.ENCHANT;
        case "END_ROD":
            return ParticleTypes.END_ROD;
        case "ENTITY_EFFECT":
            return ParticleTypes.ENTITY_EFFECT;
        case "EXPLOSION_EMITTER":
            return ParticleTypes.EXPLOSION_EMITTER;
        case "EXPLOSION":
            return ParticleTypes.EXPLOSION;
        case "FALLING_DUST":
            return ParticleTypes.FALLING_DUST;
        case "FIREWORK":
            return ParticleTypes.FIREWORK;
        case "FISHING":
            return ParticleTypes.FISHING;
        case "FLAME":
            return ParticleTypes.FLAME;
        case "SOUL_FIRE_FLAME":
            return ParticleTypes.SOUL_FIRE_FLAME;
        case "SOUL":
            return ParticleTypes.SOUL;
        case "FLASH":
            return ParticleTypes.FLASH;
        case "HAPPY_VILLAGER":
            return ParticleTypes.HAPPY_VILLAGER;
        case "COMPOSTER":
            return ParticleTypes.COMPOSTER;
        case "HEART":
            return ParticleTypes.HEART;
        case "INSTANT_EFFECT":
            return ParticleTypes.INSTANT_EFFECT;
        case "ITEM":
            return ParticleTypes.ITEM;
        case "VIBRATION":
            return ParticleTypes.VIBRATION;
        case "ITEM_SLIME":
            return ParticleTypes.ITEM_SLIME;
        case "ITEM_SNOWBALL":
            return ParticleTypes.ITEM_SNOWBALL;
        case "LARGE_SMOKE":
            return ParticleTypes.LARGE_SMOKE;
        case "LAVA":
            return ParticleTypes.LAVA;
        case "MYCELIUM":
            return ParticleTypes.MYCELIUM;
        case "NOTE":
            return ParticleTypes.NOTE;
        case "POOF":
            return ParticleTypes.POOF;
        case "PORTAL":
            return ParticleTypes.PORTAL;
        case "RAIN":
            return ParticleTypes.RAIN;
        case "SMOKE":
            return ParticleTypes.SMOKE;
        case "SNEEZE":
            return ParticleTypes.SNEEZE;
        case "SPIT":
            return ParticleTypes.SPIT;
        case "SQUID_INK":
            return ParticleTypes.SQUID_INK;
        case "SWEEP_ATTACK":
            return ParticleTypes.SWEEP_ATTACK;
        case "TOTEM_OF_UNDYING":
            return ParticleTypes.TOTEM_OF_UNDYING;
        case "UNDERWATER":
            return ParticleTypes.UNDERWATER;
        case "SPLASH":
            return ParticleTypes.SPLASH;
        case "WITCH":
            return ParticleTypes.WITCH;
        case "BUBBLE_POP":
            return ParticleTypes.BUBBLE_POP;
        case "CURRENT_DOWN":
            return ParticleTypes.CURRENT_DOWN;
        case "BUBBLE_COLUMN_UP":
            return ParticleTypes.BUBBLE_COLUMN_UP;
        case "NAUTILUS":
            return ParticleTypes.NAUTILUS;
        case "DOLPHIN":
            return ParticleTypes.DOLPHIN;
        case "CAMPFIRE_COSY_SMOKE":
            return ParticleTypes.CAMPFIRE_COSY_SMOKE;
        case "CAMPFIRE_SIGNAL_SMOKE":
            return ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
        case "DRIPPING_HONEY":
            return ParticleTypes.DRIPPING_HONEY;
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
        case "WAX_ON":
            return ParticleTypes.WAX_ON;
        case "WAX_OFF":
            return ParticleTypes.WAX_OFF;
        case "ELECTRIC_SPARK":
            return ParticleTypes.ELECTRIC_SPARK;
        case "SCRAPE":
            return ParticleTypes.SCRAPE;
        default:
            return null;
        }
    }
}