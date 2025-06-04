package com.bruhdows.minitext.serialization;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.HashMap;
import java.util.Map;

public class SerializationHelper {

    private static final Map<NamedTextColor, String> NAMED_COLOR_MAP = new HashMap<>();
    private static final Map<TextDecoration, String> DECORATION_MAP = new HashMap<>();

    private final boolean useShortHex;
    private final boolean preferNamedColors;

    static {
        initializeColorMap();
        initializeDecorationMap();
    }

    public SerializationHelper() {
        this(false, true);
    }

    public SerializationHelper(boolean useShortHex, boolean preferNamedColors) {
        this.useShortHex = useShortHex;
        this.preferNamedColors = preferNamedColors;
    }

    private static void initializeColorMap() {
        NAMED_COLOR_MAP.put(NamedTextColor.RED, "red");
        NAMED_COLOR_MAP.put(NamedTextColor.BLUE, "blue");
        NAMED_COLOR_MAP.put(NamedTextColor.AQUA, "aqua");
        NAMED_COLOR_MAP.put(NamedTextColor.YELLOW, "yellow");
        NAMED_COLOR_MAP.put(NamedTextColor.GREEN, "green");
        NAMED_COLOR_MAP.put(NamedTextColor.LIGHT_PURPLE, "purple");
        NAMED_COLOR_MAP.put(NamedTextColor.GRAY, "gray");
        NAMED_COLOR_MAP.put(NamedTextColor.WHITE, "white");
        NAMED_COLOR_MAP.put(NamedTextColor.BLACK, "black");
        NAMED_COLOR_MAP.put(NamedTextColor.DARK_RED, "dark_red");
        NAMED_COLOR_MAP.put(NamedTextColor.DARK_BLUE, "dark_blue");
        NAMED_COLOR_MAP.put(NamedTextColor.DARK_AQUA, "dark_aqua");
        NAMED_COLOR_MAP.put(NamedTextColor.DARK_GREEN, "dark_green");
        NAMED_COLOR_MAP.put(NamedTextColor.DARK_PURPLE, "dark_purple");
        NAMED_COLOR_MAP.put(NamedTextColor.DARK_GRAY, "dark_gray");
        NAMED_COLOR_MAP.put(NamedTextColor.GOLD, "gold");
    }

    private static void initializeDecorationMap() {
        DECORATION_MAP.put(TextDecoration.BOLD, "b");
        DECORATION_MAP.put(TextDecoration.ITALIC, "i");
        DECORATION_MAP.put(TextDecoration.UNDERLINED, "u");
        DECORATION_MAP.put(TextDecoration.STRIKETHROUGH, "st");
        DECORATION_MAP.put(TextDecoration.OBFUSCATED, "o");
    }

    public String serializeColor(TextColor color) {
        if (color == null) return "";

        if (preferNamedColors && color instanceof NamedTextColor) {
            String namedColor = NAMED_COLOR_MAP.get(color);
            if (namedColor != null) {
                return "[" + namedColor + "]";
            }
        }

        String hexString = color.asHexString();

        if (useShortHex) {
            String shortHex = tryConvertToShortHex(hexString);
            if (shortHex != null) {
                return "&#" + shortHex;
            }
        }

        return "&#" + hexString.substring(1);
    }

    private String tryConvertToShortHex(String hexString) {
        if (hexString.length() != 7) return null;

        String hex = hexString.substring(1);

        if (hex.charAt(0) == hex.charAt(1) &&
                hex.charAt(2) == hex.charAt(3) &&
                hex.charAt(4) == hex.charAt(5)) {

            return "" + hex.charAt(0) + hex.charAt(2) + hex.charAt(4);
        }

        return null;
    }

    public String serializeDecorations(Map<TextDecoration, TextDecoration.State> decorations) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<TextDecoration, TextDecoration.State> entry : decorations.entrySet()) {
            if (entry.getValue() == TextDecoration.State.TRUE) {
                String decoration = DECORATION_MAP.get(entry.getKey());
                if (decoration != null) {
                    sb.append("[").append(decoration).append("]");
                }
            }
        }
        return sb.toString();
    }

    public String serializeHoverEvent(HoverEvent<?> hoverEvent) {
        if (hoverEvent == null) return "";

        if (hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
            Object value = hoverEvent.value();
            if (value instanceof Component) {
                String text = value.toString().replaceAll("'", "\\'");
                return "[hover:show_text:'" + text + "']";
            }
        }

        return "";
    }

    public String serializeClickEvent(ClickEvent clickEvent) {
        if (clickEvent == null) return "";

        String action = "";
        switch (clickEvent.action()) {
            case OPEN_URL:
                action = "open_url";
                break;
            case RUN_COMMAND:
                action = "run_command";
                break;
            case SUGGEST_COMMAND:
                action = "suggest_command";
                break;
            case COPY_TO_CLIPBOARD:
                action = "copy_to_clipboard";
                break;
            default:
                return "";
        }

        String value = clickEvent.value().replaceAll("'", "\\'");
        return "[click:" + action + ":'" + value + "']";
    }
}
