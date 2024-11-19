package cn.zbx1425.mtrsteamloco.render.scripting;

import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.Scriptable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractScriptContext {

    public Scriptable state;
    public boolean created = false;
    public Future<?> scriptStatus;
    public double lastExecuteTime = 0;

    public boolean disposed = false;

    public long lastExecuteDuration = 0;
    public Map<String, Object> debugInfo = new LinkedHashMap<>();

    public abstract void renderFunctionFinished();

    public abstract Object getWrapperObject();

    public abstract boolean isBearerAlive();

    public abstract boolean isTrain();

    public synchronized Map<String, Object> getDebugInfo() {
        synchronized (debugInfo) {
            return new LinkedHashMap<>(debugInfo);
        }
    }

    public synchronized void setDebugInfo(String key, Object value) {
        synchronized (debugInfo) {
            debugInfo.put(key, value);
        }
    }

    public synchronized void setDebugInfo(String key, Object... values) {
        synchronized (debugInfo) {
            List<Object> list = new ArrayList<>();
            for (Object value : values) {
                list.add(value);
            }
            debugInfo.put(key, list);
        }
    }

    public synchronized void clearDebugInfo() {
        synchronized (debugInfo) {
            debugInfo.clear();
        }
    }

    public synchronized Scriptable getState() {
        return state;
    }

    public synchronized void setState(Scriptable state) {
        this.state = state;
    }
}
