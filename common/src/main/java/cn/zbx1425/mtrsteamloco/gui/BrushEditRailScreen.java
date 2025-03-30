package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.data.*;
import cn.zbx1425.mtrsteamloco.gui.entries.ButtonListEntry;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateHoldingItem;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateRail;
import cn.zbx1425.mtrsteamloco.render.RailPicker;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Mth;
import net.minecraft.util.FormattedCharSequence;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.FormattedText;
import mtr.client.IDrawing;
import mtr.data.Rail;
import net.minecraft.client.gui.components.AbstractSliderButton;
import mtr.mappings.Text;
import mtr.mappings.UtilitiesClient;
import net.minecraft.client.gui.screens.Screen;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import mtr.screen.WidgetBetterCheckbox;
import mtr.screen.WidgetBetterTextField;
import net.minecraft.util.Mth;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.Util;
import mtr.data.RailType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.Minecraft;
#if MC_VERSION >= "12000"
import net.minecraft.client.gui.GuiGraphics;
#endif
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
#if MC_VERSION >= "12000"
import net.minecraft.client.gui.GuiGraphics;
#else
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
#endif
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import me.shedaniel.clothconfig2.gui.entries.*;
import mtr.mappings.Text;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.mtrsteamloco.ClientConfig;
import net.minecraft.client.gui.components.EditBox;
import me.shedaniel.clothconfig2.api.Tooltip;
import me.shedaniel.math.Point;
import cn.zbx1425.mtrsteamloco.network.util.DoubleFloatMapSerializer;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.Optional;

public class BrushEditRailScreen {

    private final Rail pickedRail;
    private final RailExtraSupplier supplier;
    private final BlockPos pickedPosStart;
    private final BlockPos pickedPosEnd;

    private Screen screen;
    private Screen parent;

    private BrushEditRailScreen(Rail pickedRail, BlockPos pickedPosStart, BlockPos pickedPosEnd, Screen parent) {
        this.pickedRail = pickedRail;
        this.supplier = (RailExtraSupplier) pickedRail;
        this.pickedPosStart = pickedPosStart;
        this.pickedPosEnd = pickedPosEnd;
        this.parent = parent;
        init();
    }

    private BrushEditRailScreen(Screen parent) {
        pickedRail = RailPicker.pickedRail;
        supplier = (RailExtraSupplier) pickedRail;
        pickedPosStart = RailPicker.pickedPosStart;
        pickedPosEnd = RailPicker.pickedPosEnd;
        this.parent = parent;
        init();
    }

