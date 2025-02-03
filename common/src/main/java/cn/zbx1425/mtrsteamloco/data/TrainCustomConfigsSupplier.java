package cn.zbx1425.mtrsteamloco.data;

import java.util.Map;

public interface TrainCustomConfigsSupplier {
    
    Map<String, String> getCustomConfigs();

    void setCustomConfigs(Map<String, String> customConfigs);

    boolean isConfigsChanged();
    
    void isConfigsChanged(boolean isConfigsChanged);

    Map<String, ConfigResponder> getConfigResponders();

    void setConfigResponders(Map<String, ConfigResponder> configResponders);
}