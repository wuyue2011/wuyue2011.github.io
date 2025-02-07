package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.sowcer.math.Matrix4f;
import net.minecraft.world.phys.Vec3;
import cn.zbx1425.mtrsteamloco.data.VehicleRidingClientExtraSupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import mtr.data.*;
import mtr.data.*;
import mtr.client.*;
import mtr.*;
import mtr.packet.PacketTrainDataGuiClient;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import mtr.mappings.Utilities;
import mtr.entity.EntitySeat;

import java.util.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VehicleRidingClient.class)
public abstract class VehicleRidingClientMixin implements VehicleRidingClientExtraSupplier {

    private Float roll = null;

    @Override
    public float getRoll() {
        float p;
        if (roll == null) return 0;
        else {
            p = roll;
            roll = null;
            return p;
        }
    }

    @Override
    public void setRoll(float roll) {
        this.roll = roll;
    }

	@Shadow(remap = false) private float clientPrevYaw;
	@Shadow(remap = false) private float oldPercentageX;
	@Shadow(remap = false) private float oldPercentageZ;
	@Shadow(remap = false) private double lastSentX;
	@Shadow(remap = false) private double lastSentY;
	@Shadow(remap = false) private double lastSentZ;
	@Shadow(remap = false) private float lastSentTicks;
	@Shadow(remap = false) private int interval;
	@Shadow(remap = false) private int previousInterval;

	@Shadow(remap = false) private List<Double> offset;
	@Shadow(remap = false) private Map<UUID, Float> percentagesX;
	@Shadow(remap = false) private Map<UUID, Float> percentagesZ;
	@Shadow(remap = false) private Map<UUID, Float> newPercentagesX;
	@Shadow(remap = false) private Map<UUID, Float> newPercentagesZ;
	@Shadow(remap = false) private Map<UUID, Vec3> riderPositions;
	@Shadow(remap = false) private Set<UUID> ridingEntities;
	@Shadow(remap = false) private ResourceLocation packetId;

	private static double getValueFromPercentage(double percentage, double total) {
		return (percentage - 0.5) * total;
	}

    public void setOffsets(UUID uuid, double x, double y, double z, float yaw, float pitch, double length, int width, boolean doorLeftOpen, boolean doorRightOpen, boolean hasPitchAscending, boolean hasPitchDescending, float riderOffset, float riderOffsetDismounting, boolean shouldSetOffset, boolean shouldSetYaw, Runnable clientPlayerCallback) {
        final LocalPlayer clientPlayer = Minecraft.getInstance().player;
		if (clientPlayer == null) {
			return;
		}

		final boolean isClientPlayer = uuid.equals(clientPlayer.getUUID());
		final double percentageX = getValueFromPercentage(percentagesX.get(uuid), width);
		final float riderOffsetNew = doorLeftOpen && percentageX < 0 || doorRightOpen && percentageX > 1 ? riderOffsetDismounting : riderOffset;

        Matrix4f m0 = new Matrix4f();
        m0.rotateY((pitch < 0 ? hasPitchAscending : hasPitchDescending) ? pitch : 0);
        m0.rotateX(yaw);
        m0.translate(0, -1, 0);
        m0.rotateZ(getRoll());
        m0.translate(0, 1, 0);
        m0.translate((float) percentageX, riderOffsetNew, (float) getValueFromPercentage(Mth.frac(percentagesZ.get(uuid)), length));

        final Vec3 playerOffset = now(m0);
		ClientData.updatePlayerRidingOffset(uuid);
        Matrix4f m1 = new Matrix4f();
        m1.translate((float) x, (float) y, (float) z);
        m1.mul(m0);
		riderPositions.put(uuid, now(m1));

		if (isClientPlayer) {
			final double moveX = x + playerOffset.x;
			final double moveY = y + playerOffset.y;
			final double moveZ = z + playerOffset.z;
			final boolean movePlayer;

			if (MTRClient.isVivecraft()) {
				final Entity vehicle = clientPlayer.getVehicle();
				if (vehicle instanceof EntitySeat) {
					((EntitySeat) vehicle).setPosByTrain(moveX, moveY, moveZ);
					movePlayer = false;
				} else {
					movePlayer = true;
				}

				final float tempPercentageX = percentagesX.get(uuid);
				final boolean doorOpen = doorLeftOpen && tempPercentageX < 0 || doorRightOpen && tempPercentageX > 1;
				final boolean movedFar = Math.abs(lastSentX - moveX) > 2 || Math.abs(lastSentY - moveY) > 2 || Math.abs(lastSentZ - moveZ) > 2;

				if (doorOpen || MTRClient.getGameTick() - lastSentTicks > 60 && movedFar) {
					PacketTrainDataGuiClient.sendUpdateEntitySeatPassengerPosition(moveX, moveY, moveZ);
					lastSentX = moveX;
					lastSentY = moveY;
					lastSentZ = moveZ;
					lastSentTicks = MTRClient.getGameTick();
				}
			} else {
				movePlayer = true;
			}

			if (movePlayer) {
				clientPlayer.fallDistance = 0;
				clientPlayer.setDeltaMovement(0, 0, 0);
				clientPlayer.setSpeed(0);
				if (MTRClient.getGameTick() > 40) {
					clientPlayer.absMoveTo(moveX, moveY, moveZ);
				}
			}

			clientPlayerCallback.run();

			if (shouldSetOffset) {
				if (shouldSetYaw) {
					float angleDifference = (float) Math.toDegrees(clientPrevYaw - yaw);
					if (angleDifference > 180) {
						angleDifference -= 360;
					} else if (angleDifference < -180) {
						angleDifference += 360;
					}
					Utilities.incrementYaw(clientPlayer, angleDifference);
				}
				offset.add(x);
				offset.add(y);
				offset.add(z);
				offset.add(playerOffset.x);
				offset.add(playerOffset.y + (MTRClient.isVivecraft() ? 0 : clientPlayer.getEyeHeight()));
				offset.add(playerOffset.z);
			}

			clientPrevYaw = yaw;
		}
    }

    private static Vec3 now(Matrix4f matrix) {
        return matrix.getTranslationPart().toVec3();
    }
}