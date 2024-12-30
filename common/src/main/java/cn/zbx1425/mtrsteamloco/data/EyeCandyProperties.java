package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import cn.zbx1425.sowcerext.model.ModelCluster;
import net.minecraft.network.chat.Component;
import mtr.mappings.Text;

import java.io.Closeable;
import java.io.IOException;

public class EyeCandyProperties implements Closeable {

    public Component name;

    public ModelCluster model;
    public ScriptHolder script;
    public String shape;
    public boolean noCollision;
    public boolean fixedShape;
    public boolean fixedMatrix;
    public int lightLevel;
    public boolean isTicketBarrier;
    public boolean isEntrance;

    public EyeCandyProperties(Component name, ModelCluster model, ScriptHolder script, String shape, boolean noCollision, boolean fixedShape, boolean fixedMatrix, int lightLevel, boolean isTicketBarrier, boolean isEntrance) {
        this.name = name;
        this.model = model;
        this.script = script;
        this.shape = shape;
        this.noCollision = noCollision;
        this.fixedShape = fixedShape;
        this.fixedMatrix = fixedMatrix;
        this.lightLevel = lightLevel;
        this.isTicketBarrier = isTicketBarrier;
        this.isEntrance = isEntrance;
    }

    @Override
    public void close() throws IOException {
        if (model != null) model.close();
    }
}
