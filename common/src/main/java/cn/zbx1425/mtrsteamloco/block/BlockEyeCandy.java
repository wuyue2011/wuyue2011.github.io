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

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public class BlockEyeCandy extends BlockDirectionalMapper implements EntityBlockMapper {
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
        builder.add(FACING, LEVEL);
    }

    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.getMainHandItem().is(mtr.Items.BRUSH.get())) {
            if (!level.isClientSide) {
                PacketScreen.sendScreenBlockS2C((ServerPlayer) player, "eye_candy", pos);
            }
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
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
    
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos) {
        BlockEntity entity = world.getBlockEntity(pos);
        VoxelShape shape = Block.box(0, 0, 0, 16, 24, 16);
        if (entity instanceof BlockEntityEyeCandy) {
            shape = ((BlockEntityEyeCandy) entity).getShape();
        } else {
            Main.LOGGER.warn("BlockEntityEyeCandy not found at " + pos + ", " + world);
        }
        return shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext collisionContext) {
        VoxelShape shape = getShape(state, world, pos);
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext collisionContext) {
        final BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof BlockEntityEyeCandy) {
            if (((BlockEyeCandy.BlockEntityEyeCandy) entity).noCollision) {
                return Shapes.empty();
            } else {
                return getShape(state, world, pos);
            }
        }
        return getShape(state, world, pos);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return Shapes.empty();
    }

    public static class BlockEntityEyeCandy extends BlockEntityClientSerializableMapper {

        public String prefabId = null;

        public float translateX = 0, translateY = 0, translateZ = 0;
        public float rotateX = 0, rotateY = 0, rotateZ = 0;

        public boolean fullLight = false;

        public Map<String, String> data = new HashMap<>();

        public EyeCandyScriptContext scriptContext = new EyeCandyScriptContext(this);

        public boolean platform = true;
        public float doorValue = 0;
        public boolean doorTarget = false;

        public String shape = "0, 0, 0, 16, 16, 16";
        public boolean noCollision = true;
        public boolean noMove = true;
        public int lightLevel = 0;

        public BlockEntityEyeCandy(BlockPos pos, BlockState state) {
            super(Main.BLOCK_ENTITY_TYPE_EYE_CANDY.get(), pos, state);
        }

        @Override
        public void readCompoundTag(CompoundTag compoundTag) {
            prefabId = compoundTag.getString("prefabId");
            if (StringUtils.isEmpty(prefabId)) prefabId = null;
            fullLight = compoundTag.getBoolean("fullLight");
            try {
                byte[] dataBytes = compoundTag.getByteArray("data");
                data = Serializer.deserialize(dataBytes);
            }catch (IOException e) {
                data = new HashMap<String, String>();
            }
            
            translateX = compoundTag.contains("translateX") ? compoundTag.getFloat("translateX") : 0;
            translateY = compoundTag.contains("translateY") ? compoundTag.getFloat("translateY") : 0;
            translateZ = compoundTag.contains("translateZ") ? compoundTag.getFloat("translateZ") : 0;
            rotateX = compoundTag.contains("rotateX") ? compoundTag.getFloat("rotateX") : 0;
            rotateY = compoundTag.contains("rotateY") ? compoundTag.getFloat("rotateY") : 0;
            rotateZ = compoundTag.contains("rotateZ") ? compoundTag.getFloat("rotateZ") : 0;
            platform = compoundTag.contains("platform") ? compoundTag.getBoolean("platform") : true;
            doorValue = compoundTag.contains("doorValue") ? compoundTag.getFloat("doorValue") : 0;
            doorTarget = compoundTag.contains("doorTarget") ? compoundTag.getBoolean("doorTarget") : false;
            shape = compoundTag.contains("shape") ? compoundTag.getString("shape") : "0, 0, 0, 16, 16, 16";
            noCollision = compoundTag.contains("noCollision") ? compoundTag.getBoolean("noCollision") : true;
            noMove = compoundTag.contains("noMove") ? compoundTag.getBoolean("noMove") : true;
            lightLevel = compoundTag.contains("lightLevel") ? compoundTag.getInt("lightLevel") : 0;
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
            compoundTag.putBoolean("platform", platform);
            compoundTag.putFloat("doorValue", doorValue);
            compoundTag.putBoolean("doorTarget", doorTarget);
            compoundTag.putString("shape", shape);
            compoundTag.putBoolean("noCollision", noCollision);
            compoundTag.putBoolean("noMove", noMove);
            compoundTag.putInt("lightLevel", lightLevel);
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

        public boolean isOpen() {
            return doorValue > 0;
        }

        public boolean isPlatform() {
            return platform;
        }

        public void setShape(String shape) {
            this.shape = shape;
            getShape();
        }

        public VoxelShape getShape() {
            try {
                if (false) return Block.box(0, 0, 0, 16, 24, 16);
                String[] shapeArray = shape.split("/");
                VoxelShape[] voxelShapes= new VoxelShape[shapeArray.length];
                for (int i = 0; i < shapeArray.length; i++) {
                    String[] posArray = shapeArray[i].split(",");
                    if (posArray.length!= 6) {
                        shape = "0, 0, 0, 16, 16, 16";
                        sendUpdateC2S();
                        return Shapes.block();
                    }
                    Double[] pos = new Double[6];
                    try {
                        for (int j = 0; j < posArray.length; j++) {
                            pos[j] = Double.parseDouble(posArray[j]);
                        }
                    } catch (NumberFormatException e) {
                        shape = "0, 0, 0, 16, 16, 16";
                        sendUpdateC2S();
                        return Shapes.block();
                    }
                    try {
                        Double x1 = pos[0], y1 = pos[1], z1 = pos[2], x2 = pos[3], y2 = pos[4], z2 = pos[5];
                        Double[] newPos = null;
                        int yRot = (int)getBlockYRot();
                        switch (yRot) {
                            case 0: {
                                newPos = new Double[]{x1, y1, z1, x2, y2, z2};
                                break;
                            }
                            case 90: {
                                newPos = new Double[]{16 - z2, y1, x1, 16 - z1, y2, x2};
                                break;
                            }
                            case 180: {
                                newPos = new Double[]{16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1};
                                break;
                            }
                            case 270: {
                                newPos = new Double[]{z1, y1, 16 - x2, z2, y2, 16 - x1};
                                break;
                            }
                            default: {
                                newPos = new Double[]{x1, y1, z1, x2, y2, z2};
                                break;
                            }
                        }
                        VoxelShape voxelShape = Block.box(newPos[0], newPos[1], newPos[2], newPos[3], newPos[4], newPos[5]);
                        if (!noMove) {
                            double tx = (double)translateX, ty = (double)translateY, tz = (double)translateZ;
                            voxelShapes[i] = voxelShape.move(tx, ty, tz);
                        }
                    } catch (IllegalArgumentException e) {
                        shape = "0, 0, 0, 16, 16, 16";
                        sendUpdateC2S();
                        return Shapes.block();
                    }
                }
                return Shapes.or(Shapes.empty(), voxelShapes);
            } catch (Exception e) {
                Main.LOGGER.error("Error in getShape:" + e.getMessage());
                return Shapes.block();
            }
        }
    }
}
