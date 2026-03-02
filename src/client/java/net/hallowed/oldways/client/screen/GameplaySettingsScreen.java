package net.hallowed.oldways.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@SuppressWarnings("ALL")
@Environment(EnvType.CLIENT)
public class GameplaySettingsScreen extends Screen {
    public static final Text TITLE = Text.translatable("options.gameplay.title");

    private final Screen parent;
    private SettingsPrefs P;

    public GameplaySettingsScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        this.P = SettingsPrefs.get();

        ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, 20, 33);
        DirectionalLayoutWidget header = layout.addHeader(DirectionalLayoutWidget.vertical().spacing(8));
        header.getMainPositioner().marginTop(10);
        header.add(new TextWidget(TITLE, this.textRenderer), Positioner::alignHorizontalCenter);

        GridWidget grid = new GridWidget();
        grid.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = grid.createAdder(2);

        adder.add(ButtonWidget.builder(Text.translatable("options.gameplay.overlay"), b ->
                        MinecraftClient.getInstance().setScreen(new OverlaySettingsScreen(this)))
                .tooltip(Tooltip.of(Text.translatable("options.overlay.tooltip"))).build());

        adder.add(ButtonWidget.builder(composeOnOff("options.gameplay.locator_bar.tab_names", P.tabShowsNames), b -> {
            P.tabShowsNames = !P.tabShowsNames;
            b.setMessage(composeOnOff("options.gameplay.locator_bar.tab_names", P.tabShowsNames));
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.gameplay.locator_bar.tab_names.tooltip"))).build());

        adder.add(ButtonWidget.builder(recipeBookLabel(P.recipeBookMode), b -> {
            P.recipeBookMode = P.recipeBookMode.next();
            b.setMessage(recipeBookLabel(P.recipeBookMode));
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.recipe_book.tooltip"))).build());

        adder.add(ButtonWidget.builder(composeOnOff("options.gameplay.show_stuck_projectiles", P.showStuckProjectiles), b -> {
            P.showStuckProjectiles = !P.showStuckProjectiles;
            b.setMessage(composeOnOff("options.gameplay.show_stuck_projectiles", P.showStuckProjectiles));
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.show_stuck_projectiles.tooltip"))).build());

        adder.add(ButtonWidget.builder(composeOnOff("options.gameplay.render_2d_items", P.render2DItems), b -> {
            P.render2DItems = !P.render2DItems;
            b.setMessage(composeOnOff("options.gameplay.render_2d_items", P.render2DItems));
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.render_2d_items.tooltip"))).build());

        adder.add(ButtonWidget.builder(composeOnOff("options.gameplay.show_potion_glint", P.showPotionGlint), b -> {
            P.showPotionGlint = !P.showPotionGlint;
            b.setMessage(composeOnOff("options.gameplay.show_potion_glint", P.showPotionGlint));
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.show_potion_glint.tooltip"))).build());

        adder.add(ButtonWidget.builder(composeOnOff("options.gameplay.realms_button", P.realmsButtons), b -> {
            P.realmsButtons = !P.realmsButtons;
            b.setMessage(composeOnOff("options.gameplay.realms_button", P.realmsButtons));
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.realms_button.tooltip"))).build());

        adder.add(ButtonWidget.builder(composeOnOff("options.gameplay.accessibility_button", P.accessibilityButton), b -> {
            P.accessibilityButton = !P.accessibilityButton;
            b.setMessage(composeOnOff("options.gameplay.accessibility_button", P.accessibilityButton));
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.accessibility_button.tooltip"))).build());

        adder.add(ButtonWidget.builder(composeOnOff("options.gameplay.telemetry", !P.telemetryOff), b -> {
            P.telemetryOff = !P.telemetryOff;
            b.setMessage(composeOnOff("options.gameplay.telemetry", !P.telemetryOff));
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.gameplay.telemetry.tooltip"))).build());

        adder.add(ButtonWidget.builder(composeOnOff("options.gameplay.copy_screenshots", P.copyScreenshots), b -> {
            P.copyScreenshots = !P.copyScreenshots;
            b.setMessage(composeOnOff("options.gameplay.copy_screenshots", P.copyScreenshots));
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.copy_screenshots.tooltip"))).build());

        layout.addBody(grid);

        ButtonWidget doneBtn = ButtonWidget.builder(ScreenTexts.DONE, b -> {
            assert this.client != null;
            this.client.setScreen(this.parent);
        }).width(200).build();
        layout.addFooter(doneBtn);

        layout.forEachChild(this::addDrawableChild);
        layout.refreshPositions();
        this.setInitialFocus(doneBtn);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int top = 31, bottom = this.height - 31;
        context.fill(0, top + 2, this.width, bottom - 2, 0x7F000000);
        int light = 0x4DFFFFFF, dark = 0xBF000000;
        context.fill(0, top, this.width, top + 1, light);
        context.fill(0, top + 1, this.width, top + 2, dark);
        context.fill(0, bottom - 2, this.width, bottom - 1, dark);
        context.fill(0, bottom - 1, this.width, bottom, light);
        super.render(context, mouseX, mouseY, delta);
    }

    private static Text composeOnOff(String key, boolean on) {
        return ScreenTexts.composeGenericOptionText(Text.translatable(key), ScreenTexts.onOrOff(on));
    }

    private static MutableText recipeBookLabel(SettingsPrefs.RecipeBookMode mode) {
        return Text.translatable("options.gameplay.recipe_book")
                .append(Text.literal(": "))
                .append(Text.translatable(mode.langKey));
    }
}
