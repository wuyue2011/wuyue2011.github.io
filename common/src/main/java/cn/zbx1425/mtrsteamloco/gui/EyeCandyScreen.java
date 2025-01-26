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
import me.shedaniel.clothconfig2.gui.entries.FloatListEntry;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.gui.screens.Screen;
import cn.zbx1425.mtrsteamloco.Main;
import net.minecraft.network.chat.Component;
import cn.zbx1425.mtrsteamloco.ClientConfig;
import me.shedaniel.clothconfig2.gui.entries.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.Function;

public class EyeCandyScreen {

    public static Screen createScreen(BlockPos blockPos) {
        Optional<BlockEntityEyeCandy> opt = getBlockEntity(blockPos);
        BlockEntityEyeCandy blockEntity = opt.orElse(null);
        if (blockEntity == null) {
            return null;
        }

        List<Consumer<BlockEntityEyeCandy>> update = new ArrayList<>();// updateBlockEntityCallbacks;

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
                .setTitle(tr("title"))
                .setDoesConfirmSave(false)
                .transparentBackground();
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory common = builder.getOrCreateCategory(
                Text.translatable("gui.mtrsteamloco.config.client.category.common")
        );

        common.addEntry(entryBuilder.startTextDescription(
                Text.translatable("gui.mtrsteamloco.eye_candy.present", (properties != null ? properties.name.getString() : blockEntity.prefabId + " (???)"))
        ).build());

        common.addEntry(entryBuilder.startDropdownMenu(
            tr("select"),
            DropdownMenuBuilder.TopCellElementBuilder.of(pid, str -> str))
            .setDefaultValue(pid).setSelections(elementList).setSaveConsumer(btnKey -> {
                update.add(be -> {
                    if (be.prefabId!= elementMap.get(btnKey)) {
                        be.setPrefabId(elementMap.get(btnKey));
                        be.restore();
                    }
                });
            }).build()
        );

        common.addEntry(entryBuilder
                .startBooleanToggle(
                        tr("full_light"),
                        blockEntity.fullLight
                ).setSaveConsumer(checked -> {
                    if (checked != blockEntity.fullLight) {
                        update.add(be -> be.fullLight = checked);
                    }
                }).setDefaultValue(blockEntity.fullLight).build()
        );

        common.addEntry(entryBuilder
                .startBooleanToggle(
                        tr("as_platform"), 
                        blockEntity.asPlatform
                ).setSaveConsumer(checked -> {
                    if (checked != blockEntity.asPlatform) {
                        update.add(be -> be.asPlatform = checked);
                    }
                }).setDefaultValue(blockEntity.asPlatform).build()
        );

        common.addEntry(entryBuilder.startTextDescription(
                    Text.translatable("gui.mtrsteamloco.eye_candy.shape", blockEntity.getShape())
            ).build());

        common.addEntry(entryBuilder.startTextDescription(
                    Text.translatable("gui.mtrsteamloco.eye_candy.collision", blockEntity.getCollisionShape())
            ).build());

        if (blockEntity.fixedMatrix) {
            common.addEntry(entryBuilder.startTextDescription(
                    tr("fixed")
            ).build());

            common.addEntry(entryBuilder.startTextDescription(
                    Text.literal("TX: " + blockEntity.translateX * 100 + "cm, TY: " + blockEntity.translateY * 100 + "cm, TZ: " + blockEntity.translateZ * 100 + "cm")
            ).build());

            common.addEntry(entryBuilder.startTextDescription(
                    Text.literal("RX: " + Math.toDegrees(blockEntity.rotateX) + "°, RY: " + Math.toDegrees(blockEntity.rotateY) + "°, RZ: " + Math.toDegrees(blockEntity.rotateZ) + "°")
            ).build());
        } else {
            if (ClientConfig.enableSlider) {
                addTranslation0(common, entryBuilder, 0, update, blockEntity);
                addTranslation0(common, entryBuilder, 1, update, blockEntity);
                addTranslation0(common, entryBuilder, 2, update, blockEntity);
                addRotation0(common, entryBuilder, 3, update, blockEntity);
                addRotation0(common, entryBuilder, 4, update, blockEntity);
                addRotation0(common, entryBuilder, 5, update, blockEntity);
            } else {
                addTranslation1(common, entryBuilder, 0, update, blockEntity);
                addTranslation1(common, entryBuilder, 1, update, blockEntity);
                addTranslation1(common, entryBuilder, 2, update, blockEntity);
                addRotation1(common, entryBuilder, 3, update, blockEntity);
                addRotation1(common, entryBuilder, 4, update, blockEntity);
                addRotation1(common, entryBuilder, 5, update, blockEntity);
            }
        }

        List<AbstractConfigListEntry> customEntrys = blockEntity.getCustomConfigEntrys(entryBuilder);
        for (AbstractConfigListEntry entry : customEntrys) {
            common.addEntry(entry);
        }

        builder.setSavingRunnable(() -> {
            for (Consumer<BlockEntityEyeCandy> callback : update) {
                callback.accept(blockEntity);
            }
            blockEntity.sendUpdateC2S();
        });

        return builder.build();
    }

    private static void save(int type, float value, BlockEntityEyeCandy blockEntity) {
        switch (type) {
            case 0: blockEntity.translateX = value; break;
            case 1: blockEntity.translateY = value; break;
            case 2: blockEntity.translateZ = value; break;
            case 3: blockEntity.rotateX = value; break;
            case 4: blockEntity.rotateY = value; break;
            case 5: blockEntity.rotateZ = value; break;
        }
    }

