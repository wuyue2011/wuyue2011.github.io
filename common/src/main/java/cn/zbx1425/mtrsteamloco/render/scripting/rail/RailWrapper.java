package cn.zbx1425.mtrsteamloco.render.scripting.rail;

import mtr.data.*;
import cn.zbx1425.mtrsteamloco.data.RailExtraSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateRail;
import com.mojang.math.Vector3f;

import java.util.Map;

public class RailWrapper{
    private final Rail rail;
    private final RailExtraSupplier supplier;
    public final RailType railType;
	public final TransportMode transportMode;
	public final RailAngle facingStart;
	public final RailAngle facingEnd;

    public RailWrapper(Rail rail) {
        this.rail = rail;
        this.supplier = (RailExtraSupplier) rail;
        this.railType = rail.railType;
        this.transportMode = rail.transportMode;
        this.facingStart = rail.facingStart;
        this.facingEnd = rail.facingEnd;
    }

    public Map<String, String> getCustomConfigs() {
        return supplier.getCustomConfigs();
    }

    public void setCustomConfigs(Map<String, String> customConfigs) {
        supplier.setCustomConfigs(customConfigs);
    }

    public Map<Double, Float> getRollAngleMap() {
        return supplier.getRollAngleMap();
    }

    public void setRollAngleMap(Map<Double, Float> rollAngleMap) {
        supplier.setRollAngleMap(rollAngleMap);
    }

    public void sendUpdateC2S() {
        double length = rail.getLength();
        BlockPos pos0 = new BlockPos(rail.getPosition(0).toBlockPos());
        BlockPos pos1 = new BlockPos(rail.getPosition(length).toBlockPos());
        PacketUpdateRail.sendUpdateC2S(rail, pos0, pos1);
    }

    public double getLength() {
        return rail.getLength();
    }

    public Vector3f getPosition(double value) {
        return new Vector3f(rail.getPosition(value));
    }

    public void render(Rail.RenderRail callback, float offsetRadius1, float offsetRadius2) {
        rail.render(callback, offsetRadius1, offsetRadius2);
    }

    public float getRollAngle(double value) {
        return RailExtraSupplier.getRollAngle(rail, value);
    }
}