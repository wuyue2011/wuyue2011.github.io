package cn.zbx1425.mtrsteamloco.render.scripting;

import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.Scriptable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public abstract class AbstractScriptContext {

    public Scriptable state;
    protected boolean created = false;
    public Future<?> scriptStatus;
    public double lastExecuteTime = 0;

    protected boolean disposed = false;

    public long lastExecuteDuration = 0;
    public Map<String, Object> debugInfo = new HashMap<>();

    public abstract void renderFunctionFinished();

    public abstract Object getWrapperObject();

    public abstract boolean isBearerAlive();

    public synchronized Map<String, Object> getDebugInfo() {
        synchronized (debugInfo) {
            return new HashMap<>(debugInfo);
        }
    }

    public synchronized void setDebugInfo(String key, Object value) {
        synchronized (debugInfo) {
            debugInfo.put(key, value);
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
