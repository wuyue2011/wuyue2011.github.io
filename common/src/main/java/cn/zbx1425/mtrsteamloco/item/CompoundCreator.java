package cn.zbx1425.mtrsteamloco.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.context.UseOnContext;
import mtr.item.ItemNodeModifierBase;
import mtr.data.Rail;
import net.minecraft.world.level.block.Block;
import mtr.data.TransportMode;
import mtr.block.BlockNode;
import cn.zbx1425.sowcer.math.Matrix4f;
import mtr.mappings.Text;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import cn.zbx1425.sowcer.math.Vector3f;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import mtr.data.RailwayData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import cn.zbx1425.mtrsteamloco.data.RailActionsModuleExtraSupplier;
import net.minecraft.world.level.block.state.BlockState;
import cn.zbx1425.mtrsteamloco.data.RailExtraSupplier;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import cn.zbx1425.mtrsteamloco.network.util.IntegerArraySerializer;
import cn.zbx1425.mtrsteamloco.network.PacketScreen;
import mtr.data.RailAngle;

import java.util.*;

public class CompoundCreator extends ItemNodeModifierBase {
    private static final String TAG_TASKS = "tasks";

    public CompoundCreator() {
        super(true, false, false, true);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (clickCondition(ctx)) {
            return super.useOn(ctx);
        } else {
            Player player = ctx.getPlayer();
            if (player instanceof ServerPlayer serverPlayer) {
                PacketScreen.sendScreenBlockS2C(serverPlayer, "compound_creator", BlockPos.ZERO);
            }
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    protected void onRemove(Level world, BlockPos start, BlockPos end, Player player, RailwayData data) {
        
    }

	@Override
	protected void onConnect(Level world, ItemStack stack, TransportMode transportMode, BlockState stateStart, BlockState stateEnd, BlockPos posStart, BlockPos posEnd, RailAngle facingStart, RailAngle facingEnd, Player player, RailwayData railwayData) {
        if (player == null) return;
        if (railwayData.containsRail(posStart, posEnd)) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains(TAG_TASKS)) {
                RailActionsModuleExtraSupplier acc = (RailActionsModuleExtraSupplier) (Object) railwayData.railwayDataRailActionsModule;
                List<SliceTask> tasks = new ArrayList<>();
                CompoundTag tasksTag = tag.getCompound(TAG_TASKS);
                Set<String> keys = tasksTag.getAllKeys();
                for (String key : keys) {
                    tasks.add(new SliceTask(tasksTag.getCompound(key)));
                }
                tasks.sort(Comparator.comparingInt(sliceTask -> sliceTask.order));
                int size = tasks.size();
                for (int i = 0; i < size; i++) {
                    acc.getRailActions().add(new SliceAction(acc.getWorld(), player, acc.getRails().get(posStart).get(posEnd), tasks.get(i)));
                }
                acc.sendUpdateS2C();
            } else {
                player.displayClientMessage(Text.translatable("gui.mtr.rail_not_found_action"), true);
            }
        } else {
            player.displayClientMessage(Text.translatable("gui.mtr.rail_not_found_action"), true);
        }
	}

    public static class SliceTask {
        public int order;
        public String name;
        public int width;
        public int height;
        public double start;
        public Double length;
        public Double interval;
        public double increment;
        public Integer[] blockIds;
        public boolean useYaw;
        public boolean usePitch;
        public boolean useRoll;

        public static final String TAG_ORDER = "order";
        public static final String TAG_NAME = "name";
        public static final String TAG_HEIGHT = "height";
        public static final String TAG_LENGTH = "length";
        public static final String TAG_START = "start";
        public static final String TAG_WIDTH = "width";
        public static final String TAG_INTERVAL = "interval";
        public static final String TAG_INCREMENT = "increment";
        public static final String TAG_BLOCK_IDS = "block_ids";
        public static final String TAG_USE_YAW = "use_yaw";
        public static final String TAG_USE_PITCH = "use_pitch";
        public static final String TAG_USE_ROLL = "use_roll";

