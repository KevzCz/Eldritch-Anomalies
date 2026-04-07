package net.pixeldreamstudios.eldritch_anomalies.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class NamespacePanelWidget extends ObjectSelectionList<NamespacePanelWidget.NamespaceEntry> {

    private static final int ENTRY_HEIGHT = 22;

    private final MobManagementScreen parent;
    private final List<NamespaceEntry> allEntries = new ArrayList<>();

    public NamespacePanelWidget(Minecraft minecraft, MobManagementScreen parent, int x, int width, int height, int y) {
        super(minecraft, width, height, y, ENTRY_HEIGHT);
        this.setX(x);
        this.parent = parent;
    }

    public void build(Map<String, Integer> counts) {
        clearEntries();
        allEntries.clear();

        NamespaceEntry all = new NamespaceEntry(null, -1);
        allEntries.add(all);
        addEntry(all);

        counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    NamespaceEntry entry = new NamespaceEntry(e.getKey(), e.getValue());
                    allEntries.add(entry);
                    addEntry(entry);
                });

        setSelected(all);
        setScrollAmount(0);
    }

    @Override
    public int getRowWidth() {
        return width - 6;
    }

    @Override
    protected int getScrollbarPosition() {
        return getX() + width - 4;
    }

    @Override
    protected void renderHeader(GuiGraphics graphics, int x, int y) {
    }

    @Override
    protected void renderListBackground(GuiGraphics graphics) {
    }

    @Override
    protected void renderListSeparators(GuiGraphics graphics) {
    }

    @Environment(EnvType.CLIENT)
    public class NamespaceEntry extends ObjectSelectionList.Entry<NamespaceEntry> {

        final String namespace;
        final int count;

        NamespaceEntry(String namespace, int count) {
            this.namespace = namespace;
            this.count = count;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int rowWidth, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            boolean selected = NamespacePanelWidget.this.getSelected() == this;
            if (selected) {
                graphics.fill(left, top, left + rowWidth, top + height - 1, 0x60335588);
            } else if (hovered) {
                graphics.fill(left, top, left + rowWidth, top + height - 1, 0x30FFFFFF);
            }

            String label = namespace == null
                    ? Minecraft.getInstance().font.plainSubstrByWidth(Component.translatable("filter.eldritch-anomalies.all").getString(), rowWidth - 8)
                    : Minecraft.getInstance().font.plainSubstrByWidth(namespace, rowWidth - (count >= 0 ? 28 : 8));

            graphics.drawString(Minecraft.getInstance().font, label, left + 4, top + (height - Minecraft.getInstance().font.lineHeight) / 2, 0xFFFFFF);

            if (count >= 0) {
                String countStr = String.valueOf(count);
                int cw = Minecraft.getInstance().font.width(countStr);
                graphics.drawString(Minecraft.getInstance().font, countStr, left + rowWidth - cw - 6, top + (height - Minecraft.getInstance().font.lineHeight) / 2, 0xAAAAAA, false);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            NamespacePanelWidget.this.setSelected(this);
            parent.onNamespaceSelected(namespace);
            return true;
        }

        @Override
        public Component getNarration() {
            return namespace != null ? Component.literal(namespace) : Component.translatable("filter.eldritch-anomalies.all");
        }
    }
}
