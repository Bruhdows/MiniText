package com.bruhdows.minitext.component;

import com.bruhdows.minitext.MiniText;
import com.bruhdows.minitext.formatter.FormatterType;
import com.bruhdows.minitext.util.ColorHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SegmentParser {
    private static final Pattern BRACKET_PATTERN = Pattern.compile("\\[([^]]+)]");
    private final MiniText miniText;
    private final ColorHelper colorHelper;
    
    public SegmentParser(MiniText miniText) {
        this.miniText = miniText;
        this.colorHelper = new ColorHelper();
    }
    
    public List<TextSegment> parseSegments(String input) {
        List<TextSegment> segments = new ArrayList<>();
        TextSegment currentSegment = new TextSegment();
        
        Matcher matcher = BRACKET_PATTERN.matcher(input);
        int lastEnd = 0;
        
        while (matcher.find()) {
            String beforeTag = input.substring(lastEnd, matcher.start());
            if (!beforeTag.isEmpty()) {
                currentSegment.text = beforeTag;
                segments.add(currentSegment);
                currentSegment = new TextSegment(currentSegment);
            }
            
            String tag = matcher.group(1);
            TextSegment result = processTag(tag, currentSegment);
            
            if (isNewlineTag(tag)) {
                TextSegment newlineSegment = new TextSegment(currentSegment);
                newlineSegment.text = "\n";
                segments.add(newlineSegment);
            } else {
                currentSegment = result;
            }
            
            lastEnd = matcher.end();
        }
        
        String remainingText = input.substring(lastEnd);
        if (!remainingText.isEmpty()) {
            currentSegment.text = remainingText;
            segments.add(currentSegment);
        }
        
        return segments;
    }
    
    private boolean isNewlineTag(String tag) {
        return miniText.getEnabledFormatters().contains(FormatterType.NEW_LINES) &&
               (tag.equals("n") || tag.equals("nl") || tag.equals("br"));
    }
    
    private TextSegment processTag(String tag, TextSegment currentSegment) {
        if (isNewlineTag(tag)) {
            return currentSegment;
        }
        
        String[] parts = tag.split(":", 2);
        String tagType = parts[0].toLowerCase();
        
        switch (tagType) {
            case "reset":
                if (miniText.getEnabledFormatters().contains(FormatterType.RESET)) {
                    return new TextSegment();
                }
                break;
            case "rainbow":
                if (miniText.getEnabledFormatters().contains(FormatterType.RAINBOW)) {
                    return processRainbow(parts, currentSegment);
                }
                break;
            case "gradient":
                if (miniText.getEnabledFormatters().contains(FormatterType.GRADIENTS)) {
                    return processGradient(parts, currentSegment);
                }
                break;
            case "hover":
                if (miniText.getEnabledFormatters().contains(FormatterType.HOVER_EVENTS)) {
                    return processHover(parts, currentSegment);
                }
                break;
            case "click":
                if (miniText.getEnabledFormatters().contains(FormatterType.CLICK_EVENTS)) {
                    return processClick(parts, currentSegment);
                }
                break;
            default:
                return processSimpleTag(tagType, currentSegment);
        }
        
        return currentSegment;
    }
    
    private TextSegment processRainbow(String[] parts, TextSegment currentSegment) {
        int phase = 0;
        if (parts.length > 1) {
            try {
                phase = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        }
        currentSegment.rainbow = true;
        currentSegment.rainbowPhase = phase;
        return currentSegment;
    }
    
    private TextSegment processGradient(String[] parts, TextSegment currentSegment) {
        if (parts.length > 1) {
            String[] colors = parts[1].split(":");
            if (colors.length >= 2) {
                currentSegment.gradient = true;
                currentSegment.gradientColors = colorHelper.parseGradientColors(colors);
            }
        }
        return currentSegment;
    }
    
    private TextSegment processHover(String[] parts, TextSegment currentSegment) {
        if (parts.length > 1) {
            String[] hoverParts = parts[1].split(":", 2);
            if (hoverParts.length == 2) {
                String hoverType = hoverParts[0];
                String hoverValue = hoverParts[1].replaceAll("^'|'$", "");
                
                if ("show_text".equals(hoverType)) {
                    currentSegment.hoverEvent = HoverEvent.showText(Component.text(hoverValue));
                }
            }
        }
        return currentSegment;
    }
    
    private TextSegment processClick(String[] parts, TextSegment currentSegment) {
        if (parts.length > 1) {
            String[] clickParts = parts[1].split(":", 2);
            if (clickParts.length == 2) {
                String clickType = clickParts[0];
                String clickValue = clickParts[1].replaceAll("^'|'$", "");
                
                switch (clickType) {
                    case "open_url":
                        currentSegment.clickEvent = ClickEvent.openUrl(clickValue);
                        break;
                    case "run_command":
                        currentSegment.clickEvent = ClickEvent.runCommand(clickValue);
                        break;
                    case "suggest_command":
                        currentSegment.clickEvent = ClickEvent.suggestCommand(clickValue);
                        break;
                    case "copy_to_clipboard":
                        currentSegment.clickEvent = ClickEvent.copyToClipboard(clickValue);
                        break;
                }
            }
        }
        return currentSegment;
    }
    
    private TextSegment processSimpleTag(String tagType, TextSegment currentSegment) {
        if (miniText.getEnabledFormatters().contains(FormatterType.NAMED_COLORS)) {
            NamedTextColor namedColor = colorHelper.getNamedColor(tagType);
            if (namedColor != null) {
                currentSegment.color = namedColor;
                currentSegment.clearDecorations();
                return currentSegment;
            }
        }
        
        if (miniText.getEnabledFormatters().contains(FormatterType.DECORATIONS)) {
            TextDecoration decoration = colorHelper.getDecoration(tagType);
            if (decoration != null) {
                currentSegment.decorations.put(decoration, true);
                return currentSegment;
            }
        }
        
        return currentSegment;
    }
}
