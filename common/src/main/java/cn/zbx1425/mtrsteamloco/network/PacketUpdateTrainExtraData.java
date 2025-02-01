package cn.zbx1425.mtrsteamloco.network;

import io.netty.buffer.Unpooled;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import cn.zbx1425.mtrsteamloco.Main;
import mtr.RegistryClient;
import cn.zbx1425.mtrsteamloco.network.util.StringMapSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import mtr.data.RailwayData;
import mtr.data.Siding;
import mtr.data.TrainServer;
import cn.zbx1425.mtrsteamloco.data.TrainExtraSupplier;
import cn.zbx1425.mtrsteamloco.mixin.SidingAccessor;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class PacketUpdateTrainExtraData {
    
    public static ResourceLocation PACKET_UPDATE_TRAIN_EXTRA_DATA = new ResourceLocation(Main.MOD_ID, "update_train_extra_data");

    public static void sendUpdateC2S(long sidingId, long trainId, Map<String, String> extraData) {
        if (extraData == null) return;

        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeLong(sidingId);
        packet.writeLong(trainId);
        String str = "";
        try {
            str = StringMapSerializer.serializeToString(extraData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        packet.writeUtf(str);
        
        RegistryClient.sendToServer(PACKET_UPDATE_TRAIN_EXTRA_DATA, packet);
    }

    public static void receiveUpdateC2S(MinecraftServer server, ServerPlayer player, FriendlyByteBuf packet) {
        long trainId = packet.readLong();
        long sidingId = packet.readLong();
        String str = packet.readUtf();
        Map<String, String> extraData = null;
        try {
            extraData = StringMapSerializer.deserialize(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (extraData == null) return;
        if (packet == null) return;
        
        server.execute(() -> {
            RailwayData railwayData = RailwayData.getInstance(player.level);
            if (railwayData == null) return;
            if (railwayData.sidings == null) return;
            Set<Siding> sidings = new HashSet<>(railwayData.sidings);
            for (Siding siding : sidings) {
                if (siding == null) continue;
                if (siding.id == sidingId) {
                    Set<TrainServer> trains = ((SidingAccessor) siding).getTrains();
                    Set<TrainServer> copy = new HashSet<>(trains);
                    for (TrainServer train : trains) {
                        if (train.id == trainId) {
                            (TrainExtraSupplier train).setExtraData(extraData);
                            break;
                        }
                    }
                    break;
                }
            }
        });
    }
}