package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.Main;
import mtr.mappings.UtilitiesClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.components.events.GuiEventListener;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import mtr.screen.WidgetBetterTextField;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.CreativeModeTab;
#if MC_VERSION >= "12000"
import net.minecraft.world.item.CreativeModeTabs;
#endif
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import mtr.mappings.Text;
import net.minecraft.world.level.block.state.BlockState;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateHoldingItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.client.gui.Font;
import me.shedaniel.math.Rectangle;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.Mth;
#if MC_VERSION >= "12000"
import net.minecraft.client.gui.GuiGraphics;
#else
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
#endif
import static cn.zbx1425.mtrsteamloco.item.CompoundCreator.*;
import mtr.client.IDrawing;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Comparator;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.Iterator;
import java.util.Collection;

public class CompoundCreatorScreen extends Screen {
    public static void setScreen(Screen parent) {
        CompoundCreatorScreen screen = new CompoundCreatorScreen(parent);
        if (screen.load()) {
            Minecraft.getInstance().setScreen(screen);
        }
    }

    private static final String TAG_TASKS = "tasks";
    public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");
    private static final ResourceLocation WHITE = new ResourceLocation("minecraft", "textures/block/white_concrete_powder.png");
    private Screen parent;
    private List<Entry> entries = new ArrayList<>();
    private Entry selectedEntry = null;
    // private final WidgetLabel title = new WidgetLabel(0, 0, 20, Text.translatable("gui.mtrsteamloco.compound_creator.title"), () -> {});
    private int scroll = 0; 
    private Rectangle scissor = null;
    private boolean draggingSlider = false;

    private Button btnAdd = UtilitiesClient.newButton(20, Text.literal("+"), button -> addEntry());
    private Button btnRemove = UtilitiesClient.newButton(20, Text.literal("-"), button -> removeEntry());
    private Button btnClear = UtilitiesClient.newButton(20, Text.literal("Clear"), button -> clearEntries());
    private Button btnClose = UtilitiesClient.newButton(20, Text.literal("X"), button -> onClose());

    CompoundCreatorScreen(Screen parent) {
        super(Text.translatable(""));
        this.parent = parent;
    }

    public boolean load() {
        List<SliceTask> tasks = new ArrayList<>();
        if (getTag() == null) return false;
        CompoundTag tag = getTag();
        if (!tag.contains(TAG_TASKS)) return true;
        CompoundTag tasksTag = tag.getCompound(TAG_TASKS).copy();
        for (String key : tasksTag.getAllKeys()) {
            tasks.add(new SliceTask(tasksTag.getCompound(key)));
        }
        tasks.sort(Comparator.comparingInt(sliceTask -> sliceTask.order));
        List<Entry> entries = new ArrayList<>();
        for (SliceTask task : tasks) {
            entries.add(new Entry(task));
        }
        this.entries = entries;
        selectedEntry = entries.isEmpty()? null : entries.get(0);
        return true;
    }

    public static CompoundTag getTag() {
        if (Minecraft.getInstance().player == null) return null;
        ItemStack item = Minecraft.getInstance().player.getMainHandItem();
        if (!item.is(Main.COMPOUND_CREATOR.get())) return null;
        CompoundTag tag = item.getOrCreateTag();
        return tag;
    }

    public static void updateTag(Consumer<CompoundTag> modifier) {
        if (Minecraft.getInstance().player == null) return;
        ItemStack item = Minecraft.getInstance().player.getMainHandItem();
        if (!item.is(Main.COMPOUND_CREATOR.get())) return;
        CompoundTag tag = item.getOrCreateTag();
        modifier.accept(tag);
        PacketUpdateHoldingItem.sendUpdateC2S();
    }

    @Override
    protected void init() {
    }

