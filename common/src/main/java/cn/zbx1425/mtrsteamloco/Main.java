package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.block.*;
import cn.zbx1425.mtrsteamloco.item.*;
import cn.zbx1425.mtrsteamloco.network.*;
import com.google.gson.JsonParser;
import mtr.CreativeModeTabs;
import mtr.RegistryObject;
import mtr.item.ItemBridgeCreator;
import mtr.item.ItemWithCreativeTabBase;
import mtr.mappings.BlockEntityMapper;
import mtr.mappings.RegistryUtilities;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.zbx1425.mtrsteamloco.item.CompoundCreator;

import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.BiConsumer;

public class Main {

	public static final String MOD_ID = "mtrsteamloco";

	public static final Logger LOGGER = LoggerFactory.getLogger("MTR-ANTE");
	public static final JsonParser JSON_PARSER = new JsonParser();

	public static final boolean enableRegistry;
	static {
		boolean enableRegistry1;
		try {
			String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation()
					.toURI().getPath().toLowerCase(Locale.ROOT);
			enableRegistry1 = !jarPath.endsWith("-client.jar");
		} catch (URISyntaxException ignored) {
			enableRegistry1 = true;
		}
		enableRegistry = enableRegistry1;
	}

	public static final RegistryObject<Block> BLOCK_DEPARTURE_BELL = new RegistryObject<>(BlockDepartureBell::new);

	public static final RegistryObject<Block> BLOCK_EYE_CANDY = new RegistryObject<>(BlockEyeCandy::new);
	public static final RegistryObject<BlockEntityType<BlockEyeCandy.BlockEntityEyeCandy>>
			BLOCK_ENTITY_TYPE_EYE_CANDY = new RegistryObject<>(() ->
			RegistryUtilities.getBlockEntityType(
					BlockEyeCandy.BlockEntityEyeCandy::new,
					BLOCK_EYE_CANDY.get()
			));
	
	public static final RegistryObject<Block> BLOCK_DIRECT_NODE = new RegistryObject<>(BlockDirectNode::new);
	public static final RegistryObject<BlockEntityType<BlockDirectNode.BlockEntityDirectNode>>
			BLOCK_ENTITY_TYPE_DIRECT_NODE = new RegistryObject<>(() ->
			RegistryUtilities.getBlockEntityType(
					BlockDirectNode.BlockEntityDirectNode::new,
					BLOCK_DIRECT_NODE.get()
			));
	public static final RegistryObject<Item> ITEM_DIRECT_NODE = new RegistryObject<>(() -> new BlockItemDirectNode(CreativeModeTabs.CORE, BLOCK_DIRECT_NODE.get()));

	public static final RegistryObject<ItemWithCreativeTabBase> BRIDGE_CREATOR_1 = new RegistryObject<>(() -> new ItemBridgeCreator(1));
	public static final RegistryObject<ItemWithCreativeTabBase> COMPOUND_CREATOR = new RegistryObject<>(() -> new CompoundCreator());

	public static final SoundEvent SOUND_EVENT_BELL = RegistryUtilities.createSoundEvent(new ResourceLocation("mtrsteamloco:bell"));

	public static SimpleParticleType PARTICLE_STEAM_SMOKE;

	public static void init(RegistriesWrapper registries) {
		LOGGER.info("MTR-ANTE " + BuildConfig.MOD_VERSION + " built at "
				+ DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault()).format(BuildConfig.BUILD_TIME));
		if (enableRegistry) {
			registries.registerBlockAndItem("departure_bell", BLOCK_DEPARTURE_BELL, CreativeModeTabs.RAILWAY_FACILITIES);
			registries.registerBlockAndItem("eye_candy", BLOCK_EYE_CANDY, CreativeModeTabs.STATION_BUILDING_BLOCKS);
			registries.registerBlockEntityType("eye_candy", BLOCK_ENTITY_TYPE_EYE_CANDY);

			registries.registerBlock("direct_node", BLOCK_DIRECT_NODE);
			registries.registerBlockEntityType("direct_node", BLOCK_ENTITY_TYPE_DIRECT_NODE);
			registries.registerItem("direct_node", ITEM_DIRECT_NODE, CreativeModeTabs.CORE);

			registries.registerItem("bridge_creator_1", BRIDGE_CREATOR_1);
			registries.registerItem("compound_creator", COMPOUND_CREATOR);
			registries.registerSoundEvent("bell", SOUND_EVENT_BELL);
			PARTICLE_STEAM_SMOKE = registries.createParticleType(true);
			registries.registerParticleType("steam_smoke", PARTICLE_STEAM_SMOKE);

			mtr.Registry.registerNetworkReceiver(PacketUpdateBlockEntity.PACKET_UPDATE_BLOCK_ENTITY,
					PacketUpdateBlockEntity::receiveUpdateC2S);
			mtr.Registry.registerNetworkReceiver(PacketUpdateRail.PACKET_UPDATE_RAIL,
					PacketUpdateRail::receiveUpdateC2S);
			mtr.Registry.registerNetworkReceiver(PacketUpdateHoldingItem.PACKET_UPDATE_HOLDING_ITEM,
					PacketUpdateHoldingItem::receiveUpdateC2S);
			mtr.Registry.registerNetworkReceiver(PacketUpdateTrainCustomConfigs.C2S,
					PacketUpdateTrainCustomConfigs::receiveUpdateC2S);

			mtr.Registry.registerPlayerJoinEvent(PacketVersionCheck::sendVersionCheckS2C);
		}
	}

	@FunctionalInterface
	public interface RegisterBlockItem {
		void accept(String string, RegistryObject<Block> block, CreativeModeTabs.Wrapper tab);
	}
}
