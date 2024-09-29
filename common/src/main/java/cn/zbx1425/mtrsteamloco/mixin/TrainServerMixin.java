package cn.zbx1425.mtrsteamloco.mixin;

import mtr.data.TrainServer;
import mtr.data.Train;
import mtr.data.RailwayData;
import mtr.block.BlockPSDAPGBase;
import mtr.block.BlockPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import mtr.path.PathData;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrainServer.class)
public abstract class TrainServerMixin extends Train {

    public TrainServerMixin(long id, long sidingId, float railLength, String trainId, String baseTrainType, int trainCars, List<PathData> path, List<Double> distances, int repeatIndex1, int repeatIndex2, float accelerationConstant, boolean isManualAllowed, int maxManualSpeed, int manualToAutomaticTime) {
        super(id, sidingId, railLength, trainId, baseTrainType, trainCars, path, distances, repeatIndex1, repeatIndex2, accelerationConstant, isManualAllowed, maxManualSpeed, manualToAutomaticTime);
    }

    @Inject(method = "openDoors", at = @At("HEAD"), remap = false)
    protected void onOpenDoors(Level world, Block block, BlockPos checkPos, int dwellTicks, CallbackInfo ci) {
        if (block instanceof BlockEyeCandy) {
			BlockEntity entity = world.getBlockEntity(checkPos);
			final int doorStateValue = (int) Mth.clamp(doorValue * DOOR_MOVE_TIME, 0, 1);
			((BlockEyeCandy.BlockEntityEyeCandy) entity).setOpen(doorStateValue);
			((BlockEyeCandy.BlockEntityEyeCandy) entity).getData().put("setOpen", doorStateValue + "");
			((BlockEyeCandy.BlockEntityEyeCandy) entity).openClient = doorValue;
            ((BlockEyeCandy.BlockEntityEyeCandy) entity).sendUpdateC2S();
		}
		return;
    }
}