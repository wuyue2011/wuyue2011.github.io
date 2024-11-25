package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.Main;
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
import cn.zbx1425.sowcer.math.Vector3f;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.server.level.ServerLevel;

@Mixin(Train.class)
public abstract class TrainMixin {

    protected abstract boolean skipScanBlocks(Level world, double trainX, double trainY, double trainZ);

    protected abstract boolean openDoors(Level world, Block block, BlockPos checkPos, int dwellTicks);

	protected abstract boolean scanDoors(Level world, double trainX, double trainY, double trainZ, float checkYaw, float pitch, double halfSpacing, int dwellTicks);

	protected abstract double asin(double value);

	protected float doorValue;

	protected boolean doorTarget;

	public static double getAverage(double a, double b) {
		return (a + b) / 2;
	}

    @Inject(method = "scanDoors", at = @At("HEAD"), cancellable = true, remap = false)
    private void onScanDoors(Level world, double trainX, double trainY, double trainZ, float checkYaw, float pitch, double halfSpacing, int dwellTicks, CallbackInfoReturnable<Boolean> ci) {
        if (skipScanBlocks(world, trainX, trainY, trainZ)) {
            ci.setReturnValue(false);
			return;
		}

		boolean hasPlatform = false;
		final Vec3 offsetVec = new Vec3(1, 0, 0).yRot(checkYaw).xRot(pitch);
		final Vec3 traverseVec = new Vec3(0, 0, 1).yRot(checkYaw).xRot(pitch);
		Set<String> OKPos = new HashSet<>();
		for (int checkX = 1; checkX <= 3; checkX++) {
			for (int checkY = -2; checkY <= 3; checkY++) {
				for (double checkZ = -halfSpacing; checkZ <= halfSpacing; checkZ++) {
					final BlockPos checkPos = RailwayData.newBlockPos(trainX + offsetVec.x * checkX + traverseVec.x * checkZ, trainY + checkY, trainZ + offsetVec.z * checkX + traverseVec.z * checkZ);
					final Block block = world.getBlockState(checkPos).getBlock();

					if (block instanceof BlockPlatform || block instanceof BlockPSDAPGBase) {
						if (openDoors(world, block, checkPos, dwellTicks)) {
                            ci.setReturnValue(true);
							return;
						}
						hasPlatform = true;
					}else if (block instanceof BlockEyeCandy) {
						if (OKPos.contains(checkPos.toString())) continue;
						int[] dir = new int[]{1, -1};
						int[] f = new int[]{1, 0, 0, 1, 0, 0};
						for (int i = 0; i < 3; i++) {
							for (int j = 0; j < 2; j++) {
								for (int k = 1; k <= 20; k++) {
									int v = dir[j] * k;
									BlockPos pos = checkPos.offset(f[i] * v, f[i + 1] * v, f[i + 2] * v);
									if (OKPos.contains(pos.toString())) break;
									final BlockEntity entity = world.getBlockEntity(pos);
									if (entity instanceof BlockEyeCandy.BlockEntityEyeCandy) {
										OKPos.add(pos.toString());
										BlockEyeCandy.BlockEntityEyeCandy e = (BlockEyeCandy.BlockEntityEyeCandy) entity;
										if (!world.isClientSide()) {
											e.setDoorValue(doorValue);
											e.setDoorTarget(doorTarget);
											e.setChanged();
                    						((ServerLevel) world).getChunkSource().blockChanged(pos);
										}
										if (e.isPlatform()) {
											if (openDoors(world, block, pos, dwellTicks)) {
												ci.setReturnValue(true);
												return;
											}
											hasPlatform = true;
										} else break;
									} else break;
								}
							}
						}
						
					}
				}
			}
		}
		OKPos.clear();
        ci.setReturnValue(hasPlatform);
    }

	public boolean[] canOpenDoorsAt(Level world, Vector3f p1, Vector3f p2) {
		final Vec3 pos1 = p1.toVec3();
		final Vec3 pos2 = p2.toVec3();
		final int dwellTicks = 114514;

		final double x = getAverage(pos1.x, pos2.x);
		final double y = getAverage(pos1.y, pos2.y) + 1;
		final double z = getAverage(pos1.z, pos2.z);

		final double realSpacing = pos2.distanceTo(pos1);
		final float yaw = (float) Mth.atan2(pos2.x - pos1.x, pos2.z - pos1.z);
		final float pitch = realSpacing == 0 ? 0 : (float) asin((pos2.y - pos1.y) / realSpacing);
		final boolean doorLeftOpen = scanDoors(world, x, y, z, (float) Math.PI + yaw, pitch, realSpacing / 2, dwellTicks);
		final boolean doorRightOpen = scanDoors(world, x, y, z, yaw, pitch, realSpacing / 2, dwellTicks);
		return new boolean[]{doorLeftOpen, doorRightOpen};
	}
}
