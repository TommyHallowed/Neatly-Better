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


@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class OverlaySettingsScreen extends Screen {
    public static final Text TITLE = Text.translatable("options.gameplay.overlay.title");

    private final Screen parent;
    private SettingsPrefs P;

    public OverlaySettingsScreen(Screen parent) {
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

        // Row 1 — ON/OFF
        adder.add(ButtonWidget.builder(showCoordsText(), b -> {
            P.showCoords = !P.showCoords;
            b.setMessage(showCoordsText());
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.gameplay.overlay.coords.tooltip"))).build());

        adder.add(ButtonWidget.builder(showTimeText(), b -> {
            P.showTime = !P.showTime;
            b.setMessage(showTimeText());
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.gameplay.overlay.time.tooltip"))).build());

        adder.add(ButtonWidget.builder(coordsPosText(), b -> {
            P.coordsPos = P.coordsPos.next();
            b.setMessage(coordsPosText());
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.gameplay.overlay.coords_pos.tooltip"))).build());

        adder.add(ButtonWidget.builder(timePosText(), b -> {
            P.timePos = P.timePos.next();
            b.setMessage(timePosText());
            SettingsPrefs.save();
        }).tooltip(Tooltip.of(Text.translatable("options.gameplay.overlay.time_pos.tooltip"))).build());

        TextFieldWidget timeFmt = new TextFieldWidget(
                this.textRenderer, 0, 0, 150, 20,
                Text.translatable("options.gameplay.overlay.time_format")
        );
        timeFmt.setText(P.timeDayFormat);
        timeFmt.setTooltip(Tooltip.of(Text.translatable("options.gameplay.overlay.time_format.tooltip")));
        timeFmt.setChangedListener(s -> { P.timeDayFormat = s; SettingsPrefs.save(); });
        adder.add(timeFmt);

        TextFieldWidget coordsFmt = new TextFieldWidget(
                this.textRenderer, 0, 0, 150, 20,
                Text.translatable("options.gameplay.overlay.coords_format")
        );
        coordsFmt.setText(P.coordsFormat);
        coordsFmt.setTooltip(Tooltip.of(Text.translatable("options.gameplay.overlay.coords_format.tooltip")));
        coordsFmt.setChangedListener(s -> { P.coordsFormat = s; SettingsPrefs.save(); });
        adder.add(coordsFmt);

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

    private MutableText showCoordsText() {
        return Text.translatable("options.gameplay.overlay.show_coords")
                .append(Text.literal(": "))
                .append(P.showCoords ? ScreenTexts.ON : ScreenTexts.OFF);
    }
    private MutableText showTimeText() {
        return Text.translatable("options.gameplay.overlay.show_time")
                .append(Text.literal(": "))
                .append(P.showTime ? ScreenTexts.ON : ScreenTexts.OFF);
    }
    private MutableText coordsPosText() {
        return Text.translatable("options.gameplay.overlay.coords_pos")
                .append(Text.literal(": "))
                .append(Text.translatable(P.coordsPos.langKey));
    }
    private MutableText timePosText() {
        return Text.translatable("options.gameplay.overlay.time_pos")
                .append(Text.literal(": "))
                .append(Text.translatable(P.timePos.langKey));
    }
}
