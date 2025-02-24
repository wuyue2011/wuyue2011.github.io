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
        public double length;
        public Double interval;
        public Integer[] blockIds;

        public static final String TAG_ORDER = "order";
        public static final String TAG_NAME = "name";
        public static final String TAG_HEIGHT = "height";
        public static final String TAG_LENGTH = "length";
        public static final String TAG_START = "start";
        public static final String TAG_WIDTH = "width";
        public static final String TAG_INTERVAL = "interval";
        public static final String TAG_BLOCK_IDS = "block_ids";

        public SliceTask(int order, String name, int width, int height, double start, double length, Double interval, Integer[] blockIds) {
            this.order = order;
            this.name = name;
            this.width = width;
            this.height = height;
            this.start = start;
            this.length = length;
            this.interval = interval;
            this.blockIds = blockIds;
        }

        public SliceTask(CompoundTag compoundTag) {
            this.order = compoundTag.getInt(TAG_ORDER);
            this.name = compoundTag.getString(TAG_NAME);
            this.width = compoundTag.getInt(TAG_WIDTH);
            this.height = compoundTag.getInt(TAG_HEIGHT);
            this.start = compoundTag.getDouble(TAG_START);
            this.length = compoundTag.getDouble(TAG_LENGTH);
            if (compoundTag.contains(TAG_INTERVAL)) {
                this.interval = compoundTag.getDouble(TAG_INTERVAL);
            } else {
                this.interval = null;
            }
            this.blockIds = IntegerArraySerializer.deserialize(compoundTag.getString(TAG_BLOCK_IDS));
        }

        public CompoundTag toCompoundTag() {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putInt(TAG_ORDER, order);
            compoundTag.putString(TAG_NAME, name);
            compoundTag.putInt(TAG_WIDTH, width);
            compoundTag.putInt(TAG_HEIGHT, height);
            compoundTag.putDouble(TAG_START, start);
            compoundTag.putDouble(TAG_LENGTH, length);
            if (interval!= null) {
                compoundTag.putDouble(TAG_INTERVAL, interval);
            }
            compoundTag.putString(TAG_BLOCK_IDS, IntegerArraySerializer.serialize(blockIds));
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
                this.blockIds = sliceTask.blockIds;
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

        private static final double INCREMENT = 0.1;

        public SliceAction(Level world, Player player, Rail rail, SliceTask task) {
            super(world, player, null, rail, 0, 0, null);
			this.world = world;
			uuid = player.getUUID();
			playerName = player.getName().getString();
			this.rail = rail;
            this.task = task;
            index = 0;
            length = rail.getLength();
            distance = 0;
            width = task.width;
            height = task.height;

            if (task.interval == null) {
                starts = new double[]{task.start};
            } else {
                double temp = task.start * length;
                double count = task.length * length + task.interval;
                List<Double> list = new ArrayList<>();
                while (temp + count < length) {
                    list.add(temp);
                    temp += count;
                }
                starts = list.stream().mapToDouble(Double::doubleValue).toArray();
            }
        }

        @Override
        public boolean build() {
            final long startTime = System.currentTimeMillis();
            Vec3 last = rail.getPosition(starts[0]);
            while (System.currentTimeMillis() - startTime < 2) {
                if (index >= starts.length) {
                    showProgressMessage(1);
                    return true;
                }
                if (distance >= starts[index] + length) {
                    index++;
                    if (index >= starts.length) {
                        showProgressMessage(1);
                        return true;
                    }
                    distance = starts[index];
                    last = rail.getPosition(starts[index]);
                    continue;
                }
                distance += INCREMENT;
                if (distance >= length) {
                    showProgressMessage(1);
                    return true;
                }
                Vec3 next = rail.getPosition(distance);
                // Vec3 center = new Vec3((last.x + next.x) / 2, (last.y + next.y) / 2, (last.z + next.z) / 2);

                final float yaw = (float) Mth.atan2(next.x - last.x, next.z - last.z);
                final float pitch = (float) Mth.atan2(next.y - last.y, (float) Math.sqrt((next.x - last.x) * (next.x - last.x) + (next.z - last.z) * (next.z - last.z)));
                final float roll = RailExtraSupplier.getRollAngle(rail, distance - INCREMENT / 2);

                Matrix4f mat = new Matrix4f();
                mat.translate((float) last.x, (float) last.y, (float) last.z);
                mat.rotateY(yaw);
                mat.rotateX(pitch);
                mat.rotateZ(roll);

                mat.translate(-width / 2.0F + 0.5F, height / 2.0F - 0.5F, 0);

                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        int index = i * width + j;
                        Integer blockId = task
                        .blockIds[index];
                        Vector3f pos = mat.getTranslationPart();
                        BlockPos blockPos = new BlockPos((int) Math.floor(pos.x()), (int) Math.floor(pos.y()), (int) Math.floor(pos.z()));
                        if (blockId != null && !blacklistedPos.contains(blockPos) && canPlace(world,blockPos)) {
                            world.setBlockAndUpdate(blockPos, Block.stateById(blockId));
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