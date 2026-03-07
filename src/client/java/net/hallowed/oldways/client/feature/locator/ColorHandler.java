package net.hallowed.oldways.client.feature.locator;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ColorHandler {
    private ColorHandler() {}

    private static final Pattern HEX6 = Pattern.compile("#[0-9a-fA-F]{6}");
    public static Optional<Integer> getColor(ItemStack stack) {
        Optional<Integer> c = getColor(stack.get(DataComponents.CUSTOM_NAME));
        if (c.isEmpty()) c = getColor(stack.get(DataComponents.ITEM_NAME));
        return c;
    }

    public static Optional<Integer> getColor(Component text) {
        return (text == null) ? Optional.empty() : getColor(text.getString());
    }

    public static Optional<Integer> getColor(String s) {
        if (s == null) return Optional.empty();
        Matcher m = HEX6.matcher(s);
        if (m.find()) {
            String hex = m.group().substring(1); // drop '#'
            try {
                return Optional.of(Integer.parseInt(hex, 16));
            } catch (NumberFormatException ignored) {}
        }
        return Optional.empty();
    }

    public static Optional<Component> removeColorCode(Component text) {
        if (text == null) return Optional.empty();
        return Optional.of(Component.literal(
                text.getString().replaceAll("( ?)([({<\\[]?)(#[0-9a-fA-F]{6})([)}>\\]]?)", "")
        ));
    }
}
