package net.hallowed.oldways.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;

@SuppressWarnings("ALL")
@Environment(EnvType.CLIENT)
public class LocatorSettingsScreen extends Screen {
    public static final Text TITLE = Text.translatable("options.gameplay.locator_bar.title");

    private final Screen parent;
    private SettingsPrefs P;

    public LocatorSettingsScreen(Screen parent) {
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

        adder.add(ButtonWidget.builder(showSpecText(), b -> {
            P.showInSpectator = !P.showInSpectator;
            b.setMessage(showSpecText());
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.gameplay.locator_bar.show_in_spec.tooltip"))).build());

        adder.add(ButtonWidget.builder(tabForcesText(), b -> {
            P.tabForcesLocatorBar = !P.tabForcesLocatorBar;
            b.setMessage(tabForcesText());
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.gameplay.locator_bar.tab_forces.tooltip"))).build());

        adder.add(ButtonWidget.builder(tabShowsText(), b -> {
            P.tabShowsNames = !P.tabShowsNames;
            b.setMessage(tabShowsText());
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.gameplay.locator_bar.tab_names.tooltip"))).build());

        adder.add(ButtonWidget.builder(playerHeadsText(), b -> {
            P.renderPlayerHeads = !P.renderPlayerHeads;
            b.setMessage(playerHeadsText());
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.gameplay.locator_bar.player_heads.tooltip"))).build());

        adder.add(ButtonWidget.builder(coloredOutlineText(), b -> {
            P.coloredHeadOutline = !P.coloredHeadOutline;
            b.setMessage(coloredOutlineText());
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.gameplay.locator_bar.head_outline.tooltip"))).build());

        StepSlider sizeSlider = new StepSlider(
                0.75, 1.25, 0.01, P.headSizeMultiplier,
                Text.translatable("options.gameplay.locator_bar.head_size"),
                v -> { P.headSizeMultiplier = v; SettingsPrefs.save(); }
        );
        sizeSlider.setTooltip(Tooltip.of(Text.translatable("options.gameplay.locator_bar.head_size.tooltip")));
        adder.add(sizeSlider);

        layout.addBody(grid);

        ButtonWidget doneBtn = ButtonWidget.builder(ScreenTexts.DONE, b -> {
            SettingsPrefs.save();
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

    private MutableText showSpecText() {
        return Text.translatable("options.gameplay.locator_bar.show_in_spec")
                .append(Text.literal(": "))
                .append(P.showInSpectator ? ScreenTexts.ON : ScreenTexts.OFF);
    }
    private MutableText tabForcesText() {
        return Text.translatable("options.gameplay.locator_bar.tab_forces")
                .append(Text.literal(": "))
                .append(P.tabForcesLocatorBar ? ScreenTexts.ON : ScreenTexts.OFF);
    }
    private MutableText tabShowsText() {
        return Text.translatable("options.gameplay.locator_bar.tab_names")
                .append(Text.literal(": "))
                .append(P.tabShowsNames ? ScreenTexts.ON : ScreenTexts.OFF);
    }
    private MutableText playerHeadsText() {
        return Text.translatable("options.gameplay.locator_bar.player_heads")
                .append(Text.literal(": "))
                .append(P.renderPlayerHeads ? ScreenTexts.ON : ScreenTexts.OFF);
    }
    private MutableText coloredOutlineText() {
        return Text.translatable("options.gameplay.locator_bar.head_outline")
                .append(Text.literal(": "))
                .append(P.coloredHeadOutline ? ScreenTexts.ON : ScreenTexts.OFF);
    }

    private static final class StepSlider extends SliderWidget {
        private final double min, max, step;
        private final DoubleConsumer onChange;
        private final Text baseLabel;

        StepSlider(double min, double max, double step, double currentValue, Text label, DoubleConsumer onChange) {
            super(0, 0, 150, 20, Text.empty(), normalize(currentValue, min, max));
            this.min = min;
            this.max = max;
            this.step = step;
            this.onChange = onChange;
            this.baseLabel = label;
            this.updateMessage();
        }

        private static double normalize(double v, double min, double max) {
            double clamped = Math.max(min, Math.min(max, v));
            return (clamped - min) / (max - min);
        }

        private double denormalize(double v) {
            return min + v * (max - min);
        }

        private double snap(double v) {
            double snapped = Math.round((v - min) / step) * step + min;
            if (snapped < min) snapped = min;
            if (snapped > max) snapped = max;
            return Math.round(snapped * 100.0) / 100.0;
        }

        @Override
        protected void updateMessage() {
            double val = snap(denormalize(this.value));
            this.setMessage(baseLabel.copy().append(Text.literal(": x" + String.format("%.2f", val))));
        }

        @Override
        protected void applyValue() {
            double val = snap(denormalize(this.value));
            this.onChange.accept(val);
            this.updateMessage();
        }
    }
}
