package cn.zbx1425.mtrsteamloco.render.scripting.rail;

import cn.zbx1425.sowcerext.reuse.DrawScheduler;
import cn.zbx1425.mtrsteamloco.render.scripting.AbstractDrawCalls;
import cn.zbx1425.sowcer.math.Matrix4f;
import net.minecraft.world.level.Level;
import cn.zbx1425.sowcer.math.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.mtrsteamloco.render.scripting.util.DynamicModelHolder;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.AABB;

public interface RailDrawCalls {
    public static interface RailDrawCall {
        public void commit(DrawScheduler drawScheduler, Matrix4f world);
        public float getDistanceFrom(Vector3f src);
        public boolean isVisible(Frustum frustum);
        default public void commit(DrawScheduler drawScheduler, Matrix4f world, Frustum frustum, Vector3f cameraPos, int maxRailDistance) {
            if (getDistanceFrom(cameraPos) <= maxRailDistance && isVisible(frustum)) {
                commit(drawScheduler, world);
            }
        }
    }

    public static class SimpleRailDrawCall implements RailDrawCall {
        private final Matrix4f matrix;
        private final ModelCluster model;
        private final DynamicModelHolder holder;
        private final Vector3f local;
        private final BlockPos pos;
        private final AABB boundingBox;

        public SimpleRailDrawCall(ModelCluster model, Matrix4f matrix) {
            this.matrix = matrix.copy();
            this.model = model;
            this.holder = null;
            local = matrix.getTranslationPart();
            pos = local.toBlockPos();
            boundingBox = new AABB(local.x() - 0.5, local.y() - 0.5, local.z() - 0.5, local.x() + 0.5, local.y() + 0.5, local.z() + 0.5);
        }

        public SimpleRailDrawCall(DynamicModelHolder holder, Matrix4f matrix) {
            this.matrix = matrix.copy();
            this.model = null;
            this.holder = holder;
            local = matrix.getTranslationPart();
            pos = local.toBlockPos();
            boundingBox = new AABB(local.x() - 0.5, local.y() - 0.5, local.z() - 0.5, local.x() + 0.5, local.y() + 0.5, local.z() + 0.5);
        }

        @Override
        public void commit(DrawScheduler drawScheduler, Matrix4f world) {
            Matrix4f mat = world.copy();
            mat.mul(matrix);
            int light = LevelRenderer.getLightColor(Minecraft.getInstance().level, pos);
            drawScheduler.enqueue(model == null? holder.getUploadedModel() : model, mat, light);
        }

        @Override
        public float getDistanceFrom(Vector3f src) {
            return src.distance(local);
        }

        @Override
        public boolean isVisible(Frustum frustum) {
            return frustum.isVisible(boundingBox);
        }
    }
}