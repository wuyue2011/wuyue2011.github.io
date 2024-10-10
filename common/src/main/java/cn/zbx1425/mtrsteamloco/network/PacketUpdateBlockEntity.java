package cn.zbx1425.mtrsteamloco.network;

import cn.zbx1425.mtrsteamloco.Main;
import io.netty.buffer.Unpooled;
import mtr.RegistryClient;
import mtr.mappings.BlockEntityMapper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import mtr.data.ScheduleEntry;
import mtr.data.RailwayData;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
import cn.zbx1425.mtrsteamloco.network.util.Serializer;
import cn.zbx1425.mtrsteamloco.data.Schedule;
import mtr.data.Station;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class PacketUpdateBlockEntity {

    public static ResourceLocation PACKET_UPDATE_BLOCK_ENTITY = new ResourceLocation(Main.MOD_ID, "update_block_entity");

    public static void sendUpdateC2S(BlockEntityMapper blockEntity) {
        Level level = blockEntity.getLevel();
        if (level == null) return;

        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeResourceLocation(level.dimension().location());
        packet.writeBlockPos(blockEntity.getBlockPos());
#if MC_VERSION >= "11903"
        packet.writeId(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntity.getType());
#else
        packet.writeVarInt(net.minecraft.core.Registry.BLOCK_ENTITY_TYPE.getId(blockEntity.getType()));
#endif
        packet.writeBoolean(true);
        CompoundTag tag = new CompoundTag();
        blockEntity.writeCompoundTag(tag);
        packet.writeNbt(tag);

        RegistryClient.sendToServer(PACKET_UPDATE_BLOCK_ENTITY, packet);
    }

    public static void sendUpdateC2S(BlockEntityMapper blockEntity, boolean cover) {
                Level level = blockEntity.getLevel();
        if (level == null) return;

        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeResourceLocation(level.dimension().location());
        packet.writeBlockPos(blockEntity.getBlockPos());
#if MC_VERSION >= "11903"
        packet.writeId(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntity.getType());
#else
        packet.writeVarInt(net.minecraft.core.Registry.BLOCK_ENTITY_TYPE.getId(blockEntity.getType()));
#endif
        packet.writeBoolean(cover);
        CompoundTag tag = new CompoundTag();
        if (cover) {
            blockEntity.writeCompoundTag(tag);
        }
        packet.writeNbt(tag);

        RegistryClient.sendToServer(PACKET_UPDATE_BLOCK_ENTITY, packet);
    }

    public static void receiveUpdateC2S(MinecraftServer server, ServerPlayer player, FriendlyByteBuf packet) {
#if MC_VERSION >= "11903"
        ResourceKey<Level> levelKey = packet.readResourceKey(net.minecraft.core.registries.Registries.DIMENSION);
#else
        ResourceKey<Level> levelKey = ResourceKey.create(net.minecraft.core.Registry.DIMENSION_REGISTRY, packet.readResourceLocation());
#endif
        BlockPos blockPos = packet.readBlockPos();
#if MC_VERSION >= "11903"
        BlockEntityType<?> blockEntityType = packet.readById(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE);
#else
        BlockEntityType<?> blockEntityType = net.minecraft.core.Registry.BLOCK_ENTITY_TYPE.byId(packet.readVarInt());
#endif
        boolean cover = packet.readBoolean();

        CompoundTag tag0 = packet.readNbt();

        server.execute(() -> {
            ServerLevel level = server.getLevel(levelKey);
            if (level == null || blockEntityType == null) return;
            level.getBlockEntity(blockPos, blockEntityType).ifPresent(blockEntity -> {
                if (tag0 != null && blockEntity instanceof BlockEyeCandy.BlockEntityEyeCandy) {
                    CompoundTag tag1 = new CompoundTag();
                        BlockEyeCandy.BlockEntityEyeCandy beec = (BlockEyeCandy.BlockEntityEyeCandy) blockEntity;
                        if (!cover) {
                            beec.writeCompoundTag(tag1);
                        }else {
                            tag1 = tag0;
                        }
                        RailwayData railwayData = RailwayData.getInstance(level);
                        Map<Long, List<Schedule>> schedulesMap = new HashMap<>();
                        Long platformId = (long) 0 ;
                        Long stationId = (long) 0 ;
                        while (true) {
                            if (railwayData == null) break;

                            platformId = railwayData.getClosePlatformId(railwayData.platforms, railwayData.dataCache, blockPos, beec.radius, beec.lower, beec.upper);
                            if (platformId != null) {
                                List<ScheduleEntry> ScheduleEntries = railwayData.getSchedulesAtPlatform(platformId);
                                List<Schedule> scheduleList0 = new ArrayList<>();
                                for (ScheduleEntry scheduleEntry : ScheduleEntries) {
                                    scheduleList0.add(new Schedule(scheduleEntry));
                                }
                                schedulesMap.put(platformId, scheduleList0);
                            }

                            Station station = RailwayData.getStation(railwayData.stations, railwayData.dataCache, blockPos);

                            if (station != null) {
                                Map<Long, List<ScheduleEntry>> schedules = new HashMap<>();
                                railwayData.getSchedulesForStation(schedules, station.id);
                                List<Schedule> scheduleList = new ArrayList<>();
                                for (Long key : schedules.keySet()) {
                                    scheduleList = new ArrayList<>();
                                    for (ScheduleEntry scheduleEntry : schedules.get(key)) {
                                        scheduleList.add(new Schedule(scheduleEntry));
                                    }
                                    schedulesMap.put(key, scheduleList);
                                }
                            }
                            break;
                        }
                        try {
                            tag1.putByteArray("schedules", Serializer.serialize(schedulesMap, 1));
                        }catch (IOException e) {}
                        int ticks = beec.ticks + 1;
                        tag1.putInt("ticks", ticks);
                        tag1.putLong("platformId", platformId);
                        tag1.putLong("stationId", stationId);

                        blockEntity.load(tag1);
                        blockEntity.setChanged();
                        level.getChunkSource().blockChanged(blockPos);
                }
            });
        });
    }
}
