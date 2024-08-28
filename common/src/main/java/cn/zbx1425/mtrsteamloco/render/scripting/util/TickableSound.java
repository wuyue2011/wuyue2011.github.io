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

    public void setVolume(float volume){
        super.volume = volume;
    }

    public void setPitch(float pitch){
        super.pitch = pitch;
    }

    public void play(){
        Minecraft.getInstance().getSoundManager().play(this);
    }

    public void setData(float volume, float pitch, Vector3f position, boolean looping, int delay, boolean relative){
        super.volume = volume;
        super.pitch = pitch;
        super.x = position.x();
        super.y = position.y();
        super.z = position.z();
        super.looping = looping;
        super.delay = delay;
        super.relative = relative;
    }

    public void setData(float volume, float pitch, double x, double y, double z, boolean looping, int delay, boolean relative){
        super.volume = volume;
        super.pitch = pitch;
        super.x = x;
        super.y = y;
        super.z = z;
        super.looping = looping;
        super.delay = delay;
        super.relative = relative;
    }

    public void setData(float volume, float pitch){
        super.volume = volume;
        super.pitch = pitch;
    }

    public void setPosition(Vector3f position){
        super.x = position.x();
        super.y = position.y();
        super.z = position.z();
    }

    public void setPosition(double x, double y, double z){
        super.x = x;
        super.y = y;
        super.z = z;
    }

    public void setX(double x){
        super.x = x;
    }

    public void setY(double y){
        super.y = y;
    }

    public void setZ(double z){
        super.z = z;
    }

    public void setLooping(boolean looping){
        super.looping = looping;
    }

    public void setDelay(int delay){
        super.delay = delay;
    }

    public void setRelative(boolean relative){
        super.relative = relative;
    }

    public void stop(){
        super.stop();
    }
}