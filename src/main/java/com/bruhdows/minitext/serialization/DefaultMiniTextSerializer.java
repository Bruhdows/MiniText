package com.bruhdows.minitext.serialization;

import com.bruhdows.minitext.MiniText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;
import java.util.Map;

public class DefaultMiniTextSerializer implements MiniTextSerializer {
    
    private final MiniText miniText;
    private final ComponentAnalyzer analyzer;
    private final SerializationHelper helper;

    public DefaultMiniTextSerializer(MiniText miniText) {
        this(miniText, false, true);
    }
    
    public DefaultMiniTextSerializer(MiniText miniText, boolean useShortHex, boolean preferNamedColors) {
        this.miniText = miniText;
        this.analyzer = new ComponentAnalyzer();
        this.helper = new SerializationHelper(useShortHex, preferNamedColors);
    }
    
    @Override
    public Component deserialize(String input) {
        return miniText.deserialize(input).component();
    }
    
    @Override
    public Component deserialize(String input, Object context) {
        return miniText.deserialize(input, context).component();
    }
    
    @Override
    public String serialize(Component component) {
        List<ComponentAnalyzer.FormattedSegment> segments = analyzer.analyze(component);
        return buildMiniTextString(segments);
    }
    
    private String buildMiniTextString(List<ComponentAnalyzer.FormattedSegment> segments) {
        StringBuilder result = new StringBuilder();
        ComponentAnalyzer.FormattedSegment lastSegment = new ComponentAnalyzer.FormattedSegment();
        
        for (ComponentAnalyzer.FormattedSegment segment : segments) {
            appendFormattingChanges(result, lastSegment, segment);
            result.append(segment.text);
            lastSegment = segment;
        }
        
        return result.toString();
    }

    private void appendFormattingChanges(
            StringBuilder sb,
            ComponentAnalyzer.FormattedSegment last,
            ComponentAnalyzer.FormattedSegment current
    ) {
        if (needsReset(last, current)) {
            sb.append("[reset]");
            last = new ComponentAnalyzer.FormattedSegment();
        }

        boolean colorChanged = !colorEquals(last.color, current.color);
        if (colorChanged) {
            sb.append(helper.serializeColor(current.color));
        }

        for (TextDecoration deco : TextDecoration.values()) {
            TextDecoration.State lastState =
                    last.decorations.getOrDefault(deco, TextDecoration.State.NOT_SET);
            TextDecoration.State currState =
                    current.decorations.getOrDefault(deco, TextDecoration.State.NOT_SET);

            if (currState == TextDecoration.State.TRUE
                    && (colorChanged || lastState != currState)) {
                sb.append(helper.serializeDecorations(Map.of(deco,
                        TextDecoration.State.TRUE)));
            }
        }

        if (!hoverEquals(last.hoverEvent, current.hoverEvent)) {
            sb.append(helper.serializeHoverEvent(current.hoverEvent));
        }
        if (!clickEquals(last.clickEvent, current.clickEvent)) {
            sb.append(helper.serializeClickEvent(current.clickEvent));
        }
    }
    
    private boolean needsReset(ComponentAnalyzer.FormattedSegment last, ComponentAnalyzer.FormattedSegment current) {
        int lastComplexity = getFormattingComplexity(last);
        int currentComplexity = getFormattingComplexity(current);
        
        return lastComplexity > currentComplexity + 1;
    }
    
    private int getFormattingComplexity(ComponentAnalyzer.FormattedSegment segment) {
        int complexity = 0;
        if (segment.color != null) complexity++;
        complexity += (int) segment.decorations.values().stream()
            .filter(state -> state == TextDecoration.State.TRUE)
            .count();
        if (segment.hoverEvent != null) complexity++;
        if (segment.clickEvent != null) complexity++;
        return complexity;
    }
    
    private String getNewDecorations(ComponentAnalyzer.FormattedSegment last, ComponentAnalyzer.FormattedSegment current) {
        StringBuilder sb = new StringBuilder();
        
        for (TextDecoration decoration : TextDecoration.values()) {
            TextDecoration.State lastState = last.decorations.getOrDefault(decoration, TextDecoration.State.NOT_SET);
            TextDecoration.State currentState = current.decorations.getOrDefault(decoration, TextDecoration.State.NOT_SET);
            
            if (lastState != currentState && currentState == TextDecoration.State.TRUE) {
                sb.append(helper.serializeDecorations(Map.of(decoration, TextDecoration.State.TRUE)));
            }
        }
        
        return sb.toString();
    }
    
    private boolean colorEquals(TextColor a, TextColor b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    private boolean hoverEquals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    private boolean clickEquals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
