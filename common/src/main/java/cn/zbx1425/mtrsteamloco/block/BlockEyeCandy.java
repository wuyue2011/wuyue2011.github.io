package cn.zbx1425.mtrsteamloco.block;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.network.PacketScreen;
import cn.zbx1425.mtrsteamloco.render.scripting.eyecandy.EyeCandyScriptContext;
import cn.zbx1425.sowcer.math.Vector3f;
import mtr.mappings.BlockDirectionalMapper;
import mtr.mappings.BlockEntityClientSerializableMapper;
import mtr.mappings.BlockEntityMapper;
import mtr.mappings.EntityBlockMapper;
import net.minecraft.core.BlockPos;
import mtr.client.ClientData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
#if MC_VERSION < "12000"
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
#endif
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import cn.zbx1425.mtrsteamloco.network.util.Serializer;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import mtr.block.IBlock;
import net.minecraft.client.Minecraft;
import mtr.MTRClient;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import java.util.Random;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import mtr.block.BlockTicketBarrier;
import mtr.data.TicketSystem;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import mtr.mappings.Utilities;
import mtr.SoundEvents;
import cn.zbx1425.mtrsteamloco.data.EyeCandyRegistry;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import cn.zbx1425.mtrsteamloco.data.EyeCandyProperties;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public class BlockEyeCandy extends BlockDirectionalMapper implements EntityBlockMapper {
    
    public static final EnumProperty<TicketSystem.EnumTicketBarrierOpen> OPEN = BlockTicketBarrier.OPEN;
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
    public static final ToIntFunction<BlockState> LIGHT_EMISSION = (p_153701_) -> {
        return p_153701_.getValue(LEVEL);
    };

    public BlockEyeCandy() {
        super(
#if MC_VERSION < "12000"
                BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_GRAY)
#else
                BlockBehaviour.Properties.of()
#endif
                        .strength(2).lightLevel(LIGHT_EMISSION)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LEVEL, OPEN);
    }

    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.getMainHandItem().is(mtr.Items.BRUSH.get())) {
            if (!level.isClientSide) {
                PacketScreen.sendScreenBlockS2C((ServerPlayer) player, "eye_candy", pos);
            } else {
                return InteractionResult.PASS;
            }
            return InteractionResult.SUCCESS;
        } else {
            if (level.isClientSide) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof BlockEntityEyeCandy) {
                    BlockEntityEyeCandy blockEntityEyeCandy = (BlockEntityEyeCandy) blockEntity;
                    blockEntityEyeCandy.tryCallBeClickedFunctionAsync(player);
                } else {
                    Main.LOGGER.warn("BlockEntityEyeCandy not found at " + pos + ", " + level);
                    return InteractionResult.PASS;
                }
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    @Override
    public BlockEntityMapper createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BlockEntityEyeCandy(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(@NotNull BlockState blockState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext collisionContext) {
        final BlockEntity entity = world.getBlockEntity(pos);
        VoxelShape shape = Shapes.block();
        if (entity instanceof BlockEntityEyeCandy) {
            shape = Shapes.or(Shapes.empty(), ((BlockEntityEyeCandy) entity).shape.get());
        } else {
            Main.LOGGER.warn("BlockEntityEyeCandy not found at " + pos + ", " + world);
        }
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext collisionContext) {
        final BlockEntity entity = world.getBlockEntity(pos);
        VoxelShape shape = Shapes.empty();
        if (entity instanceof BlockEntityEyeCandy) {
            shape = Shapes.or(Shapes.empty(), ((BlockEntityEyeCandy) entity).collision.get());
        } else {
            Main.LOGGER.warn("BlockEntityEyeCandy not found at " + pos + ", " + world);
        }
        return shape;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return Shapes.empty();
    }

	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        final BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BlockEntityEyeCandy) {
            if (((BlockEntityEyeCandy) blockEntity).isTicketBarrier() == false) return;
        } else {
            return;
        }
        boolean isEntrance = ((BlockEntityEyeCandy) blockEntity).isEntrance;
		if (!world.isClientSide && entity instanceof Player) {
			final Direction facing = IBlock.getStatePropertySafe(state, FACING);
			final Vec3 playerPosRotated = entity.position().subtract(pos.getX() + 0.5, 0, pos.getZ() + 0.5).yRot((float) Math.toRadians(facing.toYRot()));
			final TicketSystem.EnumTicketBarrierOpen open = IBlock.getStatePropertySafe(state, OPEN);

			if (open.isOpen() && playerPosRotated.z > 0) {
				world.setBlockAndUpdate(pos, state.setValue(OPEN, TicketSystem.EnumTicketBarrierOpen.CLOSED));
			} else if (!open.isOpen() && playerPosRotated.z < 0) {
				final TicketSystem.EnumTicketBarrierOpen newOpen = TicketSystem.passThrough(world, pos, (Player) entity, isEntrance, !isEntrance, SoundEvents.TICKET_BARRIER, SoundEvents.TICKET_BARRIER_CONCESSIONARY, SoundEvents.TICKET_BARRIER, SoundEvents.TICKET_BARRIER_CONCESSIONARY, null, false);
				world.setBlockAndUpdate(pos, state.setValue(OPEN, newOpen));
				if (newOpen != TicketSystem.EnumTicketBarrierOpen.CLOSED && !world.getBlockTicks().hasScheduledTick(pos, this)) {
					Utilities.scheduleBlockTick(world, pos, this, 40);
				}
			}
		}
	}

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos) {
		world.setBlockAndUpdate(pos, state.setValue(OPEN, TicketSystem.EnumTicketBarrierOpen.CLOSED));
	}

    public static class BlockEntityEyeCandy extends BlockEntityClientSerializableMapper {

        public String prefabId = null;
        public EyeCandyProperties properties = null;

        public float translateX = 0, translateY = 0, translateZ = 0;
        public float rotateX = 0, rotateY = 0, rotateZ = 0;

        public boolean fullLight = false;

        public Map<String, String> data = new HashMap<>();

        public EyeCandyScriptContext scriptContext = null;

        public boolean asPlatform = true;
        public float doorValue = 0;
        public boolean doorTarget = false;

        public final SerializableShape shape = new SerializableShape(this, "0, 0, 0, 16, 16, 16", Shapes.block());
        public final SerializableShape collision = new SerializableShape(this, "0, 0, 0, 0, 0, 0", Shapes.empty());

        public boolean fixedShape = true;
        public boolean fixedMatrix = false;
        public int lightLevel = 0;
        public boolean isTicketBarrier = false;
        public boolean isEntrance = false;

        public BlockEntityEyeCandy(BlockPos pos, BlockState state) {
            super(Main.BLOCK_ENTITY_TYPE_EYE_CANDY.get(), pos, state);
        }

        @Override
        public void readCompoundTag(CompoundTag compoundTag) {
            String id = compoundTag.getString("prefabId");
            if (StringUtils.isEmpty(id)) id = null;
            setPrefabId(id);
            fullLight = compoundTag.getBoolean("fullLight");
            try {
                byte[] dataBytes = compoundTag.getByteArray("data");
                data = Serializer.deserialize(dataBytes);
            }catch (IOException e) {
            }
            
            translateX = compoundTag.contains("translateX") ? compoundTag.getFloat("translateX") : 0;
            translateY = compoundTag.contains("translateY") ? compoundTag.getFloat("translateY") : 0;
            translateZ = compoundTag.contains("translateZ") ? compoundTag.getFloat("translateZ") : 0;
            rotateX = compoundTag.contains("rotateX") ? compoundTag.getFloat("rotateX") : 0;
            rotateY = compoundTag.contains("rotateY") ? compoundTag.getFloat("rotateY") : 0;
            rotateZ = compoundTag.contains("rotateZ") ? compoundTag.getFloat("rotateZ") : 0;
            asPlatform = compoundTag.contains("asPlatform") ? compoundTag.getBoolean("asPlatform") : true;
            // doorValue = compoundTag.contains("doorValue") ? compoundTag.getFloat("doorValue") : 0;
            // doorTarget = compoundTag.contains("doorTarget") ? compoundTag.getBoolean("doorTarget") : false;
            shape.readFrom(compoundTag, "shape");
            collision.readFrom(compoundTag, "collision");
            fixedShape = compoundTag.contains("fixedShape") ? compoundTag.getBoolean("fixedShape") : true;
            fixedMatrix = compoundTag.contains("fixedMatrix") ? compoundTag.getBoolean("fixedMatrix") : false;
            lightLevel = compoundTag.contains("lightLevel") ? compoundTag.getInt("lightLevel") : 0;
            isTicketBarrier = compoundTag.contains("isTicketBarrier") ? compoundTag.getBoolean("isTicketBarrier") : false;
            isEntrance = compoundTag.contains("isEntrance") ? compoundTag.getBoolean("isEntrance") : false;
        }

        @Override
        public void writeCompoundTag(CompoundTag compoundTag) {
            compoundTag.putString("prefabId", prefabId == null ? "" : prefabId);
            compoundTag.putBoolean("fullLight", fullLight);
            try {
                byte[] dataBytes = Serializer.serialize(data);
                compoundTag.putByteArray("data", dataBytes);
            }catch (IOException e) {
                compoundTag.putByteArray("data", new byte[0]);
            }
            
            compoundTag.putFloat("translateX", translateX);
            compoundTag.putFloat("translateY", translateY);
            compoundTag.putFloat("translateZ", translateZ);
            compoundTag.putFloat("rotateX", rotateX);
            compoundTag.putFloat("rotateY", rotateY);
            compoundTag.putFloat("rotateZ", rotateZ);
            compoundTag.putBoolean("asPlatform", asPlatform);
            // compoundTag.putFloat("doorValue", doorValue);
            // compoundTag.putBoolean("doorTarget", doorTarget);
            shape.writeTo(compoundTag, "shape");
            collision.writeTo(compoundTag, "collision");
            compoundTag.putBoolean("fixedShape", fixedShape);
            compoundTag.putBoolean("fixedMatrix", fixedMatrix);
            compoundTag.putInt("lightLevel", lightLevel);
            compoundTag.putBoolean("isTicketBarrier", isTicketBarrier);
            compoundTag.putBoolean("isEntrance", isEntrance);
        }

        public void setPrefabId(String prefabId) {
            if (this.prefabId == null || !this.prefabId.equals(prefabId)) {
                if (properties != null) {
                    if (properties.script != null) {
                        properties.script.tryCallDisposeFunctionAsync(scriptContext);
                    }
                }
                scriptContext = null;
                this.prefabId = prefabId;
                properties = EyeCandyRegistry.elements.get(prefabId);
                if (properties != null) {
                    shape.set(properties.shape);
                    collision.set(properties.collision);
                    fixedShape = properties.fixedShape;
                    fixedMatrix = properties.fixedMatrix;
                    lightLevel = properties.lightLevel;
                    data.clear();
                    isTicketBarrier = properties.isTicketBarrier;
                    isEntrance = properties.isEntrance;
                    if (properties.script != null) {
                        scriptContext = new EyeCandyScriptContext(this);
                    }
                }
            }
        }

        public BlockPos getWorldPos() {
            return this.worldPosition;
        }

        public Vector3f getWorldPosVector3f() {
            return new Vector3f(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ());
        }

        public Vector3f getTransformPosVector3f() {
            return new Vector3f(this.worldPosition.getX() + translateX, this.worldPosition.getY() + translateY, this.worldPosition.getZ() + translateZ);
        }

        public void sendUpdateC2S() {
            PacketUpdateBlockEntity.sendUpdateC2S(this);
        }

        public float getBlockYRot(){
            try {
                final Direction facing = IBlock.getStatePropertySafe(getBlockState(), FACING);
                return facing.toYRot();
            } catch (Exception e) {
                return 0;
            }
        }

        public synchronized Map<String, String> getData() {
            return data;
        }

        public void setDoorValue(float value) {
            doorValue = value;
        }

        public void setDoorTarget(boolean target) {
            doorTarget = target;
        }

        public boolean isPlatform() {
            return asPlatform;
        }

        public boolean isTicketBarrier() {
            return isTicketBarrier;
        }

        public boolean isOpened() {
            try {
                return getBlockState().getValue(BlockEyeCandy.OPEN).isOpen();
            } catch (Exception e) {
                Main.LOGGER.info("Error in isOpened:" + e.getMessage());
                return false;
            }
        }

        public void tryCallBeClickedFunctionAsync(Player player) {
            if (scriptContext == null) return;
            EyeCandyProperties prop = EyeCandyRegistry.elements.get(prefabId);
            if (prop == null) return;
            ScriptHolder scriptHolder = prop.script;
            if (scriptHolder == null) return;
            scriptHolder.tryCallBeClickedFunctionAsync(scriptContext, player);
        }
        
        public static class SerializableShape {
            private String shape;
            private VoxelShape voxelShape;
            private BlockEntityEyeCandy blockEntity;
            public final VoxelShape defaultShape;
            public final String defaultString;

            public SerializableShape(BlockEntityEyeCandy blockEntity, String defaultString, VoxelShape defaultShape) {
                this.blockEntity = blockEntity;
                this.shape = defaultString;
                this.defaultString = defaultString;
                this.defaultShape = defaultShape;
                this.voxelShape = defaultShape;
            }

            public boolean set(String shape) {
                if (shape.equals(this.shape)) {
                    return false;
                }
                this.shape = shape;
                this.voxelShape = getShapeIn();
                return true;
            }

            public VoxelShape get() {
                if (voxelShape == null) {
                    Main.LOGGER.error("VoxelShape is null for " + blockEntity.getWorldPos() + ", " + blockEntity.prefabId);
                    return defaultShape;
                }
                return voxelShape;
            }

            public void writeTo(CompoundTag tag, String key) {
                tag.putString(key, shape);
            }

            public void readFrom(CompoundTag tag, String key) {
                if (tag.contains(key)) {
                    set(tag.getString(key));
                }
            }

            public String toString() {
                return shape;
            }

            private VoxelShape getShapeIn() {
                try {
                    if (shape == null || shape.isEmpty()) {
                        return getDefaultShape();
                    }

                    String[] shapeArray = shape.split("/");
                    VoxelShape[] voxelShapes = new VoxelShape[shapeArray.length];

                    for (int i = 0; i < shapeArray.length; i++) {
                        String[] posArray = shapeArray[i].split(",");
                        
                        if (posArray.length != 6) {
                            return handleInvalidShape();
                        }

                        Double[] pos = parsePositions(posArray);
                        if (pos == null) {
                            return handleInvalidShape();
                        }

                        Double[] rotatedPos = applyRotation(pos);
                        VoxelShape voxelShape = Block.box(rotatedPos[0], rotatedPos[1], rotatedPos[2], rotatedPos[3], rotatedPos[4], rotatedPos[5]);

                        voxelShapes[i] = voxelShape;
                    }

                    return combineShapes(voxelShapes);
                } catch (Exception e) {
                    Main.LOGGER.error("Error in getShape: " + e.getMessage(), e);
                    return defaultShape;
                }
            }

            private VoxelShape getDefaultShape() {
                return defaultShape;
            }

            private VoxelShape handleInvalidShape() {
                Main.LOGGER.error("Invalid shape: " + shape);
                shape = defaultString;
                blockEntity.sendUpdateC2S();
                return defaultShape;
            }

            private Double[] parsePositions(String[] posArray) {
                Double[] pos = new Double[6];
                try {
                    for (int j = 0; j < posArray.length; j++) {
                        pos[j] = Double.parseDouble(posArray[j].trim());
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
                return pos;
            }

            private Double[] applyRotation(Double[] pos) {
                int yRot = (int) blockEntity.getBlockYRot();
                double x1 = pos[0], y1 = pos[1], z1 = pos[2], x2 = pos[3], y2 = pos[4], z2 = pos[5];

                switch (yRot) {
                    case 90:
                        return new Double[]{16 - z2, y1, x1, 16 - z1, y2, x2};
                    case 180:
                        return new Double[]{16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1};
                    case 270:
                        return new Double[]{z1, y1, 16 - x2, z2, y2, 16 - x1};
                    default:
                        return new Double[]{x1, y1, z1, x2, y2, z2};
                }
            }

            private VoxelShape combineShapes(VoxelShape[] voxelShapes) {
                VoxelShape finalShape = voxelShapes[0];
                for (int i = 1; i < voxelShapes.length; i++) {
                    if (voxelShapes[i] == null || finalShape == null) {
                        return handleInvalidShape();
                    }
                    finalShape = Shapes.or(finalShape, voxelShapes[i]);
                }
                if (!blockEntity.fixedShape) {
                    finalShape = finalShape.move(blockEntity.translateX, blockEntity.translateY, blockEntity.translateZ);
                }
                return finalShape;
            }
        }
    }
}