    private void init() {
        if (pickedRail == null) screen = parent;
        else {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("gui.mtrsteamloco.brush_edit_rail.title"))
                    .setDoesConfirmSave(false)
                    .transparentBackground();
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory common = builder.getOrCreateCategory(
                    Text.translatable("gui.mtrsteamloco.config.client.category.common")
            );

            CompoundTag brushTag = getBrushTag();
            String modelKey = supplier.getModelKey();

            common.addEntry(
                entryBuilder.startTextDescription(
                    Text.translatable("gui.mtrsteamloco.brush_edit_rail.brush_hint")
                ).build());

            boolean enableRailType = brushTag != null && brushTag.contains("RailType");
            common.addEntry(
                entryBuilder.startBooleanToggle(
                    Text.translatable("gui.mtrsteamloco.brush_edit_rail.enable_rail_type"),
                    enableRailType
                ).setTooltipSupplier(checked -> {
                    if (checked != enableRailType) {
                        updateBrushTag(
                            compoundTag -> {
                                if (checked) {
                                    compoundTag.putString("RailType", pickedRail.railType.toString());
                                } else {
                                    compoundTag.remove("RailType");
                                }
                            }
                        );
                        Minecraft.getInstance().setScreen(BrushEditRailScreen.createScreen(pickedRail, pickedPosStart, pickedPosEnd, parent));
                    }
                    return Optional.empty();
                }).build()
            );
            if (enableRailType) {
                common.addEntry(
                    entryBuilder.startTextField(
                        Text.translatable("gui.mtrsteamloco.brush_edit_rail.rail_type"),
                        brushTag.getString("RailType")
                    ).setErrorSupplier(str -> {
                        try {
                            RailType type = RailType.valueOf(str);
                            if (type != null && type != RailType.NONE) return Optional.empty();
                        } catch (Exception e) {}
                        return Optional.of(Text.translatable("gui.mtrsteamloco.brush_edit_rail.rail_type_error"));
                    }
                    ).setSaveConsumer(str -> {
                        updateBrushTag(compoundTag -> {
                            compoundTag.putString("RailType", str);
                        });
                    }).build()
                );
            }

            boolean enableModelKey = brushTag != null && brushTag.contains("ModelKey");
            common.addEntry(
                entryBuilder.startBooleanToggle(
                    Text.translatable("gui.mtrsteamloco.brush_edit_rail.enable_model_key"),
                    enableModelKey
                ).setTooltipSupplier(checked -> {
                    if (checked != enableModelKey) {
                        updateBrushTag(
                            compoundTag -> {
                                if (checked) {
                                    compoundTag.putString("ModelKey", modelKey);
                                } else {
                                    compoundTag.remove("ModelKey");
                                }
                            });
                        Minecraft.getInstance().setScreen(BrushEditRailScreen.createScreen(pickedRail, pickedPosStart, pickedPosEnd, parent));
                    }
                    return Optional.empty();
                })
                .setDefaultValue(enableModelKey).build()
            );

            if (enableModelKey) {
                RailModelProperties properties = RailModelRegistry.elements.get(modelKey);
                common.addEntry(new ButtonListEntry(
                    Text.literal(""),
#if MC_VERSION >= "11903"
                    new Button.Builder(
                        Text.translatable("gui.mtrsteamloco.brush_edit_rail.present", 
                            (properties != null ? (properties.name.getString()) : (modelKey + " (???)"))),
                        btn -> Minecraft.getInstance().setScreen(new SelectScreen())).pos(0, 0).size(300, 20).build(),
#else
                    new Button(0, 0, 300, 20, 
                        Text.translatable("gui.mtrsteamloco.brush_edit_rail.present", 
                            (properties != null ? (properties.name.getString()) : (modelKey + " (???)"))), 
                        btn -> Minecraft.getInstance().setScreen(new SelectScreen())), 
#endif
                    (e, b, a1, a2, a3, a4, a5, a6, a7, a8, a9) -> {
                        Window window = Minecraft.getInstance().getWindow();
#if MC_VERSION >= "11903"
                        b.setX(window.getGuiScaledWidth() / 2 - 150);
#else
                        b.x = window.getGuiScaledWidth() / 2 - 150;
#endif
                }));
            }

            boolean enableVertCurveRadius = brushTag != null && brushTag.contains("VerticalCurveRadius");
            float vertCurveRadius = ((RailExtraSupplier)pickedRail).getVerticalCurveRadius();
            common.addEntry(
                entryBuilder.startBooleanToggle(
                    Text.translatable("gui.mtrsteamloco.brush_edit_rail.enable_vertical_curve_radius"),
                    enableVertCurveRadius
                ).setTooltipSupplier(checked -> {
                    if (checked != enableVertCurveRadius) {
                        updateBrushTag(compoundTag -> {
                            if (checked) {
                                compoundTag.putFloat("VerticalCurveRadius", vertCurveRadius);
                            } else {
                                compoundTag.remove("VerticalCurveRadius");
                            }
                        });
                        Minecraft.getInstance().setScreen(BrushEditRailScreen.createScreen(pickedRail, pickedPosStart, pickedPosEnd, parent));
                    }
                    return Optional.empty();
                })
                .setDefaultValue(enableVertCurveRadius).build()
            );
            if (enableVertCurveRadius) {
                common.addEntry(new VertCurveRadiusListEntry(vertCurveRadius, this));
            }

            boolean enableRollAngle = brushTag != null && brushTag.contains("RollAngleMap");

            common.addEntry(
                entryBuilder.startBooleanToggle(
                    Text.translatable("gui.mtrsteamloco.brush_edit_rail.enable_roll_angle"),
                    enableRollAngle
                ).setTooltipSupplier(checked -> {
                    if (checked != enableRollAngle) {
                        if (!checked) {
                            updateBrushTag(compoundTag -> {
                                compoundTag.remove("RollAngleMap");
                            });
                        } else {
                            new RollAnglesListEntry().update();
                        }
                        Minecraft.getInstance().setScreen(BrushEditRailScreen.createScreen(pickedRail, pickedPosStart, pickedPosEnd, parent));
                    }
                    return Optional.empty();
                }).build()
            );
            if (enableRollAngle) common.addEntry(new RollAnglesListEntry());

            Function<Integer, Optional<Component[]>> f = v -> Optional.of(new Component[]{Text.translatable("gui.mtrsteamloco.brush_edit_rail.opening_direction_tooltip")});

            common.addEntry(
                entryBuilder.startIntSlider(
                    Text.translatable("开门方向"),
                    supplier.getOpeningDirection(),
                    0, 3
                )
                .setTooltipSupplier(f)
                .setDefaultValue(0)
                .setSaveConsumer(value -> {
                    supplier.setOpeningDirection(value);
                    PacketUpdateRail.sendUpdateC2S(pickedRail, pickedPosStart, pickedPosEnd);
                }).build()
            );

            Map<String, String> customConfigs = supplier.getCustomConfigs();
            Map<String, ConfigResponder> responders = supplier.getCustomResponders();
            List<AbstractConfigListEntry> entries = ConfigResponder.getEntrysFromMaps(customConfigs, responders, entryBuilder, () -> BrushEditRailScreen.createScreen(pickedRail, pickedPosStart, pickedPosEnd, parent));
            for (AbstractConfigListEntry entry : entries) {
                common.addEntry(entry);
            }
            screen = builder.build();
        }
    }

    public void updateRadius(float newRadius, boolean send) {
        if (send) {
            updateBrushTag(compoundTag -> {
                compoundTag.putFloat("VerticalCurveRadius", newRadius);
            });
        }
    }

    public Component[] getVerticalValueText(float verticalRadius) {
        Rail rail = pickedRail;
        if (rail == null) return new Component[] {Text.literal("(???)")};
        int H = Math.abs(((RailExtraSupplier)rail).getHeight());
        double L = rail.getLength();
        double maxRadius = (H == 0) ? 0 : (H * H + L * L) / (H * 4);
        double gradient;
        if (verticalRadius < 0) {
            gradient = H / L * 1000;
        } else if (verticalRadius == 0 || verticalRadius > maxRadius) {
            gradient = Math.tan(RailExtraSupplier.getVTheta(rail, maxRadius)) * 1000;
        } else {
            gradient = Math.tan(RailExtraSupplier.getVTheta(rail, verticalRadius)) * 1000;
        }
        return new Component[] {
            Text.translatable("gui.mtrsteamloco.brush_edit_rail.vertical_curve_radius_values.1"),
            Text.translatable("gui.mtrsteamloco.brush_edit_rail.vertical_curve_radius_values.2", String.format("%.1f", maxRadius)),
            Text.translatable("gui.mtrsteamloco.brush_edit_rail.vertical_curve_radius_values.3", String.format("%.1f", gradient))};
    }

    public Screen getScreen() {
        return screen;
    }

    public static Screen createScreen(Screen parent) {
        return new BrushEditRailScreen(parent).getScreen();
    }

    public static Screen createScreen(Rail rail, BlockPos posStart, BlockPos posEnd, Screen parent) {
        return new BrushEditRailScreen(rail, posStart, posEnd, parent).getScreen();
    }

    public static void applyBrushToPickedRail(CompoundTag railBrushProp, boolean isBatchApply) {
        Rail pickedRail = RailPicker.pickedRail;
        BlockPos pickedPosStart = RailPicker.pickedPosStart;
        BlockPos pickedPosEnd = RailPicker.pickedPosEnd;
        if (railBrushProp == null) return;
        if (pickedRail == null) return;
        RailExtraSupplier pickedExtra = (RailExtraSupplier) pickedRail;
        boolean propertyUpdated = false;
        if (railBrushProp.contains("ModelKey") &&
                !railBrushProp.getString("ModelKey").equals(pickedExtra.getModelKey())) {
            pickedExtra.setModelKey(railBrushProp.getString("ModelKey"));
            propertyUpdated = true;
        }
        if (railBrushProp.contains("VerticalCurveRadius") &&
                railBrushProp.getFloat("VerticalCurveRadius") != pickedExtra.getVerticalCurveRadius()) {
            pickedExtra.setVerticalCurveRadius(railBrushProp.getFloat("VerticalCurveRadius"));
            propertyUpdated = true;
        }
        if (railBrushProp.contains("RollAngleMap")) {
            Map<Double, Float> raw = DoubleFloatMapSerializer.deserialize(railBrushProp.getString("RollAngleMap"));
            Map<Double, Float> rollAngleMap = new HashMap<>();
            Set<Map.Entry<Double, Float>> entries = new HashSet<>(raw.entrySet());
            double length = pickedRail.getLength();
            for(Map.Entry<Double, Float> entry : entries) {
                rollAngleMap.put(entry.getKey() * length, entry.getValue());
            }
            pickedExtra.setRollAngleMap(rollAngleMap);
            propertyUpdated = true;
        }
        if (railBrushProp.contains("RailType")) {
            pickedExtra.setRailType(RailType.valueOf(railBrushProp.getString("RailType")));
        }
        if (isBatchApply && !propertyUpdated) {
            // Right-click again to reverse the direction
            pickedExtra.setRenderReversed(!pickedExtra.getRenderReversed());
        }
        PacketUpdateRail.sendUpdateC2S(pickedRail, pickedPosStart, pickedPosEnd);
    }

    public static CompoundTag getBrushTag() {
        if (Minecraft.getInstance().player == null) return null;
        ItemStack brushItem = Minecraft.getInstance().player.getMainHandItem();
        if (!brushItem.is(mtr.Items.BRUSH.get())) return null;
        CompoundTag nteTag = brushItem.getTagElement("NTERailBrush");
        return nteTag;
    }

    public static void updateBrushTag(Consumer<CompoundTag> modifier) {
        if (Minecraft.getInstance().player == null) return;
        ItemStack brushItem = Minecraft.getInstance().player.getMainHandItem();
        if (!brushItem.is(mtr.Items.BRUSH.get())) return;
        CompoundTag nteTag = brushItem.getOrCreateTagElement("NTERailBrush");
        modifier.accept(nteTag);
        applyBrushToPickedRail(nteTag, false);
        PacketUpdateHoldingItem.sendUpdateC2S();
    }

    private class SelectScreen extends SelectListScreen {

        private static final String INSTRUCTION_LINK = "https://aphrodite281.github.io/mtr-ante/#/railmodel";
        private final WidgetLabel lblInstruction = new WidgetLabel(0, 0, 0, Text.translatable("gui.mtrsteamloco.eye_candy.tip_resource_pack"), () -> {
            this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
                if (bl) {
                    Util.getPlatform().openUri(INSTRUCTION_LINK);
                }
                this.minecraft.setScreen(this);
            }, INSTRUCTION_LINK, true));
        });

        public SelectScreen() {
            super(Text.literal("Select rail arguments"));
        }

        @Override
        protected void init() {
            super.init();

            loadPage();
        }

        @Override
        protected void loadPage() {
            clearWidgets();

            CompoundTag brushTag = getBrushTag();
            String modelKey = brushTag == null ? "" : brushTag.getString("ModelKey");
            scrollList.visible = true;
            loadSelectPage(key -> !key.equals(modelKey));
            lblInstruction.alignR = true;
            IDrawing.setPositionAndWidth(lblInstruction, width / 2 + SQUARE_SIZE, height - SQUARE_SIZE - TEXT_HEIGHT, 0);
            lblInstruction.setWidth(width / 2 - SQUARE_SIZE * 2);
            addRenderableWidget(lblInstruction);
        }

        @Override
        protected void onBtnClick(String btnKey) {
            BrushEditRailScreen.updateBrushTag(compoundTag -> compoundTag.putString("ModelKey", btnKey));
            ((RailExtraSupplier) pickedRail).setModelKey(btnKey);
            PacketUpdateRail.sendUpdateC2S(pickedRail, pickedPosStart, pickedPosEnd);
        }

        @Override
        protected List<Pair<String, String>> getRegistryEntries() {
            return new HashSet<>(RailModelRegistry.elements.entrySet()).stream()
                    .filter(e -> !e.getValue().name.getString().isEmpty())
                    .map(e -> new Pair<>(e.getKey(), e.getValue().name.getString()))
                    .toList();
        }

        @Override
    #if MC_VERSION >= "12000"
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    #else
        public void render(PoseStack guiGraphics, int mouseX, int mouseY, float partialTick) {
    #endif
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTick);

            renderSelectPage(guiGraphics);
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(BrushEditRailScreen.createScreen(pickedRail, pickedPosStart, pickedPosEnd, parent));
        }

        @Override
        public boolean isPauseScreen() {
            return true;
        }
    }
    
    @Environment(EnvType.CLIENT)
    private class VertCurveRadiusListEntry extends TooltipListEntry<String> implements ContainerEventHandler {
        
        private final Button btnSetDefaultRadius = UtilitiesClient.newButton(
        Text.translatable("gui.mtrsteamloco.brush_edit_rail.vertical_curve_radius_set_max"),
        sender -> updateRadius(0, true));
        private final Button btnSetNoRadius = UtilitiesClient.newButton(
            Text.translatable("gui.mtrsteamloco.brush_edit_rail.vertical_curve_radius_set_none"),
            sender -> updateRadius(-1, true)
        );
        private final WidgetBetterTextField radiusInput = new WidgetBetterTextField("", 8); 
        private final BrushEditRailScreen screen;
        private final List<AbstractWidget> widgets;
        private float vertCurveRadius = 0;

        public VertCurveRadiusListEntry(float v, BrushEditRailScreen screen) {
            super(Text.literal(""), null, false);
            vertCurveRadius = v;
            setTooltipSupplier(() -> Optional.of(screen.getVerticalValueText(vertCurveRadius)));
            this.screen = screen;

            updateRadius(vertCurveRadius, false);
            radiusInput.setResponder(
                text -> {
                    if (!text.isEmpty()) {
                        try {
                            float newRadius = Float.parseFloat(text);
                            Rail rail = pickedRail;
                            if (rail != null) {
                                int H = Math.abs(((RailExtraSupplier) rail).getHeight());
                                double L = rail.getLength();
                                double maxRadius = (H == 0) ? 0 : (H * H + L * L) / (H * 4);
                                if (newRadius < maxRadius) {
                                    radiusInput.setTextColor(0xE0E0E0);
                                } else {
                                    radiusInput.setTextColor(0xEEEE00);
                                }
                            } else {
                                radiusInput.setTextColor(0xEEEE00);
                            }
                            updateRadius(newRadius, true);
                        } catch (Exception ignored) {
                            radiusInput.setTextColor(0xFF0000);
                        }
                    }
                });
            widgets = Lists.newArrayList(btnSetDefaultRadius, btnSetNoRadius, radiusInput);
        }

        public void updateRadius(float newRadius, boolean send) {
            btnSetDefaultRadius.active = newRadius != 0;
            btnSetNoRadius.active = newRadius >= 0;
            String expectedText;
            if (newRadius <= 0) {
                expectedText = "";
            } else {
                expectedText = Integer.toString((int) newRadius);
            }
            if (!expectedText.equals(radiusInput.getValue())) {
                radiusInput.setValue(expectedText);
                radiusInput.moveCursorToStart();
            }
            vertCurveRadius = newRadius;
            screen.updateRadius(newRadius, send);
        }

        @Override
        public boolean isEdited() {
            return false;
        }

        @Override
        public String getValue() {
            return "";
        }
        
        @Override
        public Optional<String> getDefaultValue() {
            return Optional.empty();
        }

        @Override
#if MC_VERSION >= "12000"
        public void render(GuiGraphics matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
#else
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
#endif
            super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
            IDrawing.setPositionAndWidth(radiusInput, 80, y, 200);
            IDrawing.setPositionAndWidth(btnSetDefaultRadius, 290, y, 60);
            IDrawing.setPositionAndWidth(btnSetNoRadius, 355, y, 60);
            radiusInput.render(matrices, mouseX, mouseY, delta);
            btnSetDefaultRadius.render(matrices, mouseX, mouseY, delta);
            btnSetNoRadius.render(matrices, mouseX, mouseY, delta);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return widgets;
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return widgets;
        }
        
        @Override
        public void save() {
            
        }
    }

    @Environment(EnvType.CLIENT)
    private class RollAnglesListEntry extends TooltipListEntry<String> implements ContainerEventHandler {
        private final List<Node> nodes = new ArrayList<>();
        private Node now = null;
        private final Slider angleSlider = new Slider(50, 0, 200, 20, -180D, 0D, 180D, 
            value -> Text.translatable("gui.mtrsteamloco.brush_edit_rail.roll_angle", value * 360D - 180D), 
            value -> {
                if (now != null) now.setAngle((float) Math.toRadians(value * 360D - 180D));
            });
        private final EditBox angleInput = new EditBox(Minecraft.getInstance().font, 50, 0, 200, 20, Text.literal(""));
        
        Button btnAddNode = UtilitiesClient.newButton(
            Text.translatable("+"),
            sender -> addNode(new Node(0, 0)));
        Button btnRemoveNode = UtilitiesClient.newButton(
            Text.translatable("-"),
            sender -> removeNode(now));
        Button btnClear = UtilitiesClient.newButton(
            Text.translatable("gui.mtrsteamloco.brush_edit_rail.clear"),
            sender -> removeAllNodes()
        );
        Button btnChangeMode = UtilitiesClient.newButton(
            Text.translatable("⇄"),
            sender -> {
                ClientConfig.useEditBoxSetRailRolling = !ClientConfig.useEditBoxSetRailRolling;
                ClientConfig.save();
                focusNode(now);
            }
        );

        private final List<AbstractWidget> widgets = Lists.newArrayList(btnAddNode, btnRemoveNode, btnClear);

        private static final ResourceLocation WHITE = new ResourceLocation("minecraft", "textures/block/white_concrete_powder.png");
        private static final ResourceLocation POS = new ResourceLocation(Main.MOD_ID, "textures/gui/rail/ante.png");
        private static final int START = 50;
        private static final int WIDTH = 200;

        private final boolean flag;

        public RollAnglesListEntry() {
            super(Text.literal(""), null, false);
            Map<Double, Float> rollAngles = supplier.getRollAngleMap();
            Set<Map.Entry<Double, Float>> entries = new HashSet<>(rollAngles.entrySet());
            for (Map.Entry<Double, Float> entry : entries) {
                addNode(new Node(entry.getKey(), entry.getValue()));
            }
            Vector3f p1 = new Vector3f(pickedRail.getPosition(0));
            Vector3f p2 = new Vector3f(pickedRail.getPosition(pickedRail.getLength()));
            Vector3f start = new Vector3f(pickedPosStart);
            float d1 = p1.distance(start);
            float d2 = p2.distance(start);
            flag = d1 < d2;
            angleInput.setResponder(str -> {
                try {
                    float angle = Float.parseFloat(str);
                    if (now != null) now.setAngle((float) Math.toRadians(angle));
                    angleInput.setTextColor(0x00FF00);
                } catch (Exception ignored) {
                    angleInput.setTextColor(0xFF0000);
                }
            });
        }

        public boolean mode() {
            return ClientConfig.useEditBoxSetRailRolling;
        }

        public void update() {
            Map<Double, Float> res = new HashMap<>();
            List<Node> copy = new ArrayList<>(nodes);
            for (Node node : copy) {
                res.put(node.getValue(), node.getAngle());
            }
            supplier.setRollAngleMap(res);
            PacketUpdateRail.sendUpdateC2S(pickedRail, pickedPosStart, pickedPosEnd);

            updateBrushTag(compoundTag -> {
                Map<Double, Float> res1 = new HashMap<>();
                Set<Map.Entry<Double, Float>> entries = new HashSet<>(res.entrySet());
                double length = pickedRail.getLength();
                for (Map.Entry<Double, Float> entry : entries) {
                    res1.put(entry.getKey() / length, entry.getValue());
                }
                compoundTag.putString("RollAngleMap", DoubleFloatMapSerializer.serializeToString(res1));
            });
        }

        public void focusNode(Node node) {
            now = node;
            if (now != null) {
                if (mode()) {
                    angleInput.setValue(Math.toDegrees(node.getAngle()) + "");
                    angleInput.setTextColor(0xFFFFFF);
                } else angleSlider.setValue(1 / 360D * ((Math.toDegrees((double) node.getAngle())) + 180D), true);

                for (int i = 0; i < nodes.size(); i++) {
                    if (nodes.get(i) == node) {
                        nodes.remove(i);
                        nodes.add(0, node);
                        break;
                    }
                }
            }
        }
        
        public void addNode(Node node) {
            nodes.add(node);
            focusNode(node);
            update();
        }

        public void removeNode(Node node) {
            if (node == null) return;
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i) == node) {
                    nodes.remove(i);
                    if (now == node) {
                        focusNode(nodes.isEmpty() ? null : nodes.get(0));
                    }
                    break;
                }
            }
            update();
        }

        public void removeAllNodes() {
            nodes.clear();
            focusNode(null);
            update();
        }

        @Override
        public boolean isEdited() {
            return false;
        }

        @Override
        public String getValue() {
            return "";
        }

        @Override
        public Optional<String> getDefaultValue() {
            return Optional.empty();
        }

        @Override
        public void save() {
            
        }

        @Override
        public int getItemHeight() {
            return 24 * 3;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            List<GuiEventListener> res = new ArrayList<>(widgets);
            if (now != null) {
                if (ClientConfig.useEditBoxSetRailRolling) res.add(angleInput);
                else res.add(angleSlider);
                res.add(btnChangeMode);
            }
            res.addAll(nodes);
            return res;
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            List<NarratableEntry> res = new ArrayList<>(widgets);
            if (now != null) {
                if (ClientConfig.useEditBoxSetRailRolling) res.add(angleInput);
                else res.add(angleSlider);
                res.add(btnChangeMode);
            }
            return widgets;
        }

        @Override
