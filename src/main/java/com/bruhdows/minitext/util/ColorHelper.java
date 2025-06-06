package com.bruhdows.minitext.util;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ColorHelper {
    private static volatile Map<String, NamedTextColor> NAMED_COLORS;
    private static volatile Map<String, TextDecoration> NAMED_DECORATIONS;

    private final Map<String, TextColor> colorCache = new ConcurrentHashMap<>(64);

    private static Map<String, NamedTextColor> getNamedColors() {
        if (NAMED_COLORS == null) {
            synchronized (ColorHelper.class) {
                if (NAMED_COLORS == null) {
                    NAMED_COLORS = Collections.unmodifiableMap(initializeNamedColors());
                }
            }
        }
        return NAMED_COLORS;
    }

    private static Map<String, TextDecoration> getNamedDecorations() {
        if (NAMED_DECORATIONS == null) {
            synchronized (ColorHelper.class) {
                if (NAMED_DECORATIONS == null) {
                    NAMED_DECORATIONS = Collections.unmodifiableMap(initializeDecorations());
                }
            }
        }
        return NAMED_DECORATIONS;
    }

    private static Map<String, NamedTextColor> initializeNamedColors() {
        Map<String, NamedTextColor> colors = new HashMap<>();
        colors.put("red", NamedTextColor.RED);
        colors.put("blue", NamedTextColor.BLUE);
        colors.put("aqua", NamedTextColor.AQUA);
        colors.put("yellow", NamedTextColor.YELLOW);
        colors.put("green", NamedTextColor.GREEN);
        colors.put("purple", NamedTextColor.LIGHT_PURPLE);
        colors.put("gray", NamedTextColor.GRAY);
        colors.put("grey", NamedTextColor.GRAY);
        colors.put("white", NamedTextColor.WHITE);
        colors.put("black", NamedTextColor.BLACK);
        colors.put("dark_red", NamedTextColor.DARK_RED);
        colors.put("dark_blue", NamedTextColor.DARK_BLUE);
        colors.put("dark_aqua", NamedTextColor.DARK_AQUA);
        colors.put("dark_yellow", NamedTextColor.YELLOW);
        colors.put("dark_green", NamedTextColor.DARK_GREEN);
        colors.put("dark_purple", NamedTextColor.DARK_PURPLE);
        colors.put("dark_gray", NamedTextColor.DARK_GRAY);
        colors.put("dark_grey", NamedTextColor.DARK_GRAY);
        colors.put("light_purple", NamedTextColor.LIGHT_PURPLE);
        colors.put("gold", NamedTextColor.GOLD);
        return colors;
    }

    private static Map<String, TextDecoration> initializeDecorations() {
        Map<String, TextDecoration> decorations = new HashMap<>();
        decorations.put("bold", TextDecoration.BOLD);
        decorations.put("b", TextDecoration.BOLD);
        decorations.put("italic", TextDecoration.ITALIC);
        decorations.put("i", TextDecoration.ITALIC);
        decorations.put("underlined", TextDecoration.UNDERLINED);
        decorations.put("u", TextDecoration.UNDERLINED);
        decorations.put("strikethrough", TextDecoration.STRIKETHROUGH);
        decorations.put("st", TextDecoration.STRIKETHROUGH);
        decorations.put("obfuscated", TextDecoration.OBFUSCATED);
        decorations.put("o", TextDecoration.OBFUSCATED);
        return decorations;
    }

    public NamedTextColor getNamedColor(String name) {
        return getNamedColors().get(name.toLowerCase());
    }

    public TextDecoration getDecoration(String name) {
        return getNamedDecorations().get(name.toLowerCase());
    }

    public TextColor parseHexFromTag(String tag) {
        return colorCache.computeIfAbsent(tag, this::parseHexFromTagUncached);
    }

    private TextColor parseHexFromTagUncached(String tag) {
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

        if (tag.matches("[0-9a-fA-F]{6}")) {
            try {
                return TextColor.fromHexString("#" + tag);
            } catch (IllegalArgumentException ignored) {}
        }

        if (tag.matches("[0-9a-fA-F]{3}")) {
            try {
                return TextColor.fromHexString("#" + expandShortHex(tag));
            } catch (IllegalArgumentException ignored) {}
        }

        return null;
    }

    public List<TextColor> parseGradientColors(String[] colors) {
        List<TextColor> gradientColors = new ArrayList<>(colors.length);
        for (String color : colors) {
            color = color.trim();
            TextColor textColor = parseColor(color);
            if (textColor != null) {
                gradientColors.add(textColor);
            }
        }
        return gradientColors;
    }

    public TextColor parseColor(String color) {
        return colorCache.computeIfAbsent(color, this::parseColorUncached);
    }

    private TextColor parseColorUncached(String color) {
        NamedTextColor namedColor = getNamedColors().get(color.toLowerCase());
        if (namedColor != null) {
            return namedColor;
        }

        if (color.startsWith("#")) {
            try {
                if (color.length() == 7) {
                    return TextColor.fromHexString(color);
                } else if (color.length() == 4) {
                    return TextColor.fromHexString("#" + expandShortHex(color.substring(1)));
                }
            } catch (IllegalArgumentException ignored) {}
        }

        if (color.matches("[0-9a-fA-F]{6}")) {
            try {
                return TextColor.fromHexString("#" + color);
            } catch (IllegalArgumentException ignored) {}
        }

        if (color.matches("[0-9a-fA-F]{3}")) {
            try {
                return TextColor.fromHexString("#" + expandShortHex(color));
            } catch (IllegalArgumentException ignored) {}
        }

        return null;
    }

    private String expandShortHex(String shortHex) {
        StringBuilder expanded = new StringBuilder(6);
        for (char c : shortHex.toCharArray()) {
            expanded.append(c).append(c);
        }
        return expanded.toString();
    }
}
