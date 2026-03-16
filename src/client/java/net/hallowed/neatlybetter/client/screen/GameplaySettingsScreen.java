package net.hallowed.neatlybetter.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.hallowed.neatlybetter.client.util.SettingsPrefs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@SuppressWarnings("ALL")
@Environment(EnvType.CLIENT)
public class GameplaySettingsScreen extends Screen {
    public static final Component TITLE = Component.translatable("options.gameplay.title");

    private final Screen parent;
    private SettingsPrefs P;

    public GameplaySettingsScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.P = SettingsPrefs.get();

        HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 20, 33);
        LinearLayout header = layout.addToHeader(LinearLayout.vertical().spacing(8));
        header.defaultCellSetting().paddingTop(10);
        header.addChild(new StringWidget(TITLE, this.font), LayoutSettings::alignHorizontallyCenter);

        GridLayout grid = new GridLayout();
        grid.defaultCellSetting().paddingHorizontal(5).paddingBottom(4).alignHorizontallyCenter();
        GridLayout.RowHelper adder = grid.createRowHelper(2);

        adder.addChild(Button.builder(Component.translatable("options.gameplay.overlay"), b ->
                        Minecraft.getInstance().setScreen(new OverlaySettingsScreen(this)))
                .tooltip(Tooltip.create(Component.translatable("options.overlay.tooltip"))).build());

        adder.addChild(Button.builder(composeOnOff("options.gameplay.locator_bar.tab_names", P.tabShowsNames), b -> {
            P.tabShowsNames = !P.tabShowsNames;
            b.setMessage(composeOnOff("options.gameplay.locator_bar.tab_names", P.tabShowsNames));
            SettingsPrefs.save();
        }).tooltip(Tooltip.create(Component.translatable("options.gameplay.locator_bar.tab_names.tooltip"))).build());

        adder.addChild(Button.builder(recipeBookLabel(P.recipeBookMode), b -> {
            P.recipeBookMode = P.recipeBookMode.next();
            b.setMessage(recipeBookLabel(P.recipeBookMode));
            SettingsPrefs.save();
        }).tooltip(Tooltip.create(Component.translatable("options.recipe_book.tooltip"))).build());

        adder.addChild(Button.builder(composeOnOff("options.gameplay.show_stuck_projectiles", P.showStuckProjectiles), b -> {
            P.showStuckProjectiles = !P.showStuckProjectiles;
            b.setMessage(composeOnOff("options.gameplay.show_stuck_projectiles", P.showStuckProjectiles));
            SettingsPrefs.save();
        }).tooltip(Tooltip.create(Component.translatable("options.show_stuck_projectiles.tooltip"))).build());

        adder.addChild(Button.builder(composeOnOff("options.gameplay.render_2d_items", P.render2DItems), b -> {
            P.render2DItems = !P.render2DItems;
            b.setMessage(composeOnOff("options.gameplay.render_2d_items", P.render2DItems));
            SettingsPrefs.save();
        }).tooltip(Tooltip.create(Component.translatable("options.render_2d_items.tooltip"))).build());

        adder.addChild(Button.builder(composeOnOff("options.gameplay.show_potion_glint", P.showPotionGlint), b -> {
            P.showPotionGlint = !P.showPotionGlint;
            b.setMessage(composeOnOff("options.gameplay.show_potion_glint", P.showPotionGlint));
            SettingsPrefs.save();
        }).tooltip(Tooltip.create(Component.translatable("options.show_potion_glint.tooltip"))).build());

        adder.addChild(Button.builder(composeOnOff("options.gameplay.realms_button", P.realmsButtons), b -> {
            P.realmsButtons = !P.realmsButtons;
            b.setMessage(composeOnOff("options.gameplay.realms_button", P.realmsButtons));
            SettingsPrefs.save();
        }).tooltip(Tooltip.create(Component.translatable("options.realms_button.tooltip"))).build());

        adder.addChild(Button.builder(composeOnOff("options.gameplay.accessibility_button", P.accessibilityButton), b -> {
            P.accessibilityButton = !P.accessibilityButton;
            b.setMessage(composeOnOff("options.gameplay.accessibility_button", P.accessibilityButton));
            SettingsPrefs.save();
        }).tooltip(Tooltip.create(Component.translatable("options.accessibility_button.tooltip"))).build());

        adder.addChild(Button.builder(composeOnOff("options.gameplay.telemetry", !P.telemetryOff), b -> {
            P.telemetryOff = !P.telemetryOff;
            b.setMessage(composeOnOff("options.gameplay.telemetry", !P.telemetryOff));
            SettingsPrefs.save();
        }).tooltip(Tooltip.create(Component.translatable("options.gameplay.telemetry.tooltip"))).build());

        adder.addChild(Button.builder(composeOnOff("options.gameplay.copy_screenshots", P.copyScreenshots), b -> {
            P.copyScreenshots = !P.copyScreenshots;
            b.setMessage(composeOnOff("options.gameplay.copy_screenshots", P.copyScreenshots));
            SettingsPrefs.save();
        }).tooltip(Tooltip.create(Component.translatable("options.copy_screenshots.tooltip"))).build());

        layout.addToContents(grid);

        Button doneBtn = Button.builder(CommonComponents.GUI_DONE, b -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(this.parent);
        }).width(200).build();
        layout.addToFooter(doneBtn);

        layout.visitWidgets(this::addRenderableWidget);
        layout.arrangeElements();
        this.setInitialFocus(doneBtn);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int top = 31, bottom = this.height - 31;
        context.fill(0, top + 2, this.width, bottom - 2, 0x7F000000);
        int light = 0x4DFFFFFF, dark = 0xBF000000;
        context.fill(0, top, this.width, top + 1, light);
        context.fill(0, top + 1, this.width, top + 2, dark);
        context.fill(0, bottom - 2, this.width, bottom - 1, dark);
        context.fill(0, bottom - 1, this.width, bottom, light);
        super.render(context, mouseX, mouseY, delta);
    }

    private static Component composeOnOff(String key, boolean on) {
        return CommonComponents.optionNameValue(Component.translatable(key), CommonComponents.optionStatus(on));
    }

    private static MutableComponent recipeBookLabel(SettingsPrefs.RecipeBookMode mode) {
        return Component.translatable("options.gameplay.recipe_book")
                .append(Component.literal(": "))
                .append(Component.translatable(mode.langKey));
    }
}
