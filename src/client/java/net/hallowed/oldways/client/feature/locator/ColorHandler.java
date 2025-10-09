package net.hallowed.oldways.client.feature.locator;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorHandler {
    private ColorHandler() {}

    private static final Pattern HEX6 = Pattern.compile("#[0-9a-fA-F]{6}");
    public static Optional<Integer> getColor(ItemStack stack) {
        Optional<Integer> c = getColor(stack.get(DataComponentTypes.CUSTOM_NAME));
        if (c.isEmpty()) c = getColor(stack.get(DataComponentTypes.ITEM_NAME));
        return c;
    }

    public static Optional<Integer> getColor(Text text) {
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

    public static Optional<Text> removeColorCode(Text text) {
        if (text == null) return Optional.empty();
        return Optional.of(Text.literal(
                text.getString().replaceAll("( ?)([({<\\[]?)(#[0-9a-fA-F]{6})([)}>\\]]?)", "")
        ));
    }
}
