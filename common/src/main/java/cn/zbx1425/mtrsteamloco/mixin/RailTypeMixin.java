package cn.zbx1425.mtrsteamloco.mixin;

import net.minecraft.world.level.material.MaterialColor;
import mtr.data.RailType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RailType.class)
public abstract class RailTypeMixin {
    @Shadow(remap = false) @Final private static RailType[] $VALUES;

    @Invoker(value = "<init>")
    private static RailType create(String name, int ordinal, int speedLimit, MaterialColor MaterialColor, boolean hasSavedRail, boolean canAccelerate, boolean hasSignal, RailType.RailSlopeStyle railSlopeStyle) {
        throw new IllegalStateException();
    }

    static {
        List<RailType> railTypes = new ArrayList<>();
        railTypes.addAll(Arrays.asList($VALUES));
        for (int i = 1; i <= 600; i++) railTypes.add(create("P" + i, railTypes.size(), i, MaterialColor.COLOR_GREEN, false, true, true, RailType.RailSlopeStyle.CURVE));

        for (int i = 1000; i <= 10000; i+= 500) railTypes.add(create("P" + i, railTypes.size(), i, MaterialColor.COLOR_GREEN, false, true, true, RailType.RailSlopeStyle.CURVE));

        $VALUES = railTypes.toArray(new RailType[0]);
    }
}