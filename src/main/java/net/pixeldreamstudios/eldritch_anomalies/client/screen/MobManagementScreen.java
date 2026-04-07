package net.pixeldreamstudios.eldritch_anomalies.client.screen;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.pixeldreamstudios.eldritch_anomalies.DynamicTagRegistry;
import net.pixeldreamstudios.eldritch_anomalies.EldritchAnomalies;
import net.pixeldreamstudios.eldritch_anomalies.config.EldritchAnomaliesConfig;
import net.pixeldreamstudios.eldritch_anomalies.config.MobGroup;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class MobManagementScreen extends Screen {

    private static final int NAMESPACE_WIDTH = 158;
    private static final int DIVIDER = 4;
    private static final int HEADER_HEIGHT = 56;
    private static final int FOOTER_HEIGHT = 28;

    private final Screen parentScreen;
    MobGroup activeGroupFilter = null;
    String activeNamespaceFilter = null;

    private MobListWidget listWidget;
    private NamespacePanelWidget namespacePanel;
    private EditBox searchBox;
    private final Button[] groupTabs = new Button[6];
    private int activeTabIndex = 0;
    private int unsavedChanges = 0;

    public MobManagementScreen(Screen parentScreen) {
        super(Component.translatable("screen.eldritch-anomalies.mob_management"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        int listX = NAMESPACE_WIDTH + DIVIDER;
        int listWidth = this.width - listX;
        int listY = HEADER_HEIGHT;
        int listHeight = this.height - HEADER_HEIGHT - FOOTER_HEIGHT;

        listWidget = new MobListWidget(this.minecraft, this, listX, listWidth, listHeight, listY);
        addWidget(listWidget);

        namespacePanel = new NamespacePanelWidget(this.minecraft, this, 0, NAMESPACE_WIDTH, listHeight, listY);
        addWidget(namespacePanel);

        searchBox = new EditBox(this.font, listX + 4, 30, listWidth - 8, 16, Component.empty());
        searchBox.setHint(Component.translatable("search.eldritch-anomalies.search"));
        searchBox.setResponder(t -> refreshList());
        addRenderableWidget(searchBox);

        String[] tabKeys = {
            "tab.eldritch-anomalies.all",
            "tab.eldritch-anomalies.allowed",
            "tab.eldritch-anomalies.blacklisted",
            "tab.eldritch-anomalies.always_elite",
            "tab.eldritch-anomalies.always_ultra",
            "tab.eldritch-anomalies.always_eldritch"
        };
        MobGroup[] tabGroups = {
            null, MobGroup.ALLOWED, MobGroup.BLACKLISTED,
            MobGroup.ALWAYS_ELITE, MobGroup.ALWAYS_ULTRA, MobGroup.ALWAYS_ELDRITCH
        };
        int tabW = (listWidth - 8) / tabKeys.length;
        for (int i = 0; i < tabKeys.length; i++) {
            final int idx = i;
            groupTabs[i] = Button.builder(Component.translatable(tabKeys[i]), btn -> {
                activeTabIndex = idx;
                activeGroupFilter = tabGroups[idx];
                refreshList();
            }).bounds(listX + 4 + idx * tabW, 8, tabW - 2, 18).build();
            addRenderableWidget(groupTabs[i]);
        }

        int footerY = this.height - FOOTER_HEIGHT + 4;

        addRenderableWidget(Button.builder(Component.translatable("button.eldritch-anomalies.select_all"),
                btn -> setAllVisible(MobGroup.ALLOWED))
                .bounds(2, footerY, (NAMESPACE_WIDTH / 2) - 3, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("button.eldritch-anomalies.set_default"),
                btn -> setAllVisible(MobGroup.DEFAULT))
                .bounds((NAMESPACE_WIDTH / 2) + 1, footerY, (NAMESPACE_WIDTH / 2) - 3, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("button.eldritch-anomalies.ability_settings"),
                btn -> openAbilitySettings())
                .bounds(listX + 2, footerY, 90, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("button.eldritch-anomalies.cancel"),
                btn -> onClose())
                .bounds(this.width - 232, footerY, 52, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("button.eldritch-anomalies.save"),
                btn -> save())
                .bounds(this.width - 178, footerY, 52, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("button.eldritch-anomalies.sync_datapacks"),
                btn -> onSyncClick())
                .bounds(this.width - 124, footerY, 62, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("button.eldritch-anomalies.reset_all"),
                btn -> onResetClick())
                .bounds(this.width - 60, footerY, 58, 20).build());

        refreshList();
    }

    void refreshList() {
        if (listWidget == null) return;
        String search = searchBox != null ? searchBox.getValue() : "";
        listWidget.refresh(search, activeGroupFilter, activeNamespaceFilter);
        if (namespacePanel != null) {
            namespacePanel.build(listWidget.rebuildNamespaceCounts());
        }
    }

    void onNamespaceSelected(String namespace) {
        activeNamespaceFilter = namespace;
        if (listWidget != null) {
            listWidget.refresh(searchBox != null ? searchBox.getValue() : "", activeGroupFilter, namespace);
        }
    }

    void onGroupChanged() {
        unsavedChanges++;
    }

    void setAllVisible(MobGroup group) {
        for (MobListWidget.MobEntry entry : listWidget.getVisibleEntries()) {
            entry.setGroup(group);
        }
        unsavedChanges++;
        refreshList();
    }

    private void save() {
        EldritchAnomaliesConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG;
        config.allowedMobs.clear();
        config.blacklistedMobs.clear();
        config.alwaysEliteMobs.clear();
        config.alwaysUltraMobs.clear();
        config.alwaysEldritchMobs.clear();
        for (MobListWidget.MobEntry entry : listWidget.getAllEntries()) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(entry.entityType).toString();
            switch (entry.currentGroup) {
                case ALLOWED -> config.allowedMobs.add(id);
                case BLACKLISTED -> config.blacklistedMobs.add(id);
                case ALWAYS_ELITE -> config.alwaysEliteMobs.add(id);
                case ALWAYS_ULTRA -> config.alwaysUltraMobs.add(id);
                case ALWAYS_ELDRITCH -> config.alwaysEldritchMobs.add(id);
                default -> {}
            }
        }
        AutoConfig.getConfigHolder(EldritchAnomaliesConfig.class).save();
        unsavedChanges = 0;
        onClose();
    }

    private void onSyncClick() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        DynamicTagRegistry.forceSyncFromDatapacks();
                        mc.setScreen(new MobManagementScreen(parentScreen));
                    } else {
                        mc.setScreen(this);
                    }
                },
                Component.translatable("confirm.eldritch-anomalies.sync_title"),
                Component.translatable("confirm.eldritch-anomalies.sync_body")
        ));
    }

    private void onResetClick() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        EldritchAnomaliesConfig config = EldritchAnomalies.ELDRITCH_ANOMALIES_CONFIG;
                        config.allowedMobs.clear();
                        config.blacklistedMobs.clear();
                        config.alwaysEliteMobs.clear();
                        config.alwaysUltraMobs.clear();
                        config.alwaysEldritchMobs.clear();
                        config.datapackSyncDone = false;
                        AutoConfig.getConfigHolder(EldritchAnomaliesConfig.class).save();
                        mc.setScreen(new MobManagementScreen(parentScreen));
                    } else {
                        mc.setScreen(this);
                    }
                },
                Component.translatable("confirm.eldritch-anomalies.reset_title"),
                Component.translatable("confirm.eldritch-anomalies.reset_body")
        ));
    }

    private void openAbilitySettings() {
        try {
            Supplier<Screen> configScreen = AutoConfig.getConfigScreen(EldritchAnomaliesConfig.class, this);
            Minecraft.getInstance().setScreen(configScreen.get());
        } catch (Throwable t) {
            EldritchAnomalies.LOGGER.error("Failed to open ability settings screen", t);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.fill(0, 0, this.width, this.height, 0xBB000000);
        graphics.fill(0, 0, NAMESPACE_WIDTH, this.height, 0x88111111);
        graphics.fill(NAMESPACE_WIDTH, 0, NAMESPACE_WIDTH + DIVIDER, this.height, 0xFF222222);
        graphics.fill(0, this.height - FOOTER_HEIGHT, this.width, this.height - FOOTER_HEIGHT + 1, 0xFF444444);

        listWidget.render(graphics, mouseX, mouseY, partialTick);
        namespacePanel.render(graphics, mouseX, mouseY, partialTick);

        if (groupTabs[0] != null) {
            Button active = groupTabs[activeTabIndex];
            int[] tabColors = {0xFF5588AA, 0xFF00AA44, 0xFFAA2222, 0xFFCCAA00, 0xFFDD6600, 0xFF8800CC};
            int c = tabColors[activeTabIndex];
            graphics.fill(active.getX(), active.getY() + active.getHeight() - 2,
                    active.getX() + active.getWidth(), active.getY() + active.getHeight(), c);
        }

        int listX = NAMESPACE_WIDTH + DIVIDER;
        graphics.drawString(this.font, this.title, listX + 4, 1, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("label.eldritch-anomalies.namespaces"), 4, 1, 0xAAAAAA);

        int visible = listWidget.getVisibleCount();
        int total = listWidget.getAllEntries().size();
        String countStr = visible + " / " + total;
        graphics.drawString(this.font, countStr, this.width - this.font.width(countStr) - 8, 1, 0x888888, false);

        if (unsavedChanges > 0) {
            graphics.fill(this.width - 10, 2, this.width - 2, 10, 0xFFFF8800);
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parentScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

