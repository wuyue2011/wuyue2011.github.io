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
import net.minecraft.client.gui.components.Button;
import cn.zbx1425.mtrsteamloco.gui.entries.*;
import com.mojang.blaze3d.platform.Window;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.Function;

public class EyeCandyScreen {

    public static Screen createScreen(BlockPos blockPos, Screen parent) {
        Optional<BlockEntityEyeCandy> opt = getBlockEntity(blockPos);
        BlockEntityEyeCandy blockEntity = opt.orElse(null);
        if (blockEntity == null) {
            return null;
        }

        List<Consumer<BlockEntityEyeCandy>> update = new ArrayList<>();// updateBlockEntityCallbacks;

        EyeCandyProperties properties = blockEntity.getProperties();
        String pid = "";
        if (properties != null) {
            pid = properties.name.getString() + " (" + blockEntity.prefabId + ")";
        }

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(tr("title"))
                .setDoesConfirmSave(false)
                .transparentBackground();
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory common = builder.getOrCreateCategory(
                Text.translatable("gui.mtrsteamloco.config.client.category.common")
        );

        common.addEntry(new ButtonListEntry(
            new Button(0, 0, 300, 20, 
                Text.translatable("gui.mtrsteamloco.eye_candy.present", 
                (properties != null ? (properties.name.getString() + " (" + blockEntity.prefabId + ")") : (blockEntity.prefabId + " (???)"))), 
                btn -> Minecraft.getInstance().setScreen(new SelectScreen(blockPos))), 
            (e, b, a1, a2, a3, a4, a5, a6, a7, a8, a9) -> {
                Window window = Minecraft.getInstance().getWindow();
#if MC_VERSION >= "12000"
                b.setX(window.getGuiScaledWidth() / 2 - 150);
#else
                b.x = window.getGuiScaledWidth() / 2 - 150;
#endif
        }));

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

        List<AbstractConfigListEntry> customEntrys = blockEntity.getCustomConfigEntrys(entryBuilder, () -> createScreen(blockPos, parent));
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
        float old = getValue(type, blockEntity);
        if (old == value) return;
        switch (type) {
            case 0: blockEntity.translateX = value; break;
            case 1: blockEntity.translateY = value; break;
            case 2: blockEntity.translateZ = value; break;
            case 3: blockEntity.rotateX = value; break;
            case 4: blockEntity.rotateY = value; break;
            case 5: blockEntity.rotateZ = value; break;
        }
        blockEntity.sendUpdateC2S();
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
            .setErrorSupplier(value -> {
                save(type, ((float) value) / 20f, blockEntity);
                return Optional.empty();
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
            .setErrorSupplier(value -> {
                save(type, (float) Math.toRadians(value * 5), blockEntity);
                return Optional.empty();
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

    private static class SelectScreen extends SelectListScreen {
        private static final String INSTRUCTION_LINK = "https://aphrodite281.github.io/mtr-ante/#/eyecandy";
        private final WidgetLabel lblInstruction = new WidgetLabel(0, 0, 0, Text.translatable("gui.mtrsteamloco.eye_candy.tip_resource_pack"), () -> {
            this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
                if (bl) {
                    Util.getPlatform().openUri(INSTRUCTION_LINK);
                }
                this.minecraft.setScreen(this);
            }, INSTRUCTION_LINK, true));
        });

        private final BlockPos editingBlockPos;
        private final List<Pair<String, String>> pairs = new ArrayList<>();
        private final Map<String, String> nameMap = new HashMap<>();

        public SelectScreen(BlockPos blockPos) {
            super(Text.literal("Select EyeCandy"));
            this.editingBlockPos = blockPos;
            Set<Map.Entry<String, EyeCandyProperties>> entries = EyeCandyRegistry.elements.entrySet();

            for (Map.Entry<String, EyeCandyProperties> entry : entries) {
                EyeCandyProperties prop = entry.getValue();
                String prid = entry.getKey();
                String name = prop.name.getString();
                pairs.add(new Pair<>(prid, name + " (" + prid + ")"));
                nameMap.put(name + " (" + prid + ")", prid);
            }
        }

        @Override
        protected void init() {
            super.init();

            loadPage();
        }

        @Override
#if MC_VERSION >= "12000"
        public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
#else
        public void render(@NotNull PoseStack guiGraphics, int i, int j, float f) {
#endif
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, i, j, f);
            super.renderSelectPage(guiGraphics);
        }

        @Override
        protected void loadPage() {
            clearWidgets();

            Optional<BlockEyeCandy.BlockEntityEyeCandy> optionalBlockEntity = getBlockEntity();
            if (optionalBlockEntity.isEmpty()) { this.onClose(); return; }
            BlockEyeCandy.BlockEntityEyeCandy blockEntity = optionalBlockEntity.get();
            scrollList.visible = true;
            loadSelectPage(key -> !key.equals(blockEntity.prefabId));
            lblInstruction.alignR = true;
            IDrawing.setPositionAndWidth(lblInstruction, width / 2 + SQUARE_SIZE, height - SQUARE_SIZE - TEXT_HEIGHT, 0);
            lblInstruction.setWidth(width / 2 - SQUARE_SIZE * 2);
            addRenderableWidget(lblInstruction);
        }

        @Override
        protected void onBtnClick(String btnKey) {
            updateBlockEntity(blockEntity -> blockEntity.setPrefabId(btnKey));
        }

        @Override
        protected List<Pair<String, String>> getRegistryEntries() {
            return pairs;
        }

        private void updateBlockEntity(Consumer<BlockEyeCandy.BlockEntityEyeCandy> modifier) {
            getBlockEntity().ifPresent(blockEntity -> {
                modifier.accept(blockEntity);
                PacketUpdateBlockEntity.sendUpdateC2S(blockEntity);
            });
        }

        private Optional<BlockEyeCandy.BlockEntityEyeCandy> getBlockEntity() {
            Level level = Minecraft.getInstance().level;
            if (level == null) return Optional.empty();
            return level.getBlockEntity(editingBlockPos, Main.BLOCK_ENTITY_TYPE_EYE_CANDY.get());
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(EyeCandyScreen.createScreen(editingBlockPos, null));
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
