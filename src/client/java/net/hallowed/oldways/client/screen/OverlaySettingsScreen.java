package net.hallowed.oldways.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
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


@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class OverlaySettingsScreen extends Screen {
    public static final Component TITLE = Component.translatable("options.gameplay.overlay.title");

    private final Screen parent;
    private SettingsPrefs P;

    public OverlaySettingsScreen(Screen parent) {
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

        // Row 1 — ON/OFF
        adder.addChild(Button.builder(showCoordsText(), b -> {
            P.showCoords = !P.showCoords;
            b.setMessage(showCoordsText());
            SettingsPrefs.save();
        }).tooltip(Tooltip.create(Component.translatable("options.gameplay.overlay.coords.tooltip"))).build());

        adder.addChild(Button.builder(showTimeText(), b -> {
            P.showTime = !P.showTime;
            b.setMessage(showTimeText());
            SettingsPrefs.save();
        }).tooltip(Tooltip.create(Component.translatable("options.gameplay.overlay.time.tooltip"))).build());

        adder.addChild(Button.builder(coordsPosText(), b -> {
            P.coordsPos = P.coordsPos.next();
            b.setMessage(coordsPosText());
            SettingsPrefs.save();
        }).tooltip(Tooltip.create(Component.translatable("options.gameplay.overlay.coords_pos.tooltip"))).build());

        adder.addChild(Button.builder(timePosText(), b -> {
            P.timePos = P.timePos.next();
            b.setMessage(timePosText());
            SettingsPrefs.save();
        }).tooltip(Tooltip.create(Component.translatable("options.gameplay.overlay.time_pos.tooltip"))).build());

        EditBox timeFmt = new EditBox(
                this.font, 0, 0, 150, 20,
                Component.translatable("options.gameplay.overlay.time_format")
        );
        timeFmt.setValue(P.timeDayFormat);
        timeFmt.setTooltip(Tooltip.create(Component.translatable("options.gameplay.overlay.time_format.tooltip")));
        timeFmt.setResponder(s -> { P.timeDayFormat = s; SettingsPrefs.save(); });
        adder.addChild(timeFmt);

        EditBox coordsFmt = new EditBox(
                this.font, 0, 0, 150, 20,
                Component.translatable("options.gameplay.overlay.coords_format")
        );
        coordsFmt.setValue(P.coordsFormat);
        coordsFmt.setTooltip(Tooltip.create(Component.translatable("options.gameplay.overlay.coords_format.tooltip")));
        coordsFmt.setResponder(s -> { P.coordsFormat = s; SettingsPrefs.save(); });
        adder.addChild(coordsFmt);

        layout.addToContents(grid);

        Button doneBtn = Button.builder(CommonComponents.GUI_DONE, b -> {
            SettingsPrefs.save();
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

    private MutableComponent showCoordsText() {
        return Component.translatable("options.gameplay.overlay.show_coords")
                .append(Component.literal(": "))
                .append(P.showCoords ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }
    private MutableComponent showTimeText() {
        return Component.translatable("options.gameplay.overlay.show_time")
                .append(Component.literal(": "))
                .append(P.showTime ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }
    private MutableComponent coordsPosText() {
        return Component.translatable("options.gameplay.overlay.coords_pos")
                .append(Component.literal(": "))
                .append(Component.translatable(P.coordsPos.langKey));
    }
    private MutableComponent timePosText() {
        return Component.translatable("options.gameplay.overlay.time_pos")
                .append(Component.literal(": "))
                .append(Component.translatable(P.timePos.langKey));
    }
}
