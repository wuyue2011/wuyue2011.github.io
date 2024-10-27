package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
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

import java.util.*;
import java.util.function.Consumer;

public class EyeCandyScreen extends SelectListScreen {

    private boolean isSelectingModel = false;

    private static final String INSTRUCTION_LINK = "https://www.zbx1425.cn/nautilus/mtr-nte/#/eyecandy";
    private final WidgetLabel lblInstruction = new WidgetLabel(0, 0, 0, Text.translatable("gui.mtrsteamloco.eye_candy.tip_resource_pack"), () -> {
        this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(INSTRUCTION_LINK);
            }
            this.minecraft.setScreen(this);
        }, INSTRUCTION_LINK, true));
    });

    private final BlockPos editingBlockPos;

    private WidgetSlider tx;
    private WidgetSlider ty;
    private WidgetSlider tz;
    private WidgetSlider rx;
    private WidgetSlider ry;
    private WidgetSlider rz;

    private WidgetBetterTextField textField;
    private WidgetBetterTextField textField2;

    public EyeCandyScreen(BlockPos blockPos) {
        super(Text.literal("Select EyeCandy"));
        this.editingBlockPos = blockPos;
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

        if (isSelectingModel) {
            super.renderSelectPage(guiGraphics);
        }
    }

    @Override
    protected void loadPage() {
        clearWidgets();

        Optional<BlockEyeCandy.BlockEntityEyeCandy> optionalBlockEntity = getBlockEntity();
        if (optionalBlockEntity.isEmpty()) { this.onClose(); return; }
        BlockEyeCandy.BlockEntityEyeCandy blockEntity = optionalBlockEntity.get();

        if (isSelectingModel) {
            scrollList.visible = true;
            loadSelectPage(key -> !key.equals(blockEntity.prefabId));
            lblInstruction.alignR = true;
            IDrawing.setPositionAndWidth(lblInstruction, width / 2 + SQUARE_SIZE, height - SQUARE_SIZE - TEXT_HEIGHT, 0);
            lblInstruction.setWidth(width / 2 - SQUARE_SIZE * 2);
            addRenderableWidget(lblInstruction);
        } else {
            scrollList.visible = false;
            loadMainPage(blockEntity);
        }
    }

    @Override
    protected void onBtnClick(String btnKey) {
        updateBlockEntity((blockEntity) -> {
            if (blockEntity.prefabId != btnKey) {
                EyeCandyProperties oldProp = EyeCandyRegistry.elements.get(blockEntity.prefabId);
                if (oldProp != null && oldProp.script != null) {
                    oldProp.script.tryCallDisposeFunctionAsync(blockEntity.scriptContext);
                }
                blockEntity.prefabId = btnKey;
                EyeCandyProperties newProp = EyeCandyRegistry.elements.get(btnKey);
                if (newProp != null && newProp.script != null) {
                    blockEntity.scriptContext = new EyeCandyScriptContext(blockEntity);
                }
                blockEntity.shape = newProp.shape;
                blockEntity.noCollision = newProp.noCollision;
                blockEntity.noMove = newProp.noMove;
            }
        });
    }

    @Override
    protected List<Pair<String, String>> getRegistryEntries() {
        return EyeCandyRegistry.elements.entrySet().stream()
                .map(e -> new Pair<>(e.getKey(), e.getValue().name.getString()))
                .toList();
    }

    private void loadMainPage(BlockEyeCandy.BlockEntityEyeCandy blockEntity) {
        EyeCandyProperties properties = EyeCandyRegistry.elements.get(blockEntity.prefabId);
        IDrawing.setPositionAndWidth(addRenderableWidget(UtilitiesClient.newButton(
                properties != null ? properties.name : Text.literal(blockEntity.prefabId + " (???)"),
                sender -> { isSelectingModel = true; loadPage(); }
        )), SQUARE_SIZE, SQUARE_SIZE, COLUMN_WIDTH * 3);

        tx = new WidgetSlider(
                20 * 2, (int)Math.round(blockEntity.translateX * 100 / 5f) + 20,
                value -> { updateBlockEntity(be -> be.translateX = (value - 20) * 5f / 100f); return "TX " + ((value - 20) * 5) + "cm"; }
        );
        IDrawing.setPositionAndWidth(addRenderableWidget(tx), SQUARE_SIZE, SQUARE_SIZE * 3, (width - SQUARE_SIZE * 2) / 3);

        ty = new WidgetSlider(
                20 * 2, (int)Math.round(blockEntity.translateY * 100 / 5f) + 20,
                value -> { updateBlockEntity(be -> be.translateY = (value - 20) * 5f / 100f); return "TY " + ((value - 20) * 5) + "cm"; }
        );
        IDrawing.setPositionAndWidth(addRenderableWidget(ty), SQUARE_SIZE + (width - SQUARE_SIZE * 2) / 3, SQUARE_SIZE * 3, (width - SQUARE_SIZE * 2) / 3);

        tz = new WidgetSlider(
                20 * 2, (int)Math.round(blockEntity.translateZ * 100 / 5f) + 20,
                value -> { updateBlockEntity(be -> be.translateZ = (value - 20) * 5f / 100f); return "TZ " + ((value - 20) * 5) + "cm"; }
        ); 
        IDrawing.setPositionAndWidth(addRenderableWidget(tz), SQUARE_SIZE + (width - SQUARE_SIZE * 2) / 3 * 2, SQUARE_SIZE * 3, (width - SQUARE_SIZE * 2) / 3);

        rx = new WidgetSlider(
                18 * 2, (int)Math.round(Math.toDegrees(blockEntity.rotateX) / 5f) + 18,
                value -> { updateBlockEntity(be -> be.rotateX = (float)Math.toRadians((value - 18) * 5f)); return "RX " + ((value - 18) * 5) + "°"; }
        );
        IDrawing.setPositionAndWidth(addRenderableWidget(rx), SQUARE_SIZE, SQUARE_SIZE * 4, (width - SQUARE_SIZE * 2) / 3);
        
        ry = new WidgetSlider(
                18 * 2, (int)Math.round(Math.toDegrees(blockEntity.rotateY) / 5f) + 18,
                value -> { updateBlockEntity(be -> be.rotateY = (float)Math.toRadians((value - 18) * 5f)); return "RY " + ((value - 18) * 5) + "°"; }
        );
        IDrawing.setPositionAndWidth(addRenderableWidget(ry), SQUARE_SIZE + (width - SQUARE_SIZE * 2) / 3, SQUARE_SIZE * 4, (width - SQUARE_SIZE * 2) / 3);

        rz = new WidgetSlider(
                18 * 2, (int)Math.round(Math.toDegrees(blockEntity.rotateZ) / 5f) + 18,
                value -> { updateBlockEntity(be -> be.rotateZ = (float)Math.toRadians((value - 18) * 5f)); return "RZ " + ((value - 18) * 5) + "°"; }
        );
        IDrawing.setPositionAndWidth(addRenderableWidget(rz), SQUARE_SIZE + (width - SQUARE_SIZE * 2) / 3 * 2, SQUARE_SIZE * 4, (width - SQUARE_SIZE * 2) / 3);

        textField = new WidgetBetterTextField(Text.translatable("gui.mtrsteamloco.eye_candy.text_field").getString(), 128);
        textField.setResponder(changed -> updateBlockEntity(be -> {
            if(changed.contains("#")) {
                try{
                    String str = changed.replaceAll("#", "");
                    String[] parts = str.split(",");
                    if(parts.length == 2){
                        be.data.put(parts[0], parts[1]);
                    }
                }catch (Exception e){}
                textField.setValue("");
            }else if(changed.contains("%")){
                String newStr = "";
                try{
                    String str = changed.replaceAll("%", "");
                    newStr = str + "," + be.data.get(str);
                }catch (Exception e){}
                textField.setValue(newStr);
            }else if(changed.contains("&")){
                String keys = "/";
                try {
                    List<String> keysList = new ArrayList<>(blockEntity.data.keySet());
                    Collections.sort(keysList);
                    for (String key : keysList) {
                        keys += key + "/";
                    }
                }catch (Exception e){}
                textField.setValue(keys);
            }
        }));

        IDrawing.setPositionAndWidth(addRenderableWidget(textField), SQUARE_SIZE, SQUARE_SIZE * 6, width - SQUARE_SIZE * 2 );
        addDrawableChild(textField);
        addRenderableWidget(textField);
        textField.moveCursorToStart();

        textField2 = new WidgetBetterTextField(Text.translatable("gui.mtrsteamloco.eye_candy.text_field2").getString(), 128);
        textField2.setResponder(changed -> updateBlockEntity(be -> {
            if(changed.contains("#")) {
                try{
                    String str = changed.replaceAll("#", "");
                    String[] parts = str.split("=");
                    if(parts.length == 2){
                        switch (parts[0]){
                            case "TX":
                                be.translateX = Float.parseFloat(parts[1]);
                                tx.setValue((int)Math.round(be.translateX * 100 / 5f) + 20);
                                be.translateX = Float.parseFloat(parts[1]);
                                break;
                            case "TY":
                                be.translateY = Float.parseFloat(parts[1]);
                                ty.setValue((int)Math.round(be.translateY * 100 / 5f) + 20);
                                be.translateY = Float.parseFloat(parts[1]);
                                break;
                            case "TZ":
                                be.translateZ = Float.parseFloat(parts[1]);
                                tz.setValue((int)Math.round(be.translateZ * 100 / 5f) + 20);
                                be.translateZ = Float.parseFloat(parts[1]);
                                break;
                            case "RX":
                                be.rotateX = (float)Math.toRadians(Float.parseFloat(parts[1]));
                                rx.setValue((int)Math.round(Math.toDegrees(be.rotateX) / 5f) + 18);
                                be.rotateX = (float)Math.toRadians(Float.parseFloat(parts[1]));
                                break;
                            case "RY":
                                be.rotateY = (float)Math.toRadians(Float.parseFloat(parts[1]));
                                ry.setValue((int)Math.round(Math.toDegrees(be.rotateY) / 5f) + 18);
                                be.rotateY = (float)Math.toRadians(Float.parseFloat(parts[1]));
                                break;
                            case "RZ":
                                be.rotateZ = (float)Math.toRadians(Float.parseFloat(parts[1]));
                                rz.setValue((int)Math.round(Math.toDegrees(be.rotateZ) / 5f) + 18);
                                be.rotateZ = (float)Math.toRadians(Float.parseFloat(parts[1]));
                                break;
                            case "shape":
                                be.shape = parts[1];
                                break;
                            default:
                                break;
                        }
                    }
                }catch (Exception e){
                    
                }
                textField2.setValue("");
            }else if(changed.contains("%")) {
                String newStr = "";
                try{
                    String str = changed.replaceAll("%", "");
                    switch (str){
                        case "TX":
                            newStr = "TX=" + blockEntity.translateX;
                            break;
                        case "TY":
                            newStr = "TY=" + blockEntity.translateY;
                            break;
                        case "TZ":
                            newStr = "TZ=" + blockEntity.translateZ;
                            break;
                        case "RX":
                            newStr = "RX=" + Math.toDegrees(blockEntity.rotateX);
                            break;
                        case "RY":
                            newStr = "RY=" + Math.toDegrees(blockEntity.rotateY);
                            break;
                        case "RZ":
                            newStr = "RZ=" + Math.toDegrees(blockEntity.rotateZ);
                            break;
                        case "shape":
                            newStr = "shape=" + blockEntity.shape;
                            break;
                        default:
                            break;
                    }
                    textField2.setValue(newStr);
                }catch (Exception e){

                }
            }
        }));
        IDrawing.setPositionAndWidth(addRenderableWidget(textField2), SQUARE_SIZE, SQUARE_SIZE * 7 + 10, width - SQUARE_SIZE * 2 );
        addDrawableChild(textField2);
        addRenderableWidget(textField2);
        textField2.moveCursorToStart();

        addRenderableWidget(new WidgetBetterCheckbox(SQUARE_SIZE, SQUARE_SIZE * 9 + 10, COLUMN_WIDTH * 2, SQUARE_SIZE,
                Text.translatable("gui.mtrsteamloco.eye_candy.full_light"),
                checked -> updateBlockEntity((be) -> be.fullLight = checked)
        )).setChecked(blockEntity.fullLight);

        addRenderableWidget(new WidgetBetterCheckbox(SQUARE_SIZE, SQUARE_SIZE * 10 + 13, COLUMN_WIDTH * 2, SQUARE_SIZE,
        Text.literal("当作站台"),
        checked -> updateBlockEntity((be) -> be.platform = checked)
        )).setChecked(blockEntity.platform);

        IDrawing.setPositionAndWidth(addRenderableWidget(UtilitiesClient.newButton(
                Text.literal("X"), sender -> this.onClose()
        )), width - SQUARE_SIZE * 2, height - SQUARE_SIZE * 2, SQUARE_SIZE);
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
        if (isSelectingModel) {
            isSelectingModel = false;
            loadPage();
        } else {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        textField.tick();
        textField2.tick();
    }
}
