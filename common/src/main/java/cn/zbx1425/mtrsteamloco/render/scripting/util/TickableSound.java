package cn.zbx1425.mtrsteamloco.render.scripting.util;

import mtr.mappings.TickableSoundInstanceMapper;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import cn.zbx1425.sowcer.math.Vector3f;

public class TickableSound extends TickableSoundInstanceMapper{
    public TickableSound(SoundEvent event){
        super(event, SoundSource.BLOCKS);
    }

    public TickableSound(SoundEvent event, SoundSource source){
        super(event, source);
    }

    public void setVolume(float volume1){
        volume = volume1;
    }

    public void setPitch(float pitch1){
        pitch = pitch1;
    }

    public void play(){
        Minecraft.getInstance().getSoundManager().play(this);
    }

    public void setData(float volume1, float pitch1, Vector3f position, boolean looping1, int delay1, boolean relative1){
        volume = volume1;
        pitch = pitch1;
        x = position.x();
        y = position.y();
        z = position.z();
        looping = looping1;
        delay = delay1;
        relative = relative1;
    }

    public void setData(float volume1, float pitch1, double x1, double y1, double z1, boolean looping1, int delay1, boolean relative1){
        volume = volume1;
        pitch = pitch1;
        x = x1;
        y = y1;
        z = z1;
        looping = looping1;
        delay = delay1;
        relative = relative1;
    }

    public void setData(float volume1, float pitch1){
        volume = volume1;
        pitch = pitch1;
    }

    public void setPosition(Vector3f position){
        x = position.x();
        y = position.y();
        z = position.z();
    }

    public void setPosition(double x1, double y1, double z1){
        x = x1;
        y = y1;
        z = z1;
    }

    public void setX(double x1){
        x = x1;
    }

    public void setY(double y1){
        y = y1;
    }

    public void setZ(double z1){
        z = z1;
    }

    public void setLooping(boolean looping1){
        looping = looping1;
    }

    public void setDelay(int delay1){
        delay = delay1;
    }

    public void setRelative(boolean relative1){
        relative = relative1;
    }
}