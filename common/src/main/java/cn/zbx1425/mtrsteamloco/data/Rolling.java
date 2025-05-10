package cn.zbx1425.mtrsteamloco.data;

import net.minecraft.client.Minecraft;
import cn.zbx1425.sowcer.math.PoseStackUtil;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcer.math.Matrix4f;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import cn.zbx1425.mtrsteamloco.ClientConfig;
#if MC_VERSION >= "11903"
import org.joml.Quaternionf;
import org.joml.AxisAngle4d;
#else
import com.mojang.math.Quaternion;
#endif

public class Rolling {
    private static Rotation rotation = Rotation.IDENTITY;
    private static Rotation tempRotation = null;

    public static void setRollMatrix(double x, double y, double z, float yaw, float pitch, float roll, boolean reversed) {
        tempRotation = new Rotation(x, y, z, yaw, pitch, roll, reversed);
    }

    public static void update() {
        if (tempRotation != null) {
            rotation = tempRotation;
            tempRotation = null;
        } else {
            rotation = Rotation.IDENTITY;
        }
    }

    public static void applyRolling(PoseStack poseStack) {
        // if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;
        if (!ClientConfig.enableRolling) return;
        if (rotation.isIdentity()) return;
        
        poseStack.mulPose(getRollQuaternion(true));
    }

    public static Vector3f applyRolling(Vector3f pos, float eyeHeight) {
        if (rotation.isIdentity()) return pos;

        pos = pos.copy();
        Rotation rot = rotation;


        Matrix4f mat = new Matrix4f();
        mat.rotateZ(rot.reversed? -rot.roll : rot.roll);
        mat.rotateY(rot.yaw);
        mat.rotateX(rot.pitch);

        pos.add(0, -eyeHeight, 0);
        Vector3f ep = mat.transform(new Vector3f(0, eyeHeight, 0));
        pos.add(ep);
        return pos;
    }



#if MC_VERSION >= "11903"
    public static Quaternionf getRollQuaternion() {
        return getRollQuaternion(false);
    }

    public static Quaternionf getRollQuaternion(boolean reversed) {
        if (rotation.isIdentity()) return new Quaternionf();

        Rotation rot = rotation;

        Matrix4f mat = new Matrix4f();
        mat.rotateX(rot.pitch);
        mat.rotateY(rot.yaw);
        Vector3f fp = mat.transform(new Vector3f(0, 0, 1));
        Quaternionf q = new Quaternionf(new AxisAngle4d(reversed != rot.reversed ? rot.roll : -rot.roll, fp.x(), fp.y(), fp.z()));

        return q;
    }
#else

    public static Quaternion getRollQuaternion() {
        return getRollQuaternion(false);
    }

    public static Quaternion getRollQuaternion(boolean reversed) {
        if (rotation.isIdentity()) return Quaternion.ONE.copy();
        Rotation rot = rotation;

        Matrix4f mat = new Matrix4f();
        mat.rotateX(rot.pitch);
        mat.rotateY(rot.yaw);
        Vector3f fp = mat.transform(new Vector3f(0, 0, 1));
        Quaternion q = new Quaternion(fp.asMoj(), reversed != rot.reversed ? rot.roll : -rot.roll, false);

        return q;
    }
#endif

    private static class Rotation {
        public final Vector3f pos;
        public final float yaw;
        public final float pitch;
        public final float roll;
        public final boolean reversed;

        public static final Rotation IDENTITY = new Rotation(0, 0, 0, 0, 0, 0, false);

        public Rotation(double x, double y, double z, float yaw, float pitch, float roll, boolean reversed) {
            this.pos = new Vector3f(x, y, z);
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
            this.reversed = reversed;
        }

        public boolean isIdentity() {
            return yaw == 0 && pitch == 0 && roll == 0;
        }

        public Rotation copy() {
            return new Rotation(pos.x(), pos.y(), pos.z(), yaw, pitch, roll, reversed);
        }
    }
}