#if MC_VERSION >= "12000"
        public void render(GuiGraphics matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
#else
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
#endif
            super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
            drawText(matrices, Minecraft.getInstance().font, Text.translatable("gui.mtrsteamloco.brush_edit_rail.roll_setting").getString(), x, y + 6, 0xA8A8A8);
            y += 22;

            boolean reversed = ((RailExtraSupplier) pickedRail).getRenderReversed();
            if (!reversed == flag) {
                blit(matrices, POS, 25, y + 2, 20, 20);
                if (25 <= mouseX && mouseX <= 45 && y + 2 <= mouseY && mouseY <= y + 22) {
                    addTooltip(Tooltip.of(new Point(mouseX, mouseY), wrapLinesToScreen(new Component[] {Text.translatable("gui.mtrsteamloco.brush_edit_rail.roll_setting.pos")})));
                }
            } else {
                blit(matrices, POS, 255, y + 2, 20, 20);
                if (255 <= mouseX && mouseX <= 275 && y + 2 <= mouseY && mouseY <= y + 22) {
                    addTooltip(Tooltip.of(new Point(mouseX, mouseY), wrapLinesToScreen(new Component[] {Text.translatable("gui.mtrsteamloco.brush_edit_rail.roll_setting.pos")})));
                }
            }
            blit(matrices, WHITE, 50, y + 8,  200, 8);
            for (int i = nodes.size() - 1; i >= 0; i--) {
                Node node = nodes.get(i);
                node.render(matrices, x, y, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
            }
            y += 2;
            // drawText(matrices, Minecraft.getInstance().font, nodes.size() + "", 30, y + 6, 0xFFFFFF);
            IDrawing.setPositionAndWidth(btnAddNode, 280, y, 50);
            IDrawing.setPositionAndWidth(btnRemoveNode, 335, y, 50);
            IDrawing.setPositionAndWidth(btnClear, 390, y, 50);
            btnAddNode.render(matrices, mouseX, mouseY, delta);
            btnRemoveNode.render(matrices, mouseX, mouseY, delta);
            btnClear.render(matrices, mouseX, mouseY, delta);
            y += 24;

            IDrawing.setPositionAndWidth(btnChangeMode, 280, y, 50);
            UtilitiesClient.setWidgetY(angleSlider, y);
            UtilitiesClient.setWidgetY(angleInput, y);
            if (now != null) {
                if (!ClientConfig.useEditBoxSetRailRolling) {
                    angleSlider.render(matrices, mouseX, mouseY, delta);
                    angleSlider.visible = true;
                    angleInput.visible = false;
                } else {
                    angleInput.render(matrices, mouseX, mouseY, delta);
                    angleInput.visible = true;
                    angleSlider.visible = false;
                } 
                btnChangeMode.visible = true;
                btnChangeMode.render(matrices, mouseX, mouseY, delta);
            } else {
                angleSlider.visible = false;
                angleInput.visible = false;
                btnChangeMode.visible = false;
            }
        }

        protected FormattedCharSequence[] wrapLinesToScreen(Component[] lines) {
            return wrapLines(lines, screen.width);
        }
    
        protected FormattedCharSequence[] wrapLines(Component[] lines, int width) {
            final Font font = Minecraft.getInstance().font;
            
            return Arrays.stream(lines)
                    .map(line -> font.split(line, width))
                    .flatMap(List::stream)
                    .toArray(FormattedCharSequence[]::new);
        }

#if MC_VERSION >= "12000"
        private static int drawText(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color) {
            FormattedText formattedText = FormattedText.of(text);
            List<FormattedCharSequence> lines = font.split(formattedText, Minecraft.getInstance().getWindow().getGuiScaledWidth() - 40);
            for (FormattedCharSequence line : lines) {
                guiGraphics.drawString(font, line, x, y, color);
                y += Mth.ceil(font.lineHeight * 1.1f);
            }
            return y;
        }
        private static void blit(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height) {
            guiGraphics.blit(texture, x, y, width, height, 0, 0, 1, 1, 1, 1);
        }

        // private static void blit(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int argb) {

        // }
#else
        private static int drawText(PoseStack matrices, Font font, String text, int x, int y, int color) {
            FormattedText formattedText = FormattedText.of(text);
            List<FormattedCharSequence> lines = font.split(formattedText, Minecraft.getInstance().getWindow().getGuiScaledWidth() - 40);
            for (FormattedCharSequence line : lines) {
                font.drawShadow(matrices, line, x, y, color);
                y += Mth.ceil(font.lineHeight * 1.1f);
            }
            return y;
        }
        private static void blit(PoseStack matrices, ResourceLocation texture, int x, int y, int width, int height) {
            RenderSystem.setShaderTexture(0, texture);
            GuiComponent.blit(matrices, x, y, width, height, 0, 0, 1, 1, 1, 1);
        }

        // private static void blit(PoseStack matrices, int x1, int y1, int x2, int y2, int argb) {
        //     GuiComponent.fill(matrices, x1, y1, x2, y2, argb);
        // }
#endif

        private class Slider extends AbstractSliderButton {
            Function<Double, Component> textGetter;
            Consumer<Double> valueSetter;

            protected Slider(int x, int y, int w, int h, double min, double value, double max, Function<Double, Component> textGetter, Consumer<Double> valueSetter) {
                super(x, y, w, h, Text.translatable(""), value);
                this.textGetter = textGetter;
                this.valueSetter = valueSetter;
            }
            
            @Override
            public void updateMessage() {
                setMessage(textGetter.apply(value));
            }
            
            @Override
            protected void applyValue() {
                valueSetter.accept(value);
            }
            
            @Override
            public boolean keyPressed(int int_1, int int_2, int int_3) {
                if (!isEditable())
                    return false;
                return super.keyPressed(int_1, int_2, int_3);
            }
            
            @Override
            public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
                if (!isEditable())
                    return false;
                return super.mouseDragged(double_1, double_2, int_1, double_3, double_4);
            }

            public void setValue(double value, boolean air) {
                double d0 = this.value;
                this.value = Mth.clamp(value, 0.0D, 1.0D);
                if (d0 != this.value) {
                    this.applyValue();
                }

                this.updateMessage();
            }
        }

        private class Node implements GuiEventListener{
            private double value;
            private float angle;
            private int y;
            private boolean dragged = false;

            // private static final ResourceLocation P = new ResourceLocation(Main.MOD_ID, "textures/gui/rail/p.png");
            private static final ResourceLocation P = new ResourceLocation(Main.MOD_ID, "textures/gui/rail/pi.png");
            private static final ResourceLocation Y = new ResourceLocation(Main.MOD_ID, "textures/gui/rail/y.png");
            private static final float NW = 6;
            private static final float NH = 10;
            
            public Node(double value, float angle) {
                this.value = value / pickedRail.getLength();
                this.angle = angle;
            }

#if MC_VERSION >= "12000"
            public void render(GuiGraphics matrices, int x, int y, int width, int height, int mouseX, int mouseY, boolean isHovered, float delta) {
#else 
            public void render(PoseStack matrices, int x, int y, int width, int height, int mouseX, int mouseY, boolean isHovered, float delta) {
#endif  
                this.y = y + 8 + 4;
                if (isActive()) {
                    blit(matrices, Y, getX() - (int) (NW / 2), this.y - (int) (NH / 2), (int) NW, (int) NH);
                } else {
                    blit(matrices, P, getX() - (int) (NW / 2), this.y - (int) (NH / 2), (int) NW, (int) NH);
                }
            }

            public int getX() {
                return (int) (START + value * WIDTH);
            }

            public void setValue(int x) {
                double old = value;
                if (x <= START) x = START;
                else if (x >= START + WIDTH) x = START + WIDTH;
                else value = (x - START) / (double) WIDTH;
                if (old == value) return;
                update();
            }

            public void setValue(double value) {
                double old = this.value;
                if (value < 0) value = 0;
                else if (value > 1) value = 1;
                if (old == value) return;
                this.value = value;
                update();
            }

            public void setAngle(float angle) {
                if (this.angle == angle) return;
                this.angle = angle;
                update();
            }

            public double getValue() {
                return value * pickedRail.getLength();
            }

            public float getAngle() {
                return angle;
            }

            public boolean isActive() {
                return now == this;
            }

#if MC_VERSION >= "11904"
            boolean isFocused = false;
            
            @Override
            public boolean isFocused() {
                return isFocused;
            }

            @Override
            public void setFocused(boolean b) {
                isFocused = b;
            }
#endif

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                int midX = getX();
                float minX = midX - NW / 2;
                float maxX = midX + NW / 2;
                float minY = y - NH / 2;
                float maxY = y + NH / 2;
                return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int in) {
                if (isMouseOver(mouseX, mouseY)) {
                    focusNode(this);
                    return true;
                }
                return false;
            }

            @Override
            public boolean mouseDragged(double sx, double sy, int t, double dx, double dy) {
                if ((isActive() && dragged) || isMouseOver(sx, sy)) {
                    setValue((int) (sx + dx));
                    dragged = true;
                    return true;
                }
                return false;
            }

            @Override
            public boolean mouseReleased(double x, double y, int t) {
                if (isActive() && dragged) {
                    dragged = false;
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyPressed(int p_93596_, int p_93597_, int p_93598_) {
                boolean flag = p_93596_ == 263;
                if (flag || p_93596_ == 262) {
                    double f = flag ? -0.025 : 0.025;
                    setValue(value + f);
                }

                return false;
            }
        }
    }
}
