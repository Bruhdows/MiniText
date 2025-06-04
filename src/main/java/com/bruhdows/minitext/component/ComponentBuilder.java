package com.bruhdows.minitext.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ComponentBuilder {
    
    public Component buildComponent(List<TextSegment> segments) {
        TextComponent.@NotNull Builder builder = Component.text();
        
        for (TextSegment segment : segments) {
            if (segment.text != null && !segment.text.isEmpty()) {
                Component segmentComponent = buildSegmentComponent(segment);
                builder.append(segmentComponent);
            }
        }
        
        return builder.build();
    }
    
    private Component buildSegmentComponent(TextSegment segment) {
        TextComponent.@NotNull Builder builder = Component.text();
        
        if (segment.rainbow) {
            builder.append(createRainbowComponent(segment));
        } else if (segment.gradient && segment.gradientColors != null && segment.gradientColors.size() >= 2) {
            builder.append(createGradientComponent(segment));
        } else {
            builder.content(segment.text);
            builder.style(buildStyle(segment));
        }
        
        return builder.build();
    }
    
    private Component createRainbowComponent(TextSegment segment) {
        TextComponent.@NotNull Builder builder = Component.text();
        String text = segment.text;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != ' ' && c != '\n') {
                float hue = ((float) (i + segment.rainbowPhase) / text.length()) * 360f;
                int rgb = java.awt.Color.HSBtoRGB(hue / 360f, 1f, 1f);
                TextColor color = TextColor.color(rgb);
                
                Style style = buildStyle(segment).color(color);
                builder.append(Component.text(c).style(style));
            } else {
                builder.append(Component.text(c));
            }
        }
        
        return builder.build();
    }
    
    private Component createGradientComponent(TextSegment segment) {
        TextComponent.@NotNull Builder builder = Component.text();
        String text = segment.text;
        List<TextColor> colors = segment.gradientColors;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != ' ' && c != '\n') {
                float progress = (float) i / Math.max(1, text.length() - 1);
                TextColor color = interpolateColor(colors, progress);
                
                Style style = buildStyle(segment).color(color);
                builder.append(Component.text(c).style(style));
            } else {
                builder.append(Component.text(c));
            }
        }
        
        return builder.build();
    }
    
    private TextColor interpolateColor(List<TextColor> colors, float progress) {
        if (colors.size() == 1) return colors.get(0);
        
        float segment = progress * (colors.size() - 1);
        int index = (int) Math.floor(segment);
        float localProgress = segment - index;
        
        if (index >= colors.size() - 1) {
            return colors.get(colors.size() - 1);
        }
        
        TextColor color1 = colors.get(index);
        TextColor color2 = colors.get(index + 1);
        
        int r1 = color1.red();
        int g1 = color1.green();
        int b1 = color1.blue();
        
        int r2 = color2.red();
        int g2 = color2.green();
        int b2 = color2.blue();
        
        int r = (int) (r1 + (r2 - r1) * localProgress);
        int g = (int) (g1 + (g2 - g1) * localProgress);
        int b = (int) (b1 + (b2 - b1) * localProgress);
        
        return TextColor.color(r, g, b);
    }
    
    private Style buildStyle(TextSegment segment) {
        Style.Builder builder = Style.style();
        
        if (segment.color != null) {
            builder.color(segment.color);
        }
        
        for (Map.Entry<net.kyori.adventure.text.format.TextDecoration, Boolean> entry : segment.decorations.entrySet()) {
            builder.decoration(entry.getKey(), entry.getValue());
        }
        
        if (segment.hoverEvent != null) {
            builder.hoverEvent(segment.hoverEvent);
        }
        
        if (segment.clickEvent != null) {
            builder.clickEvent(segment.clickEvent);
        }
        
        return builder.build();
    }
}
