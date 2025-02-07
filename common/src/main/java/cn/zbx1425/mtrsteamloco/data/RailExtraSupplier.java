package cn.zbx1425.mtrsteamloco.data;

import mtr.data.Rail;
import net.minecraft.util.Mth;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public interface RailExtraSupplier {

    // "": default, "null": hidden
    String getModelKey();

    void setModelKey(String key);

    boolean getRenderReversed();

    void setRenderReversed(boolean value);

    float getVerticalCurveRadius();

    void setVerticalCurveRadius(float value);

    int getHeight();

    Map<Double, Float> getRollAngleMap();

    void setRollAngleMap(Map<Double, Float> rollAngleMap);

    Map<String, String> getCustomConfigs();
    
    void setCustomConfigs(Map<String, String> customConfigs);

    Map<String, ConfigResponder> getCustomResponders();

    void setCustomResponders(Map<String, ConfigResponder> customResponders);

    static float getVTheta(Rail rail, double verticalCurveRadius) {
        double H = Math.abs(((RailExtraSupplier) rail).getHeight());
        double L = rail.getLength();
        double R = verticalCurveRadius;
        return 2 * (float) Mth.atan2(Math.sqrt(H * H - 4 * R * H + L * L) - L, H - 4 * R);
    }

    static float getRollAngle(Rail rail, double value) {
        Map<Double, Float> rollAngleMap = ((RailExtraSupplier) rail).getRollAngleMap();
        boolean reversed = ((RailExtraSupplier) rail).getRenderReversed();
        if (reversed) {
            value = rail.getLength() - value;
        }
        if (rollAngleMap.isEmpty()) {
            return 0;
        }
        List<Double> keys = new ArrayList<>(rollAngleMap.keySet());
        keys.sort(Double::compareTo);
        if (value <= keys.get(0)) {
            return rollAngleMap.get(keys.get(0));
        }
        int size = keys.size();
        if (value >= keys.get(size - 1)) {
            return rollAngleMap.get(keys.get(size - 1));
        }
        for (int i = 0; i < size; i++) {
            double t0 = keys.get(i);
            if (value >= t0) {
                double t1 = keys.get(i + 1);
                float a0 = rollAngleMap.get(t0);
                float a1 = rollAngleMap.get(t1);
                return a0 + (a1 - a0) * (float) ((value - t0) / (t1 - t0));
            } 
        }
        return 0;
    }
}
