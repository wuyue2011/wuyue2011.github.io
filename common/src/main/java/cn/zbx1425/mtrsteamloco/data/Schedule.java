package cn.zbx1425.mtrsteamloco.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Schedule {
    public final long arrivalMillis;
	public final int trainCars;
	public final long routeId;
	public final int currentStationIndex;
    public final long arrivalDiffMillis;

    public Schedule(long arrivalMillis, int trainCars, long routeId, int currentStationIndex, long arrivalDiffMillis, int dwellTime, String name) {
        this.arrivalMillis = arrivalMillis;
        this.trainCars = trainCars;
        this.routeId = routeId;
        this.currentStationIndex = currentStationIndex;
        this.arrivalDiffMillis = arrivalDiffMillis;
        this.dwellTime = dwellTime;
        this.name = name;
    }
}