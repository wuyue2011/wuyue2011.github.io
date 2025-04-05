package cn.zbx1425.mtrsteamloco.mixin;

import mtr.data.RailAngle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RailAngle.class)
public abstract class RailAngleMixin {
    @Shadow @Final private float angleDegrees;

    @Invoker(value = "<init>")
    private static RailAngle create(String name, int ordinal, float angleDegrees) {
        throw new IllegalStateException();
    }

    public static RailAngle fromDegrees(float angleDegrees) {
        angleDegrees = (angleDegrees + 360) % 360;
        return create("unnamed", -1, angleDegrees);
    }

    public RailAngle getOpposite() {
        return fromDegrees(angleDegrees + 180);
    }

    public RailAngle add(RailAngle other) {
        return fromDegrees(angleDegrees + other.angleDegrees);
    }

    public RailAngle subtract(RailAngle other) {
        return fromDegrees(angleDegrees - other.angleDegrees);
    }

    public boolean isParallel(RailAngle other) {
        float f1 = other.angleDegrees, f2 = angleDegrees;
        return Math.abs((f1 - f2) % 180) < 0.001;
    }
}