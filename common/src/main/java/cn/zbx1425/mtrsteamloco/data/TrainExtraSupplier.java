package cn.zbx1425.mtrsteamloco.data;

import mtr.data.Rail;
import mtr.data.Train;

import java.util.Map;

public interface TrainExtraSupplier {
    
    Map<String, String> getCustomConfigs();

    void setCustomConfigs(Map<String, String> customConfigs);

    boolean isConfigsChanged();
    
    void isConfigsChanged(boolean isConfigsChanged);

    Map<String, ConfigResponder> getConfigResponders();

    void setConfigResponders(Map<String, ConfigResponder> configResponders);

    float getRollAngleAt(double rv);

    static float getRollAngleAt(Train train, int car) {
        double p = train.getRailProgress() - car * train.spacing - train.spacing / 2;
        return ((TrainExtraSupplier) train).getRollAngleAt(p);
        // double p1 = train.getRailProgress() - car * train.spacing;
        // double p2 = p1 - train.spacing;
        // TrainExtraSupplier supplier = (TrainExtraSupplier) train;
        // return (supplier.getRollAngleAt(p1) + supplier.getRollAngleAt(p2)) / 2;
    }
}