package cn.zbx1425.mtrsteamloco.block;

import cn.zbx1425.mtrsteamloco.Main;
import mtr.block.BlockNode;
import net.minecraft.world.level.Level;
import mtr.mappings.EntityBlockMapper;
import net.minecraft.world.level.block.RenderShape;
import org.jetbrains.annotations.NotNull;
import mtr.mappings.BlockEntityClientSerializableMapper;
import net.minecraft.nbt.CompoundTag;
import mtr.mappings.BlockEntityMapper;
import net.minecraft.world.level.block.state.BlockState;
import mtr.data.RailAngle;
import net.minecraft.core.BlockPos;
import cn.zbx1425.mtrsteamloco.data.RailAngleExtra;
import mtr.data.TransportMode;
import net.minecraft.server.level.ServerLevel;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateBlockEntity;

public class BlockDirectNode extends BlockNode implements EntityBlockMapper {

    public BlockDirectNode() {
        super(TransportMode.TRAIN);
    }

    @Override
    public RenderShape getRenderShape(@NotNull BlockState blockState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
	public BlockEntityMapper createBlockEntity(BlockPos pos, BlockState state) {
		return new BlockEntityDirectNode(pos, state);
	}

    public static class BlockEntityDirectNode extends BlockEntityClientSerializableMapper {
        private double angle = -114514F;
        private RailAngle railAngle = null;

        public static final String KEY_ANGLE = "angle";

        public BlockEntityDirectNode(BlockPos pos, BlockState state) {
            super(Main.BLOCK_ENTITY_TYPE_DIRECT_NODE.get(), pos, state);
        }

        public void bind(BlockEntityDirectNode other) {
            if (railAngle != null) return;
            BlockPos thi = getBlockPos();
            BlockPos oth = other.getBlockPos();
            bind(Math.toDegrees(Math.atan2(oth.getZ() - thi.getZ(), oth.getX() - thi.getX())));
            other.bind(this);
        }

        public void bind(double angle) {
            angle = normalize(angle);
            this.angle =angle;
            railAngle = RailAngleExtra.fromDegrees(angle);
            this.setChanged();
            Level level = getLevel();
            if (level == null) return;

            if (level instanceof ServerLevel sl) sl.getChunkSource().blockChanged(getBlockPos());
            else PacketUpdateBlockEntity.sendUpdateC2S(this);
        }

        public void unbind() {
            railAngle = null;
            angle = -114514D;
            this.setChanged();
            Level level = getLevel();
            if (level == null) return;

            if (level instanceof ServerLevel sl) sl.getChunkSource().blockChanged(getBlockPos());
            else PacketUpdateBlockEntity.sendUpdateC2S(this);
        }

        public double getAngleDegrees() {
            return angle;
        }

        public RailAngle getRailAngle() {
            return railAngle;
        }

        public boolean isBound() {
            return railAngle != null;
        }
        
        @Override
        public void readCompoundTag(CompoundTag compoundTag) {
            if (compoundTag.contains(KEY_ANGLE)) {
                angle = compoundTag.getDouble(KEY_ANGLE);
                railAngle = RailAngleExtra.fromDegrees(angle);
            } else {
                angle = -114514D;
                railAngle = null;
            }
        }

        @Override
        public void writeCompoundTag(CompoundTag compoundTag) {
            if (!isBound()) return;
            compoundTag.putDouble(KEY_ANGLE, angle);
        }

        public static double normalize(double angle) {
            while (angle < 0F) angle += 180D;
            while (angle >= 180F) angle -= 180D;
            return angle;
        }
    }
}