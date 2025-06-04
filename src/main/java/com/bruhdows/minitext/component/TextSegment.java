package com.bruhdows.minitext.component;

import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TextSegment {
    String text = "";
    TextColor color;
    Map<TextDecoration, Boolean> decorations = new EnumMap<>(TextDecoration.class);
    HoverEvent<?> hoverEvent;
    ClickEvent clickEvent;
    boolean rainbow = false;
    int rainbowPhase = 0;
    boolean gradient = false;
    List<TextColor> gradientColors;
    
    public TextSegment() {}
    
    public TextSegment(TextSegment other) {
        this.text = other.text;
        this.color = other.color;
        this.decorations = new EnumMap<>(other.decorations);
        this.hoverEvent = other.hoverEvent;
        this.clickEvent = other.clickEvent;
        this.rainbow = other.rainbow;
        this.rainbowPhase = other.rainbowPhase;
        this.gradient = other.gradient;
        this.gradientColors = other.gradientColors;
    }
    
    public void clearDecorations() {
        decorations.clear();
        hoverEvent = null;
        clickEvent = null;
        rainbow = false;
        gradient = false;
        gradientColors = null;
    }
    
    public void clearNonDecorations() {
        color = null;
        hoverEvent = null;
        clickEvent = null;
        rainbow = false;
        gradient = false;
        gradientColors = null;
    }
}
