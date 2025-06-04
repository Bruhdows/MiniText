package com.bruhdows.minitext.serialization;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ComponentAnalyzer {
    
    public List<FormattedSegment> analyze(Component component) {
        List<FormattedSegment> segments = new ArrayList<>();
        analyzeComponent(component, new FormattedSegment(), segments);
        return segments;
    }
    
    private void analyzeComponent(Component component, FormattedSegment parentSegment, List<FormattedSegment> segments) {
        FormattedSegment currentSegment = new FormattedSegment(parentSegment);
        
        Style style = component.style();
        currentSegment.color = style.color();
        currentSegment.decorations.putAll(style.decorations());
        currentSegment.hoverEvent = style.hoverEvent();
        currentSegment.clickEvent = style.clickEvent();
        
        if (component instanceof TextComponent textComp) {
            String content = textComp.content();
            if (!content.isEmpty()) {
                currentSegment.text = content;
                segments.add(currentSegment);
            }
        }
        
        for (Component child : component.children()) {
            analyzeComponent(child, currentSegment, segments);
        }
    }

    public static class FormattedSegment {
        String text = "";
        TextColor color;
        Map<TextDecoration, TextDecoration.State> decorations = new EnumMap<>(TextDecoration.class);
        HoverEvent<?> hoverEvent;
        ClickEvent clickEvent;

        public FormattedSegment() {}

        public FormattedSegment(FormattedSegment parent) {
            this.text = parent.text;
            this.color = parent.color;
            this.decorations = new EnumMap<>(parent.decorations);
            this.hoverEvent = parent.hoverEvent;
            this.clickEvent = parent.clickEvent;
        }
    }
}
