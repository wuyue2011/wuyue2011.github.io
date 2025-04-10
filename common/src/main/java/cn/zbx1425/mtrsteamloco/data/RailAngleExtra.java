package cn.zbx1425.mtrsteamloco.data;

import mtr.data.RailAngle;
import cn.zbx1425.mtrsteamloco.Main;

public interface RailAngleExtra {
    RailAngle _fromDegrees(float degrees);

    static RailAngle fromDegrees(float degrees) {
        RailAngle result = ((RailAngleExtra) (Object) RailAngle.S)._fromDegrees(degrees);
        return result;
    }
}