package cn.zbx1425.mtrsteamloco.test;//render.scripting.util1

import mtr.mappings.TickableSoundInstanceMapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import cn.zbx1425.sowcer.math.Vector3f;

public class TickableSound extends TickableSoundInstanceMapper {
    public TickableSound(SoundEvent event){
        super(event, SoundSource.BLOCKS);
    }

    public TickableSound(SoundEvent event, SoundSource source){
        super(event, source);
    }
}