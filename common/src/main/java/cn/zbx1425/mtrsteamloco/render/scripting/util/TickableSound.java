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
        this.volume = volume;
    }

    public void setPitch(float pitch){
        this.pitch = pitch;
    }

    public void play(){
        Minecraft.getInstance().getSoundManager().play(this);
    }

    public void setData(float volume, float pitch, Vector3f position, boolean looping, int delay, boolean relative){
        this.volume = volume;
        this.pitch = pitch;
        this.x = position.x();
        this.y = position.y();
        this.z = position.z();
        this.looping = looping;
        this.delay = delay;
        this.relative = relative;
    }

    public void setData(float volume, float pitch, double x, double y, double z, boolean looping, int delay, boolean relative){
        this.volume = volume;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.looping = looping;
        this.delay = delay;
        this.relative = relative;
    }

    public void setData(float volume, float pitch){
        this.volume = volume;
        this.pitch = pitch;
    }

    public void setPosition(Vector3f position){
        this.x = position.x();
        this.y = position.y();
        this.z = position.z();
    }

    public void setPosition(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setX(double x){
        this.x = x;
    }

    public void setY(double y){
        this.y = y;
    }

    public void setZ(double z){
        this.z = z;
    }

    public void setLooping(boolean looping){
        this.looping = looping;
    }

    public void setDelay(int delay){
        this.delay = delay;
    }

    public void setRelative(boolean relative){
        this.relative = relative;
    }

    public void stop(){
        stop();
    }
}