    private static float getValue(int type, BlockEntityEyeCandy blockEntity) {
        switch (type) {
            case 0: return blockEntity.translateX;
            case 1: return blockEntity.translateY;
            case 2: return blockEntity.translateZ;
            case 3: return blockEntity.rotateX;
            case 4: return blockEntity.rotateY;
            case 5: return blockEntity.rotateZ;
        }
        return 0;
    }

    private static String getStr(int type, BlockEntityEyeCandy blockEntity) {
        switch (type) {
            case 0: return "TX";
            case 1: return "TY";
            case 2: return "TZ";
            case 3: return "RX";
            case 4: return "RY";
            case 5: return "RZ";
        }
        return "";
    }

    private static void addTranslation0(ConfigCategory common, ConfigEntryBuilder entryBuilder, int type, List<Consumer<BlockEntityEyeCandy>> update, BlockEntityEyeCandy blockEntity) {
        float rv = getValue(type, blockEntity);
        String str = getStr(type, blockEntity);
        int sv = (int) Math.round(rv * 20f);
        IntegerSliderEntry entry = entryBuilder.startIntSlider(
                Text.literal(str),
                sv, -20, 20
            ).setDefaultValue(0)
            .setSaveConsumer(value -> {
                update.add(be -> save(type, ((float) value) / 20f, be));
            }).build();
        entry.setTextGetter(value -> Text.literal(value * 5 + "cm"));
        common.addEntry(entry);
    }

    private static void addRotation0(ConfigCategory common, ConfigEntryBuilder entryBuilder, int type, List<Consumer<BlockEntityEyeCandy>> update, BlockEntityEyeCandy blockEntity) {
        float rv = getValue(type, blockEntity);
        String str = getStr(type, blockEntity);
        int sv = (int) Math.round(Math.toDegrees(rv) / 5f);
        IntegerSliderEntry entry = entryBuilder.startIntSlider(
                Text.literal(str),
                sv, -18, 18
            ).setDefaultValue(0)
            .setSaveConsumer(value -> {
                update.add(be -> save(type, (float) Math.toRadians(value * 5), be));
            }).build();
        entry.setTextGetter(value -> Text.literal(value * 5 + "°"));
        common.addEntry(entry);
    }

    private static void addTranslation1(ConfigCategory common, ConfigEntryBuilder entryBuilder, int type, List<Consumer<BlockEntityEyeCandy>> update, BlockEntityEyeCandy blockEntity) {
        float rv = getValue(type, blockEntity);
        String str = getStr(type, blockEntity);
        float sv = rv * 100f;
        common.addEntry(entryBuilder.startTextField(
                Text.literal(str),
                sv + "cm"
            ).setSaveConsumer(str1 -> {
                float value = parseMovement(str1).orElse(blockEntity.translateX);
                if (value != blockEntity.translateX) {
                    final float v = value;
                    update.add(be -> save(type, v, be));
                }
            }).setDefaultValue(sv + "cm")
            .setErrorSupplier(verifyMovement)
            .build());
    }

    private static void addRotation1(ConfigCategory common, ConfigEntryBuilder entryBuilder, int type, List<Consumer<BlockEntityEyeCandy>> update, BlockEntityEyeCandy blockEntity) {
        float rv = getValue(type, blockEntity);
        String str = getStr(type, blockEntity);
        common.addEntry(entryBuilder.startTextField(
                Text.literal(str),
                Math.toDegrees(rv) + "°"
            ).setSaveConsumer(str1 -> {
                Float value = parseRotation(str1).orElse(blockEntity.rotateX);
                if (value != blockEntity.rotateX) {
                    final float v = value;
                    update.add(be -> save(type + 3, v, be));
                }
            }).setDefaultValue(Math.toDegrees(rv) + "°")
            .setErrorSupplier(verifyRotation)
            .build());
    }

    private static Optional<Float> parseMovement(String str) {
        try {
            Float value = 0f;
            str = str.toLowerCase().trim();
            if (str.endsWith("cm")) {
                value = Float.parseFloat(str.substring(0, str.length() - 2)) / 100;
            } else if (str.endsWith("m")) {
                value = Float.parseFloat(str.substring(0, str.length() - 1));
            } else {
                value = Float.parseFloat(str);
            }
            return Optional.of(value);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static Optional<Float> parseRotation(String str) {
        try {
            Float value = 0f;
            str = str.toLowerCase().trim();
            if (str.endsWith("°")) {
                value = Float.parseFloat(str.substring(0, str.length() - 1));
            } else {
                value = Float.parseFloat(str);
            }
            value = (float) Math.toRadians(value);
            return Optional.of(value);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static Function<String, Optional<Component>> verifyMovement = (str) -> {
        if (parseMovement(str).isEmpty()) {
            return Optional.of(Text.translatable("gui.mtrsteamloco.error.invalid_value"));
        } else {
            return Optional.empty();
        }
    };

    private static Function<String, Optional<Component>> verifyRotation = (str) -> {
        if (parseRotation(str).isEmpty()) {
            return Optional.of(Text.translatable("gui.mtrsteamloco.error.invalid_value"));
        } else {
            return Optional.empty();
        }
    };

    private static Optional<BlockEyeCandy.BlockEntityEyeCandy> getBlockEntity(BlockPos blockPos) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return Optional.empty();
        return level.getBlockEntity(blockPos, Main.BLOCK_ENTITY_TYPE_EYE_CANDY.get());
    }

    private static Component tr(String key) {
        return Text.translatable("gui.mtrsteamloco.eye_candy." + key);
    }
}
