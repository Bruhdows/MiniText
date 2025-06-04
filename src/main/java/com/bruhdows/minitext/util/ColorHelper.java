package com.bruhdows.minitext.util;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorHelper {
    private static final Map<String, NamedTextColor> NAMED_COLORS = new HashMap<>();
    private static final Map<String, TextDecoration> NAMED_DECORATIONS = new HashMap<>();

    static {
        initializeNamedColors();
        initializeDecorations();
    }

    private static void initializeNamedColors() {
        NAMED_COLORS.put("red", NamedTextColor.RED);
        NAMED_COLORS.put("blue", NamedTextColor.BLUE);
        NAMED_COLORS.put("aqua", NamedTextColor.AQUA);
        NAMED_COLORS.put("yellow", NamedTextColor.YELLOW);
        NAMED_COLORS.put("green", NamedTextColor.GREEN);
        NAMED_COLORS.put("purple", NamedTextColor.LIGHT_PURPLE);
        NAMED_COLORS.put("gray", NamedTextColor.GRAY);
        NAMED_COLORS.put("grey", NamedTextColor.GRAY);
        NAMED_COLORS.put("white", NamedTextColor.WHITE);
        NAMED_COLORS.put("black", NamedTextColor.BLACK);
        NAMED_COLORS.put("dark_red", NamedTextColor.DARK_RED);
        NAMED_COLORS.put("dark_blue", NamedTextColor.DARK_BLUE);
        NAMED_COLORS.put("dark_aqua", NamedTextColor.DARK_AQUA);
        NAMED_COLORS.put("dark_yellow", NamedTextColor.YELLOW);
        NAMED_COLORS.put("dark_green", NamedTextColor.DARK_GREEN);
        NAMED_COLORS.put("dark_purple", NamedTextColor.DARK_PURPLE);
        NAMED_COLORS.put("dark_gray", NamedTextColor.DARK_GRAY);
        NAMED_COLORS.put("dark_grey", NamedTextColor.DARK_GRAY);
        NAMED_COLORS.put("light_purple", NamedTextColor.LIGHT_PURPLE);
        NAMED_COLORS.put("gold", NamedTextColor.GOLD);
    }

    private static void initializeDecorations() {
        NAMED_DECORATIONS.put("bold", TextDecoration.BOLD);
        NAMED_DECORATIONS.put("b", TextDecoration.BOLD);
        NAMED_DECORATIONS.put("italic", TextDecoration.ITALIC);
        NAMED_DECORATIONS.put("i", TextDecoration.ITALIC);
        NAMED_DECORATIONS.put("underlined", TextDecoration.UNDERLINED);
        NAMED_DECORATIONS.put("u", TextDecoration.UNDERLINED);
        NAMED_DECORATIONS.put("strikethrough", TextDecoration.STRIKETHROUGH);
        NAMED_DECORATIONS.put("st", TextDecoration.STRIKETHROUGH);
        NAMED_DECORATIONS.put("obfuscated", TextDecoration.OBFUSCATED);
        NAMED_DECORATIONS.put("o", TextDecoration.OBFUSCATED);
    }

    public NamedTextColor getNamedColor(String name) {
        return NAMED_COLORS.get(name.toLowerCase());
    }

    public TextDecoration getDecoration(String name) {
        return NAMED_DECORATIONS.get(name.toLowerCase());
    }

    public TextColor parseHexFromTag(String tag) {
        if (tag.startsWith("#")) {
            if (tag.length() == 7) {
                try {
                    return TextColor.fromHexString(tag);
                } catch (IllegalArgumentException ignored) {}
            } else if (tag.length() == 4) {
                try {
                    String expanded = "#" + tag.charAt(1) + tag.charAt(1) +
                            tag.charAt(2) + tag.charAt(2) +
                            tag.charAt(3) + tag.charAt(3);
                    return TextColor.fromHexString(expanded);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return null;
    }

    public List<TextColor> parseGradientColors(String[] colors) {
        List<TextColor> gradientColors = new ArrayList<>();
        for (String color : colors) {
            TextColor textColor = parseColor(color);
            if (textColor != null) {
                gradientColors.add(textColor);
            }
        }
        return gradientColors;
    }

    public TextColor parseColor(String color) {
        NamedTextColor namedColor = NAMED_COLORS.get(color.toLowerCase());
        if (namedColor != null) {
            return namedColor;
        }

        if (color.startsWith("#")) {
            try {
                return TextColor.fromHexString(color);
            } catch (IllegalArgumentException ignored) {}
        } else if (color.matches("[0-9a-fA-F]{6}")) {
            try {
                return TextColor.fromHexString("#" + color);
            } catch (IllegalArgumentException ignored) {}
        } else if (color.matches("[0-9a-fA-F]{3}")) {
            try {
                return TextColor.fromHexString("#" + expandShortHex(color));
            } catch (IllegalArgumentException ignored) {}
        }

        return null;
    }

    private String expandShortHex(String shortHex) {
        StringBuilder expanded = new StringBuilder();
        for (char c : shortHex.toCharArray()) {
            expanded.append(c).append(c);
        }
        return expanded.toString();
    }
}
