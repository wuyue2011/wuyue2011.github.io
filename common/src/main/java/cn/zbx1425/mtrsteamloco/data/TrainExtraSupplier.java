package cn.zbx1425.mtrsteamloco.data;

import java.util.Map;

public interface TrainExtraSupplier {
    
    Map<String, String> getExtraData();

    void setExtraData(Map<String, String> extraData);
}