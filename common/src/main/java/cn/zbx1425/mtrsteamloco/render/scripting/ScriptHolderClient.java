package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.scripting.util.client.*;
import cn.zbx1425.sowcer.math.Matrices;
import mtr.mappings.UtilitiesClient;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.RawMesh;
import mtr.client.IDrawing;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.integration.RawMeshBuilder;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import mtr.client.ClientData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.*;
import mtr.block.IBlock;
import net.minecraft.world.entity.player.Player;
import cn.zbx1425.mtrsteamloco.render.scripting.util.WrappedEntity;
import cn.zbx1425.mtrsteamloco.render.scripting.AbstractDrawCalls;
import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.data.ShapeSerializer;
import cn.zbx1425.mtrsteamloco.data.ConfigResponder;
import net.minecraft.network.chat.Component;
import cn.zbx1425.mtrsteamloco.render.scripting.rail.RailDrawCalls.*;
import com.google.gson.JsonObject;
import cn.zbx1425.mtrsteamloco.CustomResources;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScriptHolderClient extends ScriptHolderBase {
    public ScriptHolderClient() {
        super("client");
    }

    protected void appendImporter(Scriptable scope, Context rhinoCtx) {
        scope.put("Resources", scope, new NativeJavaClass(scope, ScriptResourceUtilClient.class));
        scope.put("GraphicsTexture", scope, new NativeJavaClass(scope, GraphicsTexture.class));
        scope.put("SoundHelper", scope, new NativeJavaClass(scope, SoundHelper.class));
        scope.put("ParticleHelper", scope, new NativeJavaClass(scope, ParticleHelper.class));
        scope.put("TickableSound", scope, new NativeJavaClass(scope, TickableSound.class));
        scope.put("IScreen", scope, new NativeJavaClass(scope, IScreen.class));
        scope.put("ConfigResponder", scope, new NativeJavaClass(scope, ConfigResponder.class));
        scope.put("ClientConfig", scope, new NativeJavaClass(scope, ClientConfig.class));
        scope.put("MinecraftClient", scope, new NativeJavaClass(scope, MinecraftClientUtil.class));

        scope.put("DrawCall", scope, new NativeJavaClass(scope, AbstractDrawCalls.DrawCall.class));
        scope.put("ClusterDrawCall", scope, new NativeJavaClass(scope, AbstractDrawCalls.ClusterDrawCall.class));
        scope.put("WorldDrawCall", scope, new NativeJavaClass(scope, AbstractDrawCalls.WorldDrawCall.class));
        scope.put("RailDrawCall", scope, new NativeJavaClass(scope, RailDrawCall.class));
        scope.put("SimpleRailDrawCall", scope, new NativeJavaClass(scope, SimpleRailDrawCall.class));

        scope.put("ModelManager", scope, Context.toObject(MainClient.modelManager, scope));
        scope.put("RawModel", scope, new NativeJavaClass(scope, RawModel.class));
        scope.put("RawMesh", scope, new NativeJavaClass(scope, RawMesh.class));
        scope.put("RawMeshBuilder", scope, new NativeJavaClass(scope, RawMeshBuilder.class));
        scope.put("ModelCluster", scope, new NativeJavaClass(scope, ModelCluster.class));
        scope.put("DynamicModelHolder", scope, new NativeJavaClass(scope, DynamicModelHolder.class));

        scope.put("MTRClientData", scope, new NativeJavaClass(scope, ClientData.class));
        scope.put("IBlock", scope, new NativeJavaClass(scope, IBlock.class));
        scope.put("UtilitiesClient", scope, new NativeJavaClass(scope, UtilitiesClient.class));
        scope.put("IDrawing", scope, new NativeJavaClass(scope, IDrawing.class));

        try {
            String[] classesToLoad = {
                    "util.AddParticleHelper",
                    "particle.MadParticleOption",
                    "particle.SpriteFrom",
                    "command.inheritable.InheritableBoolean",
                    "particle.ParticleRenderTypes",
                    "particle.ChangeMode"
            };
            for (String classToLoad : classesToLoad) {
                Class<?> classToLoadClass = Class.forName("cn.ussshenzhou.madparticle." + classToLoad);
                scope.put(classToLoad.substring(classToLoad.lastIndexOf(".") + 1), scope,
                        new NativeJavaClass(scope, classToLoadClass));
            }
            scope.put("foundMadParticle", scope, true);
        } catch (ClassNotFoundException ignored) {
            // Main.LOGGER.warn("MadParticle", ignored);
            scope.put("foundMadParticle", scope, false);
        }
    }
}
