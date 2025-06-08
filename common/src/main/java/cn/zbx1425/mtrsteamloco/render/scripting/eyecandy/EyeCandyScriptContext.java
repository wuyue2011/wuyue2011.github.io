package cn.zbx1425.mtrsteamloco.render.scripting.eyecandy;

import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
import cn.zbx1425.mtrsteamloco.render.scripting.AbstractScriptContext;
import cn.zbx1425.mtrsteamloco.render.scripting.util.client.DynamicModelHolder;
import cn.zbx1425.sowcer.math.Matrices;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcerext.model.ModelCluster;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import cn.zbx1425.sowcerext.reuse.DrawScheduler;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.mtrsteamloco.render.scripting.AbstractDrawCalls.DrawCallMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class EyeCandyScriptContext extends AbstractScriptContext {

    public BlockEyeCandy.BlockEntityEyeCandy entity;

    public EyeCandyDrawCalls scriptResult;
    private EyeCandyDrawCalls scriptResultWriting;

    public DrawCallMap drawCalls = new DrawCallMap();
    
    public EyeCandyScriptContext(BlockEyeCandy.BlockEntityEyeCandy entity) {
        scriptResult = new EyeCandyDrawCalls();
        scriptResultWriting = new EyeCandyDrawCalls();
        this.entity = entity;
    }

    public void commit(DrawScheduler drawScheduler, Matrix4f basePose, Matrix4f worldPose, int light) {
        drawCalls.commit(drawScheduler, basePose, worldPose, light);
    }

    @Override
    public void renderFunctionFinished() {
        synchronized (this) {
            EyeCandyDrawCalls temp = scriptResultWriting;
            scriptResultWriting = scriptResult;
            scriptResult = temp;
            scriptResultWriting.reset();
        }
    }

    @Override
    public Object getWrapperObject() {
        return entity;
    }

    // Something more graceful?
    public boolean disposeForReload = false;

    @Override
    public boolean isBearerAlive() {
        return !disposeForReload && !entity.isRemoved() && !disposed;
    }

    public void drawModel(ModelCluster model, Matrices poseStack) {
        scriptResultWriting.addModel(model, poseStack == null ? Matrix4f.IDENTITY : poseStack.last().copy());
    }

    public void drawModel(DynamicModelHolder model, Matrices poseStack) {
        scriptResultWriting.addModel(model, poseStack == null ? Matrix4f.IDENTITY : poseStack.last().copy());
    }

    public void playSound(ResourceLocation sound, float volume, float pitch) {
        scriptResultWriting.addSound(
#if MC_VERSION >= "11903"
                SoundEvent.createVariableRangeEvent(sound),
#else
                new SoundEvent(sound),
#endif
                volume, pitch
        );
    }
}
