package com.bruhdows.minitext.formatter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexFormatter {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern SHORT_HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{3})");
    
    public String processHex(String input) {
        input = processFullHex(input);
        input = processShortHex(input);
        return input;
    }
    
    private String processFullHex(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = "§x§" + String.join("§", hex.split(""));
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    private String processShortHex(String input) {
        Matcher matcher = SHORT_HEX_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        
        while (matcher.find()) {
            String shortHex = matcher.group(1);
            String fullHex = expandShortHex(shortHex);
            String replacement = "§x§" + String.join("§", fullHex.split(""));
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    private String expandShortHex(String shortHex) {
        StringBuilder expanded = new StringBuilder();
        for (char c : shortHex.toCharArray()) {
            expanded.append(c).append(c);
        }
        return expanded.toString();
    }
}
