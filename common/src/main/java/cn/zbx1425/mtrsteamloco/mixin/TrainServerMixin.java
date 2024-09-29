package cn.zbx1425.mtrsteamloco.mixin;

import mtr.data.TrainServer;
import mtr.data.Train;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrainServer.class)
public abstract class TrainServerMixin extends Train {
    @Inject(method = "openDoors", at = @At("HEAD"), remap = false)
    protected void onOpenDoors(Level world, Block block, BlockPos checkPos, int dwellTicks, CallbackInfo ci) {
        if (block instanceof BlockEyeCandy) {
			BlockEntity entity = world.getBlockEntity(checkPos);
			final int doorStateValue = (int) Mth.clamp(doorValue * DOOR_MOVE_TIME, 0, 1);
			((BlockEyeCandy.BlockEntityEyeCandy) entity).setOpen(doorStateValue);
		}
    }
}