        public SliceTask(int order, String name, int width, int height, double start, Double length, Double interval, double increment, Integer[] blockIds, boolean useYaw, boolean usePitch, boolean useRoll) {
            this.order = order;
            this.name = name;
            this.width = width;
            this.height = height;
            this.start = start;
            this.length = length;
            this.interval = interval;
            this.increment = increment;
            this.blockIds = blockIds;
            this.useYaw = useYaw;
            this.usePitch = usePitch;
            this.useRoll = useRoll;
        }

        public SliceTask(CompoundTag compoundTag) {
            this.order = compoundTag.getInt(TAG_ORDER);
            this.name = compoundTag.getString(TAG_NAME);
            this.width = compoundTag.getInt(TAG_WIDTH);
            this.height = compoundTag.getInt(TAG_HEIGHT);
            this.start = compoundTag.getDouble(TAG_START);
            if (compoundTag.contains(TAG_LENGTH)) {
                this.length = compoundTag.getDouble(TAG_LENGTH);
            } else {
                this.length = null;
            }
            this.increment = compoundTag.getDouble(TAG_INCREMENT);
            if (compoundTag.contains(TAG_INTERVAL)) {
                this.interval = compoundTag.getDouble(TAG_INTERVAL);
            } else {
                this.interval = null;
            }
            this.blockIds = IntegerArraySerializer.deserialize(compoundTag.getString(TAG_BLOCK_IDS));
            this.useYaw = compoundTag.getBoolean(TAG_USE_YAW);
            this.usePitch = compoundTag.getBoolean(TAG_USE_PITCH);
            this.useRoll = compoundTag.getBoolean(TAG_USE_ROLL);
        }

        public CompoundTag toCompoundTag() {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putInt(TAG_ORDER, order);
            compoundTag.putString(TAG_NAME, name);
            compoundTag.putInt(TAG_WIDTH, width);
            compoundTag.putInt(TAG_HEIGHT, height);
            compoundTag.putDouble(TAG_START, start);
            if (length != null) {
                compoundTag.putDouble(TAG_LENGTH, length);
            }
            compoundTag.putDouble(TAG_INCREMENT, increment);
            if (interval!= null) {
                compoundTag.putDouble(TAG_INTERVAL, interval);
            }
            compoundTag.putString(TAG_BLOCK_IDS, IntegerArraySerializer.serialize(blockIds));
            compoundTag.putBoolean(TAG_USE_YAW, useYaw);
            compoundTag.putBoolean(TAG_USE_PITCH, usePitch);
            compoundTag.putBoolean(TAG_USE_ROLL, useRoll);
            return compoundTag;
        }

        public void copyFrom(SliceTask sliceTask) {
            synchronized (this) {
                this.order = sliceTask.order;
                this.name = sliceTask.name;
                this.width = sliceTask.width;
                this.height = sliceTask.height;
                this.start = sliceTask.start;
                this.length = sliceTask.length;
                this.interval = sliceTask.interval;
                this.increment = sliceTask.increment;
                this.blockIds = sliceTask.blockIds;
                this.useYaw = sliceTask.useYaw;
                this.usePitch = sliceTask.usePitch;
                this.useRoll = sliceTask.useRoll;
            }
        }
    }

    public static class SliceAction extends Rail.RailActions {
        public final SliceTask task;
        public double[] starts;
        public int index;
        private int width;
        private int height;
        private double distance;
        private final Level world;
		private final UUID uuid;
		private final String playerName;
		private final Rail rail;
		private final double length;
		private final Set<BlockPos> blacklistedPos = new HashSet<>();

        private final double INCREMENT;

        public SliceAction(Level world, Player player, Rail rail, SliceTask task) {
            super(world, player, null, rail, 0, 0, null);
			this.world = world;
			uuid = player.getUUID();
			playerName = player.getName().getString();
			this.rail = rail;
            this.task = task;
            index = 0;
            length = rail.getLength();
            width = task.width;
            height = task.height;
            INCREMENT = task.increment;

            if (task.interval == null || task.length == null) {
                starts = new double[]{task.start};
            } else {
                double temp = task.start;
                double count = task.length + task.interval;
                List<Double> list = new ArrayList<>();
                while (temp + count < length) {
                    list.add(temp);
                    temp += count;
                }
                starts = list.stream().mapToDouble(Double::doubleValue).toArray();
            }
            distance = starts[0];
        }