    private void update() {
        updateTag(tag -> {
            List<Entry> copy = new ArrayList<>(entries);
            CompoundTag tasksTag = new CompoundTag();
            for (int i = 0; i < copy.size(); i++) {
                copy.get(i).task.order = i;
                tasksTag.put(i + "", copy.get(i).task.toCompoundTag());
            }
            tag.put(TAG_TASKS, tasksTag);
        });
    }


    private void clearEntries() {
        entries.clear();
        selectedEntry = null;
        update();
    }

    private void addEntry() {
        Entry entry = new Entry(new SliceTask(0, entries.size() + "", 11, 11, 0, 1, null, new Integer[11 * 11]));
        entries.add(entry);
        selectedEntry = entry;
        update();
    }

    private void removeEntry() {
        if (selectedEntry == null) return;
        entries.remove(selectedEntry);
        if (entries.isEmpty()) selectedEntry = null;
        else selectedEntry = entries.get(0);
        update();
    }

    @Override
#if MC_VERSION >= "12000"
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float partialTick) {
        renderDirtBackground(matrices);
#else
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTick) {
        renderDirtBackground(0);
#endif
        super.render(matrices, mouseX, mouseY, partialTick);
        fill(matrices, 0, 38, width, height - 38, 0x90000000);
        
        String title = "我是标题";
#if MC_VERSION >= "12000"
        matrices.drawCenteredString(minecraft.font, title, width / 2, 18, 0xFFFFFFFF);
#else
        Gui.drawCenteredString(matrices, minecraft.font, title, width / 2, 18, 0xFFFFFFFF);
#endif
        IDrawing.setPositionAndWidth(btnAdd, width - 50, 60, 40);
        IDrawing.setPositionAndWidth(btnRemove, width - 50, 90, 40);
        IDrawing.setPositionAndWidth(btnClear, width - 50, 120, 40);
        IDrawing.setPositionAndWidth(btnClose, 10, 10, 20);
        btnAdd.render(matrices, mouseX, mouseY, partialTick);
        btnRemove.render(matrices, mouseX, mouseY, partialTick);
        btnClear.render(matrices, mouseX, mouseY, partialTick);
        btnClose.render(matrices, mouseX, mouseY, partialTick);
        scissor = new Rectangle(0, 40, width, height - 80);
        ScissorsHandler.INSTANCE.scissor(scissor);
        checkAndScroll(scroll);
        int y = 40 + scroll;
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).render(matrices, mouseX, mouseY, i, y, partialTick);
            y += entries.get(i).height();
        }
        ScissorsHandler.INSTANCE.clearScissors();
        if (canScoll()) {
            int[] pas = getSliderPositionAndSize();
            blit(matrices, WHITE, pas[0], pas[1], pas[2], pas[3]);
        }
    }

    private void setScroll(int mouseY) {
        int[] pas = getSliderPositionAndSize();
        int sh = pas[3];
        int maxd = scissor.height - sh;
        int dy = mouseY - sh / 2 - scissor.y;
        int maxScroll = -entries.size() * Entry.height() + scissor.height;
        checkAndScroll((int) (dy / ((float) maxd) * maxScroll));
    }

    private void checkAndScroll(int temp) {
        if (!canScoll()) {
            scroll = 0;
            return;
        }
        if (temp > 0) temp = 0;
        int min = -entries.size() * Entry.height() + scissor.height;
        if (temp < min) temp = min;
        scroll = temp;
    }


    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        if (!canScoll()) return super.mouseScrolled(x, y, amount);
        if (scissor.x <= x && x <= scissor.x + scissor.width && scissor.y <= y && y <= scissor.y + scissor.height) {
            checkAndScroll(scroll + 100 * (int) amount);
            return true;
        }
        return super.mouseScrolled(x, y, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int i) {
        if (!canScoll()) return super.mouseClicked(mouseX, mouseY, i);
        int[] spas = getSliderPositionAndSize();
        if (isMouseOverSlider(mouseX, mouseY)) {
            setScroll((int) mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, i);
    }

    @Override
    public boolean mouseDragged(double sx, double sy, int i, double tx, double ty) {
        if (isMouseOverSlider(sx, sy) || draggingSlider) {
            setScroll((int) (sy + ty));
            draggingSlider = true;
            return true;
        }
        return super.mouseDragged(sx, sy, i, tx, ty);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int i) {
        if (draggingSlider) {
            draggingSlider = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, i);
    }

    private boolean isMouseOverSlider(double mouseX, double mouseY) {
        if (!canScoll()) return false;
        int[] pas = getSliderPositionAndSize();
        return pas[0] <= mouseX && mouseX <= pas[0] + pas[2] && pas[1] <= mouseY && mouseY <= pas[1] + pas[3];
    }

    private int[] getSliderPositionAndSize() {
        float ah = (float) entries.size() * Entry.height();
        float th = (float) scissor.height;
        int h = (int) (th / ah * th);
        int py = scissor.y + (int) (-1F * scroll / ah * th);
        return new int[]{Entry.x() + Entry.width(width), py, (int) (width * 0.01F), h};
    }

    private boolean canScoll() {
        return entries.size() * Entry.height() > scissor.height;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> result = new ArrayList<>();
        result.addAll(super.children());
        for (Entry entry : entries) {
            result.addAll(entry.children());
        }
        // result.add(title);
        result.add(btnAdd);
        result.add(btnRemove);
        result.add(btnClear);
        result.add(btnClose);
        return result;
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

    private static void fill(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, width, height, color);
    }
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
#endif

    @Override
    public void onClose() {
        update();
        minecraft.setScreen(parent);
    }

    public class Entry implements GuiEventListener{
        public SliceTask task;
        private int y;
        private static int x = 20;
        private static int height = 20;
        private WidgetBetterTextField nameField = new WidgetBetterTextField("", 256);
        private Button enter = UtilitiesClient.newButton(Text.literal("Enter"), btn -> enter());
        private Button up = UtilitiesClient.newButton(Text.literal("▲"), btn -> moveUp());
        private Button down = UtilitiesClient.newButton(Text.literal("▼"), btn -> moveDown());
        private List<GuiEventListener> children = Arrays.asList(nameField, enter, up, down, this);

        public Entry(SliceTask task) {
            this.task = task;
            nameField.setResponder(this::updateName);
            nameField.setValue(task.name);
            nameField.moveCursorToStart();
        }

        public List<? extends GuiEventListener> children() {
            return children;
        }

        public void enter() {
            minecraft.setScreen(new TaskScreen(task));
        }

        public void updateName(String name) {
            task.name = name;
            update();
            selectedEntry = this;
        }

        public void moveUp() {
            int index = entries.indexOf(this);
            if (index > 0) {
                entries.set(index, entries.get(index - 1));
                entries.set(index - 1, this);
                update();
            }
            selectedEntry = this;
        }

        private boolean isFocused = false;
        
        public boolean isFocused() {
            return isFocused;
        }

        public void setFocused(boolean focused) {
            isFocused = focused;
        }

        public void moveDown() {
            int index = entries.indexOf(this);
            if (index < entries.size() - 1) {
                entries.set(index, entries.get(index + 1));
                entries.set(index + 1, this);
                update();
            }
            selectedEntry = this;
        }

    #if MC_VERSION >= "12000"
        public void render(GuiGraphics matrices, int mouseX, int mouseY, int num, int y, float partialTick) {
    #else
        public void render(PoseStack matrices, int mouseX, int mouseY, int num, int y, float partialTick) {
    #endif
            this.y = y;
            if (isSelected()) {
                fill(matrices, 0, y(), width - 55, y() + height(), 0xa0eeeeee);
            } else if (isMouseOver(mouseX, mouseY)) {
                fill(matrices, 0, y(), width - 55, y() + height(), 0x40eeeeee);
            }
            y += 2;
            int count = width() / 20;
            int x = x();
            IDrawing.setPositionAndWidth(nameField, x, y, count * 7);
            x += count * 8;
            IDrawing.setPositionAndWidth(enter, x, y, count * 6);
            x += count * 7;
            IDrawing.setPositionAndWidth(up, x, y, count * 2);
            x += count * 3;
            IDrawing.setPositionAndWidth(down, x, y, count * 2);
            nameField.render(matrices, mouseX, mouseY, partialTick);
            enter.render(matrices, mouseX, mouseY, partialTick);
            up.render(matrices, mouseX, mouseY, partialTick);
            down.render(matrices, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (x() <= mouseX && mouseX <= x() + width() && y() <= mouseY && mouseY <= y() + height()) {
                return true;
            }
            return false;
        }

        public static int x() {
            return 20;
        }

        public int y() {
            return y;
        }

        public int width() {
            return width - 80;
        }

        public static int width(int width) {
            return width - 80;
        }

        public static int height() {
            return 24;
        }

        public boolean isSelected() {
            return selectedEntry == this;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int i) {
            if (isMouseOver(mouseX, mouseY)) {
                selectedEntry = this;
                return true;
            } else {
                return false;
            }
        }
    }

    public class TaskScreen extends Screen {
        public SliceTask task;
        private int tx = 0;
        private int ty = 0;
        private Button btnReturn = UtilitiesClient.newButton(Text.literal("X"), btn -> onClose());
        private Rectangle scissor = new Rectangle(0, 0, 0, 0);
        private List<Square> canvas = new ArrayList<>();
        private Inventory inventory = new Inventory();

        private WidgetBetterTextField nameField = new WidgetBetterTextField("", 256);
        private WidgetBetterTextField widthField = new WidgetBetterTextField("", 256);
        private WidgetBetterTextField heightField = new WidgetBetterTextField("", 256);
        private Square now = new Square(0, 0, null, square -> square.state = null, square -> true);

        public TaskScreen(SliceTask task) {
            super(Text.translatable(""));
            this.task = task;
            reload();
        }

        private void setWidthAndHeight(int width, int height) {
            if (width != task.width || height != task.height) ;
            else return;
            task.width = width;
            task.height = height;
            task.blockIds = new Integer[width * height];
            update();
            reload();
        }

        private void updateTask() {
            task.blockIds = new Integer[canvas.size()];
            for (int i = 0; i < canvas.size(); i++) {
                task.blockIds[i] = canvas.get(i).state == null? null : Block.getId(canvas.get(i).state);
            }
            update();
        }

        private void reload() {
            List<Square> canvas = new ArrayList<>();
            for (int i = 0; i < task.blockIds.length; i++) {
                int p = i;
                Consumer<Square> consumer = square -> {
                    square.state = now.state;
                    updateTask();
                };
                if (task.blockIds[i] == null) canvas.add(new Square(i / task.width, i % task.width, null, consumer, square -> true));
                else canvas.add(new Square(i / task.width, i % task.width, Block.stateById(task.blockIds[i]), consumer, square -> true));
            }
            this.canvas = canvas;
        }

        @Override
        protected void init() {
            nameField.setValue(task.name);
            nameField.moveCursorToStart();
            widthField.setValue(Integer.toString(task.width));
            heightField.setValue(Integer.toString(task.height));
            widthField.moveCursorToStart();
            heightField.moveCursorToStart();

            nameField.setResponder(str -> {
                task.name = str;
                updateTask();   
            });
            widthField.setResponder(str -> {
                try {
                    setWidthAndHeight(Integer.parseInt(str), task.height);
                    widthField.setTextColor(0x00ff00);
                } catch (NumberFormatException e) {
                    widthField.setTextColor(0xff0000);
                }
            });
            heightField.setResponder(str -> {
                try {
                    setWidthAndHeight(task.width, Integer.parseInt(str));
                    heightField.setTextColor(0x00ff00);
                } catch (NumberFormatException e) {
                    heightField.setTextColor(0xff0000);
                }
            });
            IDrawing.setPositionAndWidth(btnReturn, 10, 10, 20);
            IDrawing.setPositionAndWidth(nameField, 40, 10, 30);
            IDrawing.setPositionAndWidth(widthField, 80, 10, 30);
            IDrawing.setPositionAndWidth(heightField, 120, 10, 30);
            addRenderableWidget(btnReturn);
            addRenderableWidget(nameField);
            addRenderableWidget(widthField);
            addRenderableWidget(heightField);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            List<GuiEventListener> result = new ArrayList<>();
            result.addAll(super.children());
            result.addAll(canvas);
            result.addAll(inventory.children());
            result.add(now);
            return result;
        }

        @Override
    #if MC_VERSION >= "12000"
        public void render(GuiGraphics matrices, int mouseX, int mouseY, float partialTick) {
            renderDirtBackground(matrices);
    #else
        public void render(PoseStack matrices, int mouseX, int mouseY, float partialTick) {
            renderDirtBackground(0);
    #endif

            super.render(matrices, mouseX, mouseY, partialTick);
            
            scissor = new Rectangle(30, 30, width - 30 - Inventory.width, height - 30 - 30);
            IDrawing.setPositionAndWidth(btnReturn, 10, 10, 20);
            IDrawing.setPositionAndWidth(btnReturn, 10, 10, 20);
            IDrawing.setPositionAndWidth(nameField, 40, 10, 30);
            IDrawing.setPositionAndWidth(widthField, 80, 10, 20);
            IDrawing.setPositionAndWidth(heightField, 110, 10, 20);

            int x = this.tx + 40;
            int y = this.ty + 40;
            for (int i = 0; i < canvas.size(); i++) {
                canvas.get(i).render(matrices, mouseX, mouseY, x + i % task.width * Square.length, y + i / task.width * Square.length, partialTick);
            }
            now.render(matrices, mouseX, mouseY, 141, 11, partialTick);

            inventory.render(matrices, mouseX, mouseY, partialTick);
        }

        @Override
        public void onClose() {
            minecraft.setScreen(CompoundCreatorScreen.this);
        }

        private void setNowBlock(BlockState state) {
            now.state = state;
        }

    #if MC_VERSION >= "12000"
        private void renderBlockState(GuiGraphics matrices, int x, int y, float partialTick, BlockState state) {
            matrices.renderFakeItem(new ItemStack(state.getBlock()), x, y);
        }
    #else
        private void renderBlockState(PoseStack matrices, int x, int y, float partialTick, BlockState state) {
            ItemStack stack = state.getBlock().asItem().getDefaultInstance();
            if (!stack.isEmpty()) {
                PoseStack posestack = RenderSystem.getModelViewStack();
                float f = (float)stack.getPopTime() - partialTick;
                if (f > 0.0F) {
                    float f1 = 1.0F + f / 5.0F;
                    posestack.pushPose();
                    posestack.translate((double)(x + 8), (double)(y + 12), 0.0D);
                    posestack.scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                    posestack.translate((double)(-(x + 8)), (double)(-(y + 12)), 0.0D);
                    RenderSystem.applyModelViewMatrix();
                }

                itemRenderer.renderAndDecorateItem(minecraft.getInstance().player, stack, x, y, 0);
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                if (f > 0.0F) {
                    posestack.popPose();
                    RenderSystem.applyModelViewMatrix();
                }

                itemRenderer.renderGuiItemDecorations(minecraft.font, stack, x, y);
            }
        }
    #endif

        public class Square implements GuiEventListener {
            public static int length = 18;
            public int x;
            public int y;
            public BlockState state;
            public Consumer<Square> consumer;
            public Function<Square, Boolean> highlight = square -> false;

            public Square(int x, int y, BlockState state, Consumer<Square> consumer, Function<Square, Boolean> highlight) {
                this.x = x;
                this.y = y;
                this.state = state;
                this.consumer = consumer;
                if (highlight != null) this.highlight = highlight;
            }

        #if MC_VERSION >= "12000"
            public void render(GuiGraphics matrices, int mouseX, int mouseY, int tx, int ty, float partialTick) {
        #else
            public void render(PoseStack matrices, int mouseX, int mouseY, int tx, int ty, float partialTick) {
        #endif
                x = tx;
                y = ty;
                if (!visible()) return;
                fill(matrices, x, y, x + length, y + length, 0xffb0b0b0);
                if (state != null) {
                    renderBlockState(matrices, x + 1, y + 1, partialTick, state);
                    fill(matrices, x + 1, y + 1, x + length - 1, y + length - 1, highlight.apply(this) ? 0xffacec92 : 0xff113d00);
                } else {
                    fill(matrices, x + 1, y + 1, x + length - 1, y + length - 1, highlight.apply(this) ? 0xff808080 : 0xff404040);
                }
                if (isMouseOver(mouseX, mouseY)) {
                    drawText(matrices, minecraft.font, (state == null ? "Empty" : state.getBlock().getName().getString()), 0, height - 10, 0xffffffff);
                    if (state != null) {
                        Block block = state.getBlock();
                        StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();
                        Collection<Property<?>> collection = statedefinition.getProperties();
                        StringBuilder sb = new StringBuilder();
                        sb.append("/");
                        for (Property<?> property : collection) {
                            sb.append(property.getName());
                            sb.append(": ");
                            sb.append(state.getValue(property).toString());
                            sb.append("/");
                        }
                        String s = sb.toString();
                        Main.LOGGER.info(s);
                        drawText(matrices, minecraft.font, s, 0, height - 20, 0xffffffff);
                    }
                }
            }

            public boolean isMouseOver(double mouseX, double mouseY) {
                if (x <= mouseX && mouseX <= x + length && y <= mouseY && mouseY <= y + length) {
                    return true;
                }
                return false;
            }

            private boolean visible() {
                return x >= -length && x <= TaskScreen.this.width && y >= -length && y <= TaskScreen.this.height;
            }

            public boolean mouseClicked(double mouseX, double mouseY, int i) {
                if (isMouseOver(mouseX, mouseY)) {
                    consumer.accept(this);
                    return true;
                }
                return false;
            }

            private boolean isFocused = false;

            public boolean isFocused() {
                return isFocused;
            }

            public void setFocused(boolean focused) {
                isFocused = focused;
            }
        }

        public class Inventory implements GuiEventListener {
            public static final int width = 100;
            public static final int col = 5;
            private int scroll = 0;
            private List<Square> blocksList = new ArrayList<>();
            private boolean draggingSlider = false;
            private Rectangle scissor = new Rectangle(19, 19, 8, 10);

            public Inventory() {
                NonNullList<ItemStack> items = NonNullList.create();
                /* for (BlockState state : Block.BLOCK_STATE_REGISTRY) {
                    blocksList.add(new Square(0, 0, state, square -> {
                        now.state = square.state;
                    }, square -> square.state == now.state));
                }*/
        #if MC_VERSION >= "12000"
                for (CreativeModeTab tab : CreativeModeTabs.tabs()) {
                    for (ItemStack stack : tab.getDisplayItems()) {
                        items.add(stack);
                    }
                }
        #else
                for (CreativeModeTab tab : CreativeModeTab.TABS) {
                    if (tab == CreativeModeTab.TAB_HOTBAR) continue;
                    tab.fillItemList(items);
                }
        #endif
                blocksList.add(new Square(0, 0, Blocks.AIR.defaultBlockState(), square -> {
                        now.state = square.state;
                    }, square -> square.state == now.state));
                for (ItemStack stack : items) {
                    Item item = stack.getItem();
                    if (item instanceof BlockItem) {
                        BlockState state = ((BlockItem) item).getBlock().defaultBlockState();
                        blocksList.add(new Square(0, 0, state, square -> {
                            now.state = square.state;
                        }, square -> square.state == now.state));
                    }
                }
            }

            int ka = 0;
            int kb = 0;

        #if MC_VERSION >= "12000"
            public void render(GuiGraphics matrices, int mouseX, int mouseY, float partialTick) {
        #else
            public void render(PoseStack matrices, int mouseX, int mouseY, float partialTick) {
        #endif
                scissor = new Rectangle(TaskScreen.this.width - width, 0, width, TaskScreen.this.height);
                checkAndScroll(scroll);
                int x = TaskScreen.this.width - width;
                for (int i = 0; i < blocksList.size(); i++) {
                    blocksList.get(i).render(matrices, mouseX, mouseY, x + i % col * Square.length, scroll + i / col * Square.length, partialTick);
                }
                if (canScoll()) {
                    int[] pas = getSliderPositionAndSize();
                    fill(matrices, pas[0], pas[1], pas[0] + pas[2], pas[1] + pas[3], 0xffb0b0b0);
                }
            }

            public List<? extends GuiEventListener> children() {
                List<GuiEventListener> result = new ArrayList<>();
                result.add(this);
                result.addAll(blocksList);
                return result;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int i) {
                if (!canScoll()) return false;
                int[] spas = getSliderPositionAndSize();
                if (isMouseOverSlider(mouseX, mouseY)) {
                    setScroll((int) mouseY);
                    return true;
                }
                return false;
            }

            private boolean isFocused = false;

            public boolean isFocused() {
                return isFocused;
            }

            public void setFocused(boolean focused) {
                isFocused = focused;
            }

            @Override
            public boolean mouseDragged(double sx, double sy, int i, double tx, double ty) {
                if (isMouseOverSlider(sx, sy) || draggingSlider) {
                    setScroll((int) (sy + ty));
                    draggingSlider = true;
                    return true;
                }
                return false;
            }

            @Override
            public boolean mouseScrolled(double x, double y, double amount) {
                if (!canScoll()) return false;
                if (isMouseOver(x, y)) {
                    checkAndScroll(scroll + 2 * (int) amount);
                    return true;
                }
                return false;
            }

            public boolean isMouseOver(double mouseX, double mouseY) {
                return scissor.x <= mouseX && mouseX <= scissor.x + scissor.width && scissor.y <= mouseY && mouseY <= scissor.y + scissor.height;
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int i) {
                if (draggingSlider) {
                    draggingSlider = false;
                    return true;
                }
                return false;
            }

            private boolean isMouseOverSlider(double mouseX, double mouseY) {
                if (!canScoll()) return false;
                int[] pas = getSliderPositionAndSize();
                return pas[0] <= mouseX && mouseX <= pas[0] + pas[2] && pas[1] <= mouseY && mouseY <= pas[1] + pas[3];
            }

            private int[] getSliderPositionAndSize() {
                float ah = (float) ah();
                float th = (float) scissor.height;
                int h = (int) (th / ah * th);
                int py = scissor.y + (int) (-1F * scroll / ah * th);
                if (h > 5) {
                    return new int[]{TaskScreen.this.width - 5, py, 5, h};
                } else {
                    return new int[]{TaskScreen.this.width - 5, py, 5, 5};
                }
            }

            private boolean canScoll() {
                return ah() > scissor.height;
            }

            private int ah() {
                return (blocksList.size() / col) * Square.length;
            }

            private void setScroll(int mouseY) {
                int[] pas = getSliderPositionAndSize();
                int sh = pas[3];
                int maxd = scissor.height - sh;
                int dy = mouseY - sh / 2 - scissor.y;
                int maxScroll = -ah() + scissor.height;
                checkAndScroll((int) (dy / ((float) maxd) * maxScroll));
            }

            private void checkAndScroll(int temp) {
                if (!canScoll()) {
                    scroll = 0;
                    return;
                }
                if (temp > 0) temp = 0;
                int min = -ah() + scissor.height;
                if (temp < min) temp = min;
                scroll = temp;
            }
        }
    }
}