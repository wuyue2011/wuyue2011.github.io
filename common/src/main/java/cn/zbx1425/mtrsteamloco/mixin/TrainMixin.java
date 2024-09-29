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
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Train.class)
public abstract class TrainMixin {

    @Shadow
    protected abstract boolean skipScanBlocks(Level world, double trainX, double trainY, double trainZ);

    @Shadow
    protected abstract boolean openDoors(Level world, Block block, BlockPos checkPos, int dwellTicks);

    @Inject(method = "scanDoors", at = @At("HEAD"), cancellable = true, remap = false)
    private void onScanDoors(Level world, double trainX, double trainY, double trainZ, float checkYaw, float pitch, double halfSpacing, int dwellTicks, CallbackInfoReturnable<Boolean> ci) {
        if (skipScanBlocks(world, trainX, trainY, trainZ)) {
            ci.setReturnValue(false);
			return;
		}

		boolean hasPlatform = false;
		final Vec3 offsetVec = new Vec3(1, 0, 0).yRot(checkYaw).xRot(pitch);
		final Vec3 traverseVec = new Vec3(0, 0, 1).yRot(checkYaw).xRot(pitch);

		for (int checkX = 1; checkX <= 3; checkX++) {
			for (int checkY = -2; checkY <= 3; checkY++) {
				for (double checkZ = -halfSpacing; checkZ <= halfSpacing; checkZ++) {
					final BlockPos checkPos = RailwayData.newBlockPos(trainX + offsetVec.x * checkX + traverseVec.x * checkZ, trainY + checkY, trainZ + offsetVec.z * checkX + traverseVec.z * checkZ);
					final Block block = world.getBlockState(checkPos).getBlock();

					if (block instanceof BlockPlatform || block instanceof BlockPSDAPGBase || block instanceof BlockEyeCandy) {
						if (openDoors(world, block, checkPos, dwellTicks)) {
                            ci.setReturnValue(true);
							return;
						}
						hasPlatform = true;
					}
				}
			}
		}

        ci.setReturnValue(hasPlatform);
		return;
    }
}
