package cn.zbx1425.mtrsteamloco.mixin;

import mtr.data.RailAngle;
import cn.zbx1425.mtrsteamloco.data.RailAngleExtra;
import cn.zbx1425.mtrsteamloco.Main;

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
public abstract class RailAngleMixin implements RailAngleExtra{
    @Shadow(remap = false) @Final private float angleDegrees;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void init(String name, int ordinal, float angleDegrees, CallbackInfo ci) {
        // Main.LOGGER.info("RailAngle created: " + name + " " + this.angleDegrees);
    }

    @Invoker(value = "<init>")
    private static RailAngle create(String name, int ordinal, float angleDegrees) {
        throw new IllegalStateException();
    }

    @Override
    public RailAngle _fromDegrees(float angleDegrees) {
        RailAngle result = create("A" + String.format("%.2f", angleDegrees), -1, angleDegrees);
        return result;
    }

    private RailAngle fromDegrees(float angleDegrees) {
        return _fromDegrees(angleDegrees);
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
        float f3 = Math.abs(f1 - f2) % 180;
        boolean result = f3 < 0.001;
        // Main.LOGGER.info("isParallel: " + f1 + " " + f2 + " " + f3 + result);
        return result;
    }
}