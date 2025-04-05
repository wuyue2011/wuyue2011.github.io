package cn.zbx1425.mtrsteamloco.block;

import mtr.block.BlockNode;
import mtr.mappings.BlockEntityClientSerializableMapper;
import mtr.mappings.BlockEntityMapper;
import cn.zbx1425.mtrsteamloco.mixin.RailAngleMixin;

public class BlockDirectNode extends BlockNode.BlockContinuousMovementNode {
    public BlockDirectNode(boolean upper, boolean isStation) {
        super(upper, isStation);
    }

    @Override
	public BlockEntityMapper createBlockEntity(BlockPos pos, BlockState state) {
		return new BlockEntityDirectNode(pos, state);
	}

    public static class BlockEntityDirectNode extends BlockEntityClientSerializableMapper {
        private float angle = -114514F;
        private RailAngle railAngle = null;

        public static final String KEY_ANGLE = "angle";
        public static final String KEY_LOCKED = "locked";

        public BlockEntityDirectNode(BlockPos pos, BlockState state) {
            super(pos, state);
        }

        public void bind(BlockEntityDirectNode other) {
            if (railAngle == null) return;
            BlockPos thi = getBlockPos();
            BlockPos oth = other.getBlockPos();
            angle = (float) Math.toDegrees(Math.atan2(oth.getZ() - thi.getZ(), oth.getX() - thi.getX()));
            angle = (angle + 360F) % 360F;
            railAngle = RailAngleMixin.fromDegrees(angle);
            other.bind(this);
        }

        public void unbind() {
            railAngle = null;
            angle = -114514F;
        }

        public RailAngle getRailAngle() {
            return railAngle;
        }

        public boolean isLocked() {
            return railAngle != null;
        }
        
        @Override
        public void readCompoundTag(CompoundTag compoundTag) {
            angle = compoundTag.getFloat(KEY_ANGLE);
            boolean locked = compoundTag.getBoolean(KEY_LOCKED);
            if (locked) {
                railAngle = RailAngleMixin.fromDegrees(angle);
            } else {
                railAngle = null;
            }
        }

        @Override
        public void writeCompoundTag(CompoundTag compoundTag) {
            compoundTag.putFloat(KEY_ANGLE, angle);
            compoundTag.putBoolean(KEY_LOCKED, isLocked());
        }
    }
}