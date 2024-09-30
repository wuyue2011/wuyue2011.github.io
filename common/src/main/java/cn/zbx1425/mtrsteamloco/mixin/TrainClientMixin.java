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
import mtr.data.train;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrainClient.class)
public abstract class TrainClientMixin extends Train{

    public TrainClientMixin(long id, long sidingId, float railLength, String trainId, String baseTrainType, int trainCars, List<PathData> path, List<Double> distances, int repeatIndex1, int repeatIndex2, float accelerationConstant, boolean isManualAllowed, int maxManualSpeed, int manualToAutomaticTime) {
        super(id, sidingId, railLength, trainId, baseTrainType, trainCars, path, distances, repeatIndex1, repeatIndex2, accelerationConstant, isManualAllowed, maxManualSpeed, manualToAutomaticTime);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void initTail(FriendlyByteBuf packet, CallbackInfo ci) {
        if (!ClientConfig.enableTrainRender) ((TrainClientAccessor)this).setTrainRenderer(NoopTrainRenderer.INSTANCE);
        if (!ClientConfig.enableTrainSound) ((TrainClientAccessor)this).setTrainSound(NoopTrainSound.INSTANCE);
    }

    @Inject(method = "openDoors", at = @At("HEAD"), remap = false)
    protected boolean onOpenDoors(Level world, Block block, BlockPos checkPos, int dwellTicks, CallbackInfoReturnable<Boolean> ci) {
        if (block instanceof BlockEyeCandy) {
            final BlockEntity entity = world.getBlockEntity(block);
            ((BlockEyeCandy.BlockEntityEyeCandy) entity).setDoorValue(doorValue);
        }
        ci.setReturnValue(false);
        return;
    }
}