        @Override
        public boolean build() {
            final long startTime = System.currentTimeMillis();
            Vec3 last = rail.getPosition(starts[0]);
            while (System.currentTimeMillis() - startTime < 2) {
                if (index >= starts.length) {
                    showProgressMessage(100);
                    return true;
                }
                
                if (distance >= starts[index] + (task.length == null ? length : task.length)) {
                    index++;
                    if (index >= starts.length) {
                        showProgressMessage(100);
                        return true;
                    }
                    distance = starts[index];
                    last = rail.getPosition(starts[index]);
                    continue;
                }
                distance += INCREMENT;
                if (distance >= length) {
                    showProgressMessage(100);
                    return true;
                }
                Vec3 next = rail.getPosition(distance);
                // Vec3 center = new Vec3((last.x + next.x) / 2, (last.y + next.y) / 2, (last.z + next.z) / 2);

                Matrix4f mat = new Matrix4f();
                mat.translate((float) last.x, (float) last.y, (float) last.z);
                if (task.useYaw) {
                    final float yaw = (float) Mth.atan2(next.x - last.x, next.z - last.z);
                    mat.rotateY(yaw);
                }
                if (task.usePitch) {
                    final float pitch = (float) Mth.atan2(next.y - last.y, (float) Math.sqrt((next.x - last.x) * (next.x - last.x) + (next.z - last.z) * (next.z - last.z)));
                    mat.rotateX(pitch);
                }
                if (task.useRoll) {
                    final float roll = RailExtraSupplier.getRollAngle(rail, distance - INCREMENT / 2);
                    mat.rotateZ(roll);
                }

                mat.translate(-width / 2.0F + 0.5F, height / 2.0F - 0.5F, 0);

                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        int index = i * width + j;
                        Integer blockId = task
                        .blockIds[index];
                        Vector3f pos = mat.getTranslationPart();
                        BlockPos blockPos = new BlockPos((int) Math.floor(pos.x()), (int) Math.floor(pos.y()), (int) Math.floor(pos.z()));
                        if (blockId != null && !blacklistedPos.contains(blockPos) && canPlace(world,blockPos)) {
                            BlockState state = Block.stateById(blockId);
                            if (state.hasProperty(BlockStateProperties.FACING)) {
                                Direction dir = rotateDirection(state.getValue(BlockStateProperties.FACING), mat);
                                state = state.setValue(BlockStateProperties.FACING, dir);
                            } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                                Direction dir = rotateDirection(state.getValue(BlockStateProperties.HORIZONTAL_FACING), mat);
                                state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, dir);
                            } else if (state.hasProperty(BlockStateProperties.FACING_HOPPER)) {
                                Direction dir = rotateDirection(state.getValue(BlockStateProperties.FACING_HOPPER), mat);
                                state = state.setValue(BlockStateProperties.FACING_HOPPER, dir);
                            }
                            world.setBlockAndUpdate(blockPos, state);
                        }
                        blacklistedPos.add(blockPos);
                        mat.translate(1.0F, 0, 0);
                    }
                    mat.translate(width * -1.0F, -1.0F, 0);
                }
                last = next;
            }

            showProgressMessage(RailwayData.round(100 * distance / length, 1));
            return false;
        }

        private Direction rotateDirection(Direction dir, Matrix4f mat) {
            if (dir == Direction.UP || Direction.DOWN == dir) return dir;
            double d = dir.toYRot() + mat.getEulerAnglesYXZ().y() / Math.PI * 180;
            return Direction.fromYRot(d);
        }

        private void showProgressMessage(float percentage) {
            final Player player = world.getPlayerByUUID(uuid);
			if (player != null) {
				player.displayClientMessage(Text.translatable("gui.mtr.percentage_complete_slice", percentage), true);
			}
        }

        private static boolean canPlace(Level world, BlockPos pos) {
			return world.getBlockEntity(pos) == null && !(world.getBlockState(pos).getBlock() instanceof BlockNode);
		}

        @Override
        public void writePacket(FriendlyByteBuf packet) {
			packet.writeLong(id);
			packet.writeUtf(playerName);
			packet.writeFloat(RailwayData.round(length, 1));
			packet.writeUtf("percentage_complete_slice");
			packet.writeUtf("rail_action_slice");
			packet.writeInt(0x47fec4);
		}
    }
}