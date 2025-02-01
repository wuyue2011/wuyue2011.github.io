package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.render.train.NoopTrainRenderer;
import cn.zbx1425.mtrsteamloco.sound.NoopTrainSound;
import mtr.data.TrainClient;
import net.minecraft.network.FriendlyByteBuf;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import mtr.data.Train;
import mtr.path.PathData;
import cn.zbx1425.mtrsteamloco.data.TrainCustomConfigsSupplier;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrainClient.class)
public abstract class TrainClientMixin{

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void initTail(FriendlyByteBuf packet, CallbackInfo ci) {
        if (!ClientConfig.enableTrainRender) ((TrainClientAccessor) this).setTrainRenderer(NoopTrainRenderer.INSTANCE);
        if (!ClientConfig.enableTrainSound) ((TrainClientAccessor) this).setTrainSound(NoopTrainSound.INSTANCE);
    }

    @Inject(method = "copyFromTrain", at = @At("TAIL"), remap = false)
    private void copyFromTrainTail(Train other, CallbackInfo ci) {
        ((TrainCustomConfigsSupplier) (Object) this).setCustomConfigs(((TrainCustomConfigsSupplier) (Object) other).getCustomConfigs());
    }
}
