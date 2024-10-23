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
import mtr.block.BlockNode;
import net.minecraft.client.Minecraft;
import mtr.MTRClient;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlockEyeCandy extends BlockDirectionalMapper implements EntityBlockMapper {

    public static final IntegerProperty KEY = IntegerProperty.create("key", 0, Integer.MAX_VALUE);

    public BlockEyeCandy() {
        super(
#if MC_VERSION < "12000"
                BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_GRAY)
#else
                BlockBehaviour.Properties.of()
#endif
                        .strength(2)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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
        return RenderShape.INVISIBLE;
    }

    /*public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos) {
        final BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof BlockEntityEyeCandy) {
            return ((BlockEyeCandy.BlockEntityEyeCandy) entity).getShape();
        }else {
            return Block.box(3, 8, 3, 13, 16, 13);
        }
    }*/
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        try {
            int shape = Integer.valueOf(state.getValue(KEY)).intValue();
            return Block.box(shape >>> 25 & 0x1F, shape >>> 20 & 0x1F, shape >>> 15 & 0x1F, shape >>> 10 & 0x1F, shape >>> 5 & 0x1F, shape >>> 0 & 0x1F);
        } catch (NumberFormatException e) {
            return Block.box(3, 8, 3, 13, 13, 13);
        }
    }

    /*@Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        return getShape(state, blockGetter, pos);
    }*/

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

        protected String shape = "0, 0, 0, 16, 16, 16";
        public boolean isEmpty = false;

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
            isEmpty = compoundTag.contains("isEmpty") ? compoundTag.getBoolean("isEmpty") : true;
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
            compoundTag.putBoolean("isEmpty", isEmpty);
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
            final Direction facing = IBlock.getStatePropertySafe(Minecraft.getInstance().level.getBlockState(this.worldPosition), HorizontalDirectionalBlock.FACING);
            return facing.toYRot();
        }

        public Vector3f getNodePos(Vector3f vPos, Float fFacing) {
            BlockPos pos = vPos.toBlockPos();
            Direction facing = Direction.fromYRot(fFacing);
            BlockGetter world = Minecraft.getInstance().level;
		    final int[] checkDistance = {0, 1, -1, 2, -2, 3, -3, 4, -4};
		    for (final int z : checkDistance) {
		    	for (final int x : checkDistance) {
		    		for (int y = -5; y <= 0; y++) {
		    			final BlockPos checkPos = pos.above(y).relative(facing.getClockWise(), x).relative(facing, z);
		    			final BlockState checkState = world.getBlockState(checkPos);
		    			if (checkState.getBlock() instanceof BlockNode) {
		    				return new Vector3f(checkPos);
		    			}
		    		}
		    	}
		    }
		    return null;
	    }

        public Map<String, String> getData() {
            return data;
        }

        public void setDoorValue(float value) {
            doorValue = value;
            sendUpdateC2S();
        }

        public void setDoorTarget(boolean target) {
            doorTarget = target;
            sendUpdateC2S();
        }

        public boolean isOpen() {
            return doorValue > 0;
        }

        public boolean isPlatform() {
            return platform;
        }

        public void setShape(int shape) {
            getBlockState().setValue(BlockEyeCandy.KEY, Integer.valueOf(shape));
        }

        public VoxelShape getShape() {
            if (isEmpty) {
                return Shapes.block();
            } else {
                String[] shapeArray = shape.split("/");
                VoxelShape[] voxelShapes= new VoxelShape[shapeArray.length];
                for (int i = 0; i < shapeArray.length; i++) {
                    String[] posArray = shapeArray[i].split(",");
                    if (posArray.length!= 6) {
                        shape = "0, 0, 0, 16, 16, 16";
                        sendUpdateC2S();
                        return Shapes.block();
                    }
                    Double[] pos = new Double[posArray.length];
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
                        voxelShapes[i] = Block.box(pos[0], pos[1], pos[2], pos[3], pos[4], pos[5]);
                    } catch (Exception e) {
                        shape = "0, 0, 0, 16, 16, 16";
                        sendUpdateC2S();
                        return Shapes.block();
                    }
                }
                return Shapes.or(Shapes.empty(), voxelShapes);
            }
        }
    }
}
