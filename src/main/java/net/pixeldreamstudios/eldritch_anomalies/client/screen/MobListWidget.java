package net.pixeldreamstudios.eldritch_anomalies.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.pixeldreamstudios.eldritch_anomalies.config.MobConfigHelper;
import net.pixeldreamstudios.eldritch_anomalies.config.MobGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Environment(EnvType.CLIENT)
public class MobListWidget extends ContainerObjectSelectionList<MobListWidget.MobEntry> {

    private static final int ENTRY_HEIGHT = 36;
    private static final int RENDER_SIZE = 32;
    private static final int BUTTON_WIDTH = 130;

    private final MobManagementScreen parent;
    private final List<MobEntry> allEntries = new ArrayList<>();

    public MobListWidget(Minecraft minecraft, MobManagementScreen parent, int x, int width, int height, int y) {
        super(minecraft, width, height, y, ENTRY_HEIGHT);
        this.setX(x);
        this.parent = parent;
        buildEntries();
    }

    private void buildEntries() {
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (entityType.getCategory() == MobCategory.MISC) continue;
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            if (id == null) continue;
            MobEntry entry = new MobEntry(entityType, MobConfigHelper.getGroup(entityType));
            allEntries.add(entry);
        }
        allEntries.sort((a, b) -> a.entityType.getDescription().getString()
                .compareToIgnoreCase(b.entityType.getDescription().getString()));
    }

    public void refresh(String search, MobGroup groupFilter, String namespaceFilter) {
        clearEntries();
        setScrollAmount(0);
        String lower = search.toLowerCase().trim();
        for (MobEntry entry : allEntries) {
            String name = entry.entityType.getDescription().getString().toLowerCase();
            String ns = namespace(entry);
            if (!lower.isEmpty() && !name.contains(lower) && !ns.contains(lower)) continue;
            if (groupFilter != null && entry.currentGroup != groupFilter) continue;
            if (namespaceFilter != null && !ns.equals(namespaceFilter)) continue;
            addEntry(entry);
        }
    }

    public List<MobEntry> getAllEntries() {
        return allEntries;
    }

    public List<MobEntry> getVisibleEntries() {
        List<MobEntry> visible = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            visible.add(getEntry(i));
        }
        return visible;
    }

    public int getVisibleCount() {
        return getItemCount();
    }

    public TreeMap<String, Integer> rebuildNamespaceCounts() {
        TreeMap<String, Integer> counts = new TreeMap<>();
        for (int i = 0; i < getItemCount(); i++) {
            String ns = namespace(getEntry(i));
            counts.merge(ns, 1, Integer::sum);
        }
        return counts;
    }

    private static String namespace(MobEntry entry) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entry.entityType);
        return id != null ? id.getNamespace() : "unknown";
    }

    private static int groupRowColor(MobGroup group) {
        return switch (group) {
            case ALLOWED -> 0x1800FF44;
            case BLACKLISTED -> 0x18FF2222;
            case ALWAYS_ELITE -> 0x18FFEE00;
            case ALWAYS_ULTRA -> 0x18FF8800;
            case ALWAYS_ELDRITCH -> 0x18AA00FF;
            default -> 0;
        };
    }

    @Override
    public int getRowWidth() {
        return width - 20;
    }

    @Override
    protected int getScrollbarPosition() {
        return getX() + width - 6;
    }

    @Environment(EnvType.CLIENT)
    public class MobEntry extends ContainerObjectSelectionList.Entry<MobEntry> {

        final EntityType<?> entityType;
        MobGroup currentGroup;
        private final CycleButton<MobGroup> groupButton;
        private LivingEntity entityInstance;
        private boolean entityResolved = false;

        MobEntry(EntityType<?> entityType, MobGroup initialGroup) {
            this.entityType = entityType;
            this.currentGroup = initialGroup;
            this.groupButton = CycleButton.builder(MobEntry::groupLabel)
                    .withValues(MobGroup.values())
                    .withInitialValue(initialGroup)
                    .create(0, 0, BUTTON_WIDTH, 20, Component.empty(), (btn, val) -> {
                        this.currentGroup = val;
                        parent.onGroupChanged();
                        if (parent.activeGroupFilter != null) parent.refreshList();
                    });
        }

        public void setGroup(MobGroup group) {
            this.currentGroup = group;
            this.groupButton.setValue(group);
        }

        private static Component groupLabel(MobGroup group) {
            return Component.translatable(group.translationKey());
        }

        private LivingEntity getOrCreateEntity() {
            if (!entityResolved) {
                entityResolved = true;
                try {
                    ClientLevel level = Minecraft.getInstance().level;
                    if (level != null) {
                        var entity = entityType.create(level);
                        if (entity instanceof LivingEntity living) {
                            entityInstance = living;
                        }
                    }
                } catch (Exception ignored) {}
            }
            return entityInstance;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int rowColor = groupRowColor(currentGroup);
            if (rowColor != 0) {
                graphics.fill(left, top, left + width, top + height, rowColor);
            }
            if (hovered) {
                graphics.fill(left, top, left + width, top + height, 0x22FFFFFF);
            }

            int renderLeft = left + 2;
            int renderTop = top + 2;
            int renderRight = renderLeft + RENDER_SIZE;
            int renderBottom = renderTop + RENDER_SIZE;

            LivingEntity entity = getOrCreateEntity();
            if (entity != null) {
                try {
                    InventoryScreen.renderEntityInInventoryFollowsMouse(
                            graphics, renderLeft, renderTop, renderRight, renderBottom,
                            12, 0.0f,
                            (float) (renderLeft + RENDER_SIZE / 2),
                            (float) (renderTop + RENDER_SIZE / 2),
                            entity);
                } catch (Exception ignored) {
                    graphics.fill(renderLeft, renderTop, renderRight, renderBottom, 0x40808080);
                }
            } else {
                graphics.fill(renderLeft, renderTop, renderRight, renderBottom, 0x40808080);
            }

            int textX = left + RENDER_SIZE + 10;
            int textMaxWidth = left + width - BUTTON_WIDTH - 8 - textX;
            graphics.drawString(Minecraft.getInstance().font, entityType.getDescription(), textX, top + 8, 0xFFFFFF);
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            if (id != null) {
                String idStr = Minecraft.getInstance().font.plainSubstrByWidth(id.toString(), textMaxWidth);
                graphics.drawString(Minecraft.getInstance().font, idStr, textX, top + 20, 0x999999, false);
            }

            groupButton.setX(left + width - BUTTON_WIDTH - 4);
            groupButton.setY(top + (height - 20) / 2);
            groupButton.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(groupButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(groupButton);
        }
    }
}

