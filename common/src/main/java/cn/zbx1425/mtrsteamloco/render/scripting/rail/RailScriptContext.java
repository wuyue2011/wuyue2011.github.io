package cn.zbx1425.mtrsteamloco.render.scripting.rail;

import cn.zbx1425.mtrsteamloco.render.scripting.AbstractScriptContext;
import mtr.data.Rail;
import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.NativeObject;
import net.minecraft.client.renderer.culling.Frustum;
import cn.zbx1425.sowcerext.reuse.DrawScheduler;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcer.math.Matrices;
import cn.zbx1425.mtrsteamloco.render.rail.RailChunkBase;
import cn.zbx1425.mtrsteamloco.render.rail.BakedRail;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collection;

public class RailScriptContext extends AbstractScriptContext {

    public final BakedRail bakedRail;
    public final Map<Object, RailChunkBase> chunks = new HashMap<>();
    public boolean living = true;

    public RailScriptContext(BakedRail bakedRail) {
        this.bakedRail = bakedRail;
    }

    @Override
    public void renderFunctionFinished() {
        // 不会每次都渲染的 所以不用管
        // 动态逻辑需要自己实现 RailChunkBase
    }

    @Override
    public Object getWrapperObject() {
        return bakedRail;
    }

    public void dispose() {
        living = false;
    }

    @Override
    public boolean isBearerAlive() {
        return living && !disposed;
    }

    public void addChunk(Object key, RailChunkBase chunk) {
        chunks.put(key, chunk);
    }
}