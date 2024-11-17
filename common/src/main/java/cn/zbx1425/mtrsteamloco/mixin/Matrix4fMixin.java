#if MC_VERSION < "11903"
package cn.zbx1425.mtrsteamloco.mixin;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Matrix4f.class)
public  class Matrix4fMixin {

    @Shadow protected float m00;
    @Shadow protected float m01;
    @Shadow protected float m02;
    @Shadow protected float m03;
    @Shadow protected float m10;
    @Shadow protected float m11;
    @Shadow protected float m12;
    @Shadow protected float m13;
    @Shadow protected float m20;
    @Shadow protected float m21;
    @Shadow protected float m22;
    @Shadow protected float m23;
    @Shadow protected float m30;
    @Shadow protected float m31;
    @Shadow protected float m32;
    @Shadow protected float m33;

    public Vector3f getRotationAngles() {
        float[] angles = new float[3];
        angles[0] = (float) Math.atan2(m32, m22); // pitch
        angles[1] = (float) Math.atan2(-m20, Math.sqrt(m21 * m21 + m22 * m22)); // yaw
        angles[2] = (float) Math.atan2(m10, m00); // roll

        // 如果需要将弧度转换为度
        for (int i = 0; i < 3; i++) {
            angles[i] = (float) Math.toDegrees(angles[i]);
        }
        
        return new Vector3f(angles[0], angles[1], angles[2]);
    }
}

#endif