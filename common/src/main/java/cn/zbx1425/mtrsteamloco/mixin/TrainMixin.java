package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.Main;
import mtr.data.TrainServer;
import mtr.data.Train;
import mtr.data.RailwayData;
import mtr.data.MessagePackHelper;
import mtr.block.BlockPSDAPGBase;
import mtr.block.BlockPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import mtr.path.PathData;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.mtrsteamloco.data.TrainExtraSupplier;
import org.msgpack.core.MessagePacker;
import cn.zbx1425.mtrsteamloco.network.util.StringMapSerializer;
import org.msgpack.value.Value;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import cn.zbx1425.mtrsteamloco.Main;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Train.class)
public abstract class TrainMixin implements TrainExtraSupplier{

    private Map<String, String> extraData = new HashMap<>();
	
	@Override
	public Map<String, String> getExtraData() {
		return extraData;
	}

	@Override
	public void setExtraData(Map<String, String> extraData) {
		this.extraData = extraData;
	}

	@Inject(method = "<init>(JFLjava/util/List;Ljava/util/List;IIFZIILjava/util/Map;)V", at = @At("TAIL"), remap = false)
	private void fromMassagePack(
			long sidingId, float railLength,
			List<PathData> path, List<Double> distances, int repeatIndex1, int repeatIndex2,
			float accelerationConstant, boolean isManualAllowed, int maxManualSpeed, int manualToAutomaticTime,
			Map<String, Value> map, CallbackInfo ci
	) {
		print("Mixin-Train InitFromMassagePack" + map.toString());
		MessagePackHelper messagePackHelper = new MessagePackHelper(map);
		try {
			extraData = StringMapSerializer.deserialize(messagePackHelper.getString("extra_data"));
		} catch (IOException e) {
			extraData = new HashMap<>();
		}
	}

	@Inject(method = "<init>(JFLjava/util/List;Ljava/util/List;IIFZIILnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
	private void fromCompoundTag(
			long sidingId, float railLength,
			List<PathData> path, List<Double> distances, int repeatIndex1, int repeatIndex2,
			float accelerationConstant, boolean isManualAllowed, int maxManualSpeed, int manualToAutomaticTime,
			CompoundTag compoundTag, CallbackInfo ci
	) {
		print("Mixin-Train InitFromCompoundTag" + compoundTag.toString());
		try {
			extraData = StringMapSerializer.deserialize(compoundTag.getString("extra_data"));
		} catch (IOException e) {
			extraData = new HashMap<>();
		}
	}

	@Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("TAIL"))
	private void fromFriendlyByteBuf(FriendlyByteBuf buffer, CallbackInfo ci) {
		print ("Mixin-Train InitFromFriendlyByteBuf" + buffer.toString());
		try {
			extraData = StringMapSerializer.deserialize(buffer.readUtf());
		} catch (IOException e) {
			extraData = new HashMap<>();
		}
	}

	@Inject(method = "toMessagePack", at = @At("TAIL"), remap = false)
    private void toMessagePack(MessagePacker messagePacker, CallbackInfo ci) throws IOException {
		String res;
		try {
			res = StringMapSerializer.serializeToString(extraData);
		} catch (IOException e) {
			res = "";
		}
		messagePacker.packString("extra_data").packString(res);
		print ("Mixin-Train ToMessagePack" + res);
	}

    @Inject(method = "messagePackLength", at = @At("TAIL"), cancellable = true, remap = false)
    private void messagePackLength(CallbackInfoReturnable<Integer> cir) {
		print ("Mixin-Train MessagePackLength" + cir.getReturnValue() + 1);
        cir.setReturnValue(cir.getReturnValue() + 1);
    }

	@Inject(method = "writePacket", at = @At("TAIL"))
    private void toPacket(FriendlyByteBuf packet, CallbackInfo ci) {
		String res;
		try {
			res = StringMapSerializer.serializeToString(extraData);
		} catch (IOException e) {
			res = "";
		}
		print ("Mixin-Train ToPacket" + res);
		packet.writeUtf(res);
	}

	private static void print(String s) {
		Main.LOGGER.info(s);
	}

	protected abstract boolean skipScanBlocks(Level world, double trainX, double trainY, double trainZ);

    protected abstract boolean openDoors(Level world, Block block, BlockPos checkPos, int dwellTicks);

	protected float doorValue;

	protected boolean doorTarget;

    @Inject(method = "scanDoors", at = @At("HEAD"), cancellable = true)
    private void onScanDoors(Level world, double trainX, double trainY, double trainZ, float checkYaw, float pitch, double halfSpacing, int dwellTicks, CallbackInfoReturnable<Boolean> ci) {
        if (skipScanBlocks(world, trainX, trainY, trainZ)) {
            ci.setReturnValue(false);
			return;
		}

		boolean hasPlatform = false;
		boolean isClientSide = world.isClientSide();
		final Vec3 offsetVec = new Vec3(1, 0, 0).yRot(checkYaw).xRot(pitch);
		final Vec3 traverseVec = new Vec3(0, 0, 1).yRot(checkYaw).xRot(pitch);
		Set<BlockPos> OKPos = new HashSet<>();
		for (int checkX = 1; checkX <= 3; checkX++) {
			for (int checkY = -2; checkY <= 3; checkY++) {
				for (double checkZ = -halfSpacing; checkZ <= halfSpacing; checkZ++) {
					final BlockPos checkPos = RailwayData.newBlockPos(trainX + offsetVec.x * checkX + traverseVec.x * checkZ, trainY + checkY, trainZ + offsetVec.z * checkX + traverseVec.z * checkZ);
					final Block block = world.getBlockState(checkPos).getBlock();

					if (block instanceof BlockPlatform || block instanceof BlockPSDAPGBase) {
						openDoors(world, block, checkPos, dwellTicks);
						hasPlatform = true;
					}else if (block instanceof BlockEyeCandy) {
						if (OKPos.contains(checkPos)) continue;
						int[] dir = new int[]{1, -1};
						int[] f = new int[]{1, 0, 0, 1, 0, 0};
						if (checkEyeCandy(world, checkPos, isClientSide)) hasPlatform = true;
						for (int i = 0; i < 3; i++) {
							for (int j = 0; j < 2; j++) {
								for (int k = 1; k <= 40; k++) {
									int v = dir[j] * k;
									BlockPos pos = checkPos.offset(f[i] * v, f[i + 1] * v, f[i + 2] * v);
									if (OKPos.contains(pos)) break;
									OKPos.add(pos);
									if (checkEyeCandy(world, pos, isClientSide)) hasPlatform = true;
									else break;
								}
							}
						}
						
					}
				}
			}
		}
        ci.setReturnValue(hasPlatform);
		return;
    }

	private boolean checkEyeCandy(Level world, BlockPos pos, boolean isClientSide) {
		final BlockEntity entity = world.getBlockEntity(pos);
		if (entity instanceof BlockEyeCandy.BlockEntityEyeCandy) {
			BlockEyeCandy.BlockEntityEyeCandy e = (BlockEyeCandy.BlockEntityEyeCandy) entity;
			if (e.isPlatform()) {
				if (isClientSide) {
					e.setDoorTarget(doorTarget);
					e.setDoorValue(doorValue);
				}
				return true;
			} else return false;
		} else {
			return false;
		}
	}
}
