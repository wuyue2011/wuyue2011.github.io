package cn.zbx1425.mtrsteamloco.network.util;

import cn.zbx1425.mtrsteamloco.data.Schedule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Serializer {

    public static byte[] serialize(Map<String, String> map) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(map.size());

        for (Map.Entry<String, String> entry : map.entrySet()) {
            dos.writeUTF(entry.getKey());
            dos.writeUTF(entry.getValue());
        }

        dos.flush();
        return baos.toByteArray();
    }

    public static Map<String, String> deserialize(byte[] bytes) throws IOException {
        Map<String, String> map = new HashMap<>();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);

        int size = dis.readInt();

        for (int i = 0; i < size; i++) {
            String key = dis.readUTF();
            String value = dis.readUTF();
            map.put(key, value);
        }

        dis.close();
        return map;
    }

    public static byte[] serialize(List<Schedule> list) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(list.size());

        for (Schedule entry : list) {
            dos.writeLong(entry.arrivalMillis);
            dos.writeInt(entry.trainCars);
            dos.writeLong(entry.routeId);
            dos.writeInt(entry.currentStationIndex);
            dos.writeLong(entry.arrivalDiffMillis);
        }

        dos.flush();
        return baos.toByteArray();
    }

    public static List<Schedule> deserialize(byte[] bytes, boolean a) throws IOException {
        List<Schedule> list = new ArrayList<>();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);

        int size = dis.readInt();

        for (int i = 0; i < size; i++) {
            long arrivalMillis = dis.readLong();
            int trainCars = dis.readInt();
            long routeId = dis.readLong();
            int currentStationIndex = dis.readInt();
            long arrivalDiffMillis = dis.readLong();
            list.add(new Schedule(arrivalMillis, trainCars, routeId, currentStationIndex, arrivalDiffMillis));
        }

        dis.close();
        return list;
    }

    public static byte[] serialize(Map<Long, List<Schedule>> map) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(map.size());

        for (Map.Entry<Long, List<Schedule>> entry : map.entrySet()) {
            dos.writeLong(entry.getKey());
            byte[] valueBytes = serialize(entry.getValue());
            dos.writeInt(valueBytes.length);
            dos.write(valueBytes);
        }

        dos.flush();
        return baos.toByteArray();
    }

    public static Map<Long, List<Schedule>> deserialize(byte[] bytes, int a) throws IOException {
        Map<Long, List<Schedule>> map = new HashMap<>();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);

        int size = dis.readInt();

        for (int i = 0; i < size; i++) {
            long key = dis.readLong();
            byte[] valueBytes = new byte[dis.readInt()];
            dis.read(valueBytes);
            List<Schedule> value = deserialize(valueBytes, true);
            map.put(key, value);
        }

        dis.close();
        return map;
    }
}