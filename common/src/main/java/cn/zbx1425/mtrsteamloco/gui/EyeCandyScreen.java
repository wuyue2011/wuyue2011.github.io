package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy.BlockEntityEyeCandy;
import cn.zbx1425.mtrsteamloco.data.EyeCandyProperties;
import cn.zbx1425.mtrsteamloco.data.EyeCandyRegistry;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import mtr.client.IDrawing;
import mtr.mappings.Text;
import mtr.mappings.UtilitiesClient;
import mtr.screen.WidgetBetterCheckbox;
import mtr.screen.WidgetBetterTextField;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
#if MC_VERSION >= "12000"
import net.minecraft.client.gui.GuiGraphics;
#endif
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import cn.zbx1425.mtrsteamloco.render.scripting.eyecandy.EyeCandyScriptContext;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import net.minecraft.client.gui.screens.Screen;
import cn.zbx1425.mtrsteamloco.Main;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public class EyeCandyScreen {

    private final BlockPos blockPos;
    private final List<Consumer<BlockEntityEyeCandy>> updateBlockEntityCallbacks = new ArrayList<>();
    public Screen screen;
    private final BlockEntityEyeCandy blockEntity;

    public EyeCandyScreen(BlockPos blockPos) {
        this.blockPos = blockPos;
        Optional<BlockEntityEyeCandy> optionalBlockEntity = getBlockEntity();
        if (!optionalBlockEntity.isEmpty()) {
            blockEntity = optionalBlockEntity.get();
        } else {
            Main.LOGGER.error("Cannot find block entity at " + blockPos);
            blockEntity = null;
        }
    }

    public void setScreen() {
        Minecraft.getInstance().setScreen(createScreen());
    }

    public Screen createScreen() {
        if (blockEntity == null) {
            return null;
        }

        EyeCandyProperties properties = EyeCandyRegistry.elements.get(blockEntity.prefabId);
        String pid = "";
        if (properties != null) {
            pid = properties.name.getString() + " (" + blockEntity.prefabId + ")";
        }

        Set<Map.Entry<String, EyeCandyProperties>> entries = EyeCandyRegistry.elements.entrySet();
        Map<String, String> elementMap = new HashMap<>();
        for (Map.Entry<String, EyeCandyProperties> entry : entries) {
            EyeCandyProperties prop = entry.getValue();
            String prid = entry.getKey();
            String name = prop.name.getString();
            elementMap.put(name + " (" + prid + ")", prid);
        }
        List<String> elementList = new ArrayList<>(elementMap.keySet());
        Collections.sort(elementList);

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(null)
                .setTitle(Text.literal("装饰物件设置"))
                .setDoesConfirmSave(false)
                .transparentBackground();
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory common = builder.getOrCreateCategory(
                Text.translatable("gui.mtrsteamloco.config.client.category.common")
        );

        common.addEntry(entryBuilder.startTextDescription(
                Text.literal("当前模型: " + (properties != null ? properties.name.getString() : blockEntity.prefabId + " (???)"))
        ).build());

        common.addEntry(entryBuilder.startDropdownMenu(
            Text.literal("选择模型"),
            DropdownMenuBuilder.TopCellElementBuilder.of(pid, str -> str))
            .setDefaultValue(pid).setSelections(elementList).setSaveConsumer(btnKey -> {
                updateBlockEntity((blockEntity) -> {
                    blockEntity.setPrefabId(elementMap.get(btnKey));
                });
            }).build()
        );

        common.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.eye_candy.full_light"),
                        blockEntity.fullLight
                ).setSaveConsumer(checked -> {
                    if (checked != blockEntity.fullLight) {
                        updateBlockEntity(be -> be.fullLight = checked);
                    }
                }).setDefaultValue(blockEntity.fullLight).build()
        );

        common.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.literal("当作站台"),
                        blockEntity.bePlatform
                ).setSaveConsumer(checked -> {
                    if (checked != blockEntity.bePlatform) {
                        updateBlockEntity(be -> be.bePlatform = checked);
                    }
                }).setDefaultValue(blockEntity.bePlatform).build()
        );

        if (blockEntity.fixedMatrix) {
            common.addEntry(entryBuilder.startTextDescription(
                    Text.literal("模型位置已固定，无法编辑")
            ).build());

            common.addEntry(entryBuilder.startTextDescription(
                    Text.literal("TX: " + blockEntity.translateX * 100 + "cm, TY: " + blockEntity.translateY * 100 + "cm, TZ: " + blockEntity.translateZ * 100 + "cm")
            ).build());

            common.addEntry(entryBuilder.startTextDescription(
                    Text.literal("RX: " + Math.toDegrees(blockEntity.rotateX) + "°, RY: " + Math.toDegrees(blockEntity.rotateY) + "°, RZ: " + Math.toDegrees(blockEntity.rotateZ) + "°")
            ).build());
        } else {
            StringListEntry tx = entryBuilder.startTextField(
                            Text.literal("TX"),
                            blockEntity.translateX * 100 + "cm"
                ).setSaveConsumer(str -> {
                    try {
                        str = str.toLowerCase().trim();
                        Float value = 0f;
                        if (str.endsWith("cm")) {
                            value = Float.parseFloat(str.substring(0, str.length() - 2)) / 100;
                        } else if (str.endsWith("m")) {
                            value = Float.parseFloat(str.substring(0, str.length() - 1));
                        } else {
                            value = Float.parseFloat(str);
                        }
                        if (value != blockEntity.translateX) {
                            final float v = value;
                            updateBlockEntity(be -> be.translateX = v);
                        }
                    } catch (NumberFormatException e) {
                        // tx.setValue(blockEntity.translateX + "cm");
                    }  
                }).setDefaultValue(blockEntity.translateX * 100 + "cm")
                .build();

            common.addEntry(tx);

            StringListEntry ty = entryBuilder.startTextField(
                            Text.literal("TY"),
                            blockEntity.translateY * 100 + "cm"
                ).setSaveConsumer(str -> {
                    try {
                        Float value = 0f;
                        if (str.endsWith("cm")) {
                            value = Float.parseFloat(str.substring(0, str.length() - 2)) / 100;
                        } else if (str.endsWith("m")) {
                            value = Float.parseFloat(str.substring(0, str.length() - 1));
                        } else {
                            value = Float.parseFloat(str);
                        }
                        if (value != blockEntity.translateY) {
                            final float v = value;
                            updateBlockEntity(be -> be.translateY = v);
                        }
                    } catch (NumberFormatException e) {
                        // tx.setValue(blockEntity.translateY + "cm");
                    }  
                }).setDefaultValue(blockEntity.translateY * 100 + "cm")
                .build();

            common.addEntry(ty);

            StringListEntry tz = entryBuilder.startTextField(
                            Text.literal("TZ"),
                            blockEntity.translateZ + "cm"
                ).setSaveConsumer(str -> {  
                    try {
                        Float value = 0f;
                        if (str.endsWith("cm")) {
                            value = Float.parseFloat(str.substring(0, str.length() - 2));
                        } else if (str.endsWith("m")) {
                            value = Float.parseFloat(str.substring(0, str.length() - 1)) * 100;
                        } else {
                            value = Float.parseFloat(str);
                        }
                        if (value != blockEntity.translateZ) {
                            final float v = value;
                            updateBlockEntity(be -> be.translateZ = v);
                        }
                    } catch (NumberFormatException e) {
                        // tz.setValue(blockEntity.translateZ + "cm");
                    }  
                }).setDefaultValue(blockEntity.translateZ * 100 + "cm")
                .build();

            common.addEntry(tz);

            StringListEntry rx = entryBuilder.startTextField(
                            Text.literal("RX"),
                            Math.toDegrees(blockEntity.rotateX) + "°"
                ).setSaveConsumer(str -> {  
                    try {
                        Float value = 0f;
                        if (str.endsWith("°")) {
                            value = Float.parseFloat(str.substring(0, str.length() - 1));
                        } else {
                            value = Float.parseFloat(str);
                        }
                        value = (float) Math.toRadians(value);
                        if (value != blockEntity.rotateX) {
                            final float v = value;
                            updateBlockEntity(be -> be.rotateX = v);
                        }
                    } catch (NumberFormatException e) {
                        // tz.setValue(blockEntity.translateZ + "cm");
                    }  
                }).setDefaultValue(Math.toDegrees(blockEntity.rotateX) + "°")
                .build();

            common.addEntry(rx);

            StringListEntry ry = entryBuilder.startTextField(
                            Text.literal("RY"),
                            Math.toDegrees(blockEntity.rotateY) + "°"
                ).setSaveConsumer(str -> {  
                    try {
                        Float value = 0f;
                        if (str.endsWith("°")) {
                            value = Float.parseFloat(str.substring(0, str.length() - 1));
                        } else {
                            value = Float.parseFloat(str);
                        }
                        value = (float) Math.toRadians(value);
                        if (value != blockEntity.rotateY) {
                            final float v = value;
                            updateBlockEntity(be -> be.rotateY = v);
                        }
                    } catch (NumberFormatException e) {
                        // tz.setValue(blockEntity.translateZ + "cm");
                    }  
                }).setDefaultValue(Math.toDegrees(blockEntity.rotateY) + "°")
                .build();

            common.addEntry(ry);

            StringListEntry rz = entryBuilder.startTextField(
                            Text.literal("RZ"),
                            Math.toDegrees(blockEntity.rotateZ) + "°"
                ).setSaveConsumer(str -> {  
                    try {
                        Float value = 0f;
                        if (str.endsWith("°")) {
                            value = Float.parseFloat(str.substring(0, str.length() - 1));
                        } else {
                            value = Float.parseFloat(str);
                        }
                        value = (float) Math.toRadians(value);
                        if (value != blockEntity.rotateZ) {
                            final float v = value;
                            updateBlockEntity(be -> be.rotateZ = v);
                        }
                    } catch (NumberFormatException e) {
                        // tz.setValue(blockEntity.translateZ + "cm");
                    }  
                }).setDefaultValue(Math.toDegrees(blockEntity.rotateZ) + "°")
                .build();

            common.addEntry(rz);
        }

        Set<String> keys = new HashSet<>(blockEntity.data.keySet());
        List<String> sortedKeys = new ArrayList<>(keys);
        Collections.sort(sortedKeys);
        if (!sortedKeys.isEmpty()) {
            common.addEntry(entryBuilder.startTextDescription(
                    Text.literal("自定义数据")
            ).build());
            for (String key : sortedKeys) {
                String value = blockEntity.data.get(key);
                common.addEntry(entryBuilder.startTextField(
                        Text.literal(key),
                        value
                ).setSaveConsumer(str -> {
                    if (!str.equals(value)) {
                        updateBlockEntity(be -> be.data.put(key, str));
                    }
                }).setDefaultValue(value).build());
            }
        }
        

        builder.setSavingRunnable(() -> save());

        return builder.build();
    }

    private void updateBlockEntity(Consumer<BlockEyeCandy.BlockEntityEyeCandy> callback) {
        updateBlockEntityCallbacks.add(callback);
    }

    private void save() {
        for (Consumer<BlockEntityEyeCandy> callback : updateBlockEntityCallbacks) {
            callback.accept(blockEntity);
        }
        updateBlockEntityCallbacks.clear();
        PacketUpdateBlockEntity.sendUpdateC2S(blockEntity);
    }

    private Optional<BlockEyeCandy.BlockEntityEyeCandy> getBlockEntity() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return Optional.empty();
        return level.getBlockEntity(blockPos, Main.BLOCK_ENTITY_TYPE_EYE_CANDY.get());
    }
}
