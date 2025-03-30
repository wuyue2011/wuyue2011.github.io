package cn.zbx1425.mtrsteamloco.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import mtr.screen.SidingScreen;
import mtr.data.RailType;

@Mixin(SidingScreen.class)
public abstract class SidingScreenMixin {
    
    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lmtr/data/RailType;ordinal()I",
            remap = false
        ),
        remap = false
    )
    private int modifySliderMaxValue(RailType railType) {
        return RailType.valueOf("P10000").ordinal();
    }
}