package com.bruhdows.minitext.formatter;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacyFormatter {
    private static final Pattern LEGACY_PATTERN = Pattern.compile("&([0-9a-fklmnor])");
    private static final Map<Character, NamedTextColor> LEGACY_COLORS = new HashMap<>();
    private static final Map<Character, TextDecoration> LEGACY_DECORATIONS = new HashMap<>();
    
    static {
        initializeMappings();
    }
    
    private static void initializeMappings() {
        LEGACY_COLORS.put('0', NamedTextColor.BLACK);
        LEGACY_COLORS.put('1', NamedTextColor.DARK_BLUE);
        LEGACY_COLORS.put('2', NamedTextColor.DARK_GREEN);
        LEGACY_COLORS.put('3', NamedTextColor.DARK_AQUA);
        LEGACY_COLORS.put('4', NamedTextColor.DARK_RED);
        LEGACY_COLORS.put('5', NamedTextColor.DARK_PURPLE);
        LEGACY_COLORS.put('6', NamedTextColor.GOLD);
        LEGACY_COLORS.put('7', NamedTextColor.GRAY);
        LEGACY_COLORS.put('8', NamedTextColor.DARK_GRAY);
        LEGACY_COLORS.put('9', NamedTextColor.BLUE);
        LEGACY_COLORS.put('a', NamedTextColor.GREEN);
        LEGACY_COLORS.put('b', NamedTextColor.AQUA);
        LEGACY_COLORS.put('c', NamedTextColor.RED);
        LEGACY_COLORS.put('d', NamedTextColor.LIGHT_PURPLE);
        LEGACY_COLORS.put('e', NamedTextColor.YELLOW);
        LEGACY_COLORS.put('f', NamedTextColor.WHITE);
        
        LEGACY_DECORATIONS.put('k', TextDecoration.OBFUSCATED);
        LEGACY_DECORATIONS.put('l', TextDecoration.BOLD);
        LEGACY_DECORATIONS.put('m', TextDecoration.STRIKETHROUGH);
        LEGACY_DECORATIONS.put('n', TextDecoration.UNDERLINED);
        LEGACY_DECORATIONS.put('o', TextDecoration.ITALIC);
        LEGACY_DECORATIONS.put('r', null);
    }
    
    public String processLegacy(String input) {
        Matcher matcher = LEGACY_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        
        while (matcher.find()) {
            char code = matcher.group(1).charAt(0);
            String replacement = "§" + code;
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    public static Map<Character, NamedTextColor> getLegacyColors() {
        return LEGACY_COLORS;
    }
    
    public static Map<Character, TextDecoration> getLegacyDecorations() {
        return LEGACY_DECORATIONS;
    }
}
