package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.mtrsteamloco.render.scripting.util.DynamicModelHolder;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.reuse.DrawScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public abstract class AbstractDrawCalls {

    public static abstract class DrawCall {
        public ModelCluster model;
        public DynamicModelHolder modelHolder;
        public Matrix4f pose;

        public DrawCall(ModelCluster model, Matrix4f pose) {
            this.model = model;
            this.pose = pose;
        }

        public DrawCall(DynamicModelHolder model, Matrix4f pose) {
            this.modelHolder = model;
            this.pose = pose;
        }

        public abstract void commit(DrawScheduler drawScheduler, Matrix4f basePose, Matrix4f worldPose, int light);
    }

    public static class ClusterDrawCall extends DrawCall {

        public ClusterDrawCall(ModelCluster model, Matrix4f pose) {
            super(model, pose);
        }

        public ClusterDrawCall(DynamicModelHolder model, Matrix4f pose) {
            super(model, pose);
        }

        @Override
        public void commit(DrawScheduler drawScheduler, Matrix4f basePose, Matrix4f worldPose, int light) {
            Matrix4f finalPose = basePose.copy();
            finalPose.multiply(pose);
            if (model != null) {
                drawScheduler.enqueue(model, finalPose, light);
            } else {
                ModelCluster model = modelHolder.getUploadedModel();
                if (model != null) {
                    drawScheduler.enqueue(model, finalPose, light);
                }
            }
        }
    }

    public static class WorldDrawCall extends DrawCall {
        public WorldDrawCall(ModelCluster model, Matrix4f pose) {
            super(model, pose);
        }

        public WorldDrawCall(DynamicModelHolder model, Matrix4f pose) {
            super(model, pose);
        }

        @Override
        public void commit(DrawScheduler drawScheduler, Matrix4f basePose, Matrix4f worldPose, int light) {
            Matrix4f finalPose = worldPose.copy();
            finalPose.multiply(pose);
            if (model != null) {
                drawScheduler.enqueue(model, finalPose, light);
            } else {
                ModelCluster model = modelHolder.getUploadedModel();
                if (model != null) {
                    drawScheduler.enqueue(model, finalPose, light);
                }
            }
        }
    }

    public static class PlaySoundCall {
        public SoundEvent sound;
        public Vector3f position;
        public float volume;
        public float pitch;

        public PlaySoundCall(SoundEvent sound, Vector3f position, float volume, float pitch) {
            this.sound = sound;
            this.position = position;
            this.volume = volume;
            this.pitch = pitch;
        }

        public void commit(ClientLevel level, Matrix4f worldPose) {
            Vector3f worldPos = worldPose.transform(position);
            level.playLocalSound(worldPos.x(), worldPos.y(), worldPos.z(),
                    sound, SoundSource.BLOCKS,
                    volume, pitch, false);
        }
    }
}
