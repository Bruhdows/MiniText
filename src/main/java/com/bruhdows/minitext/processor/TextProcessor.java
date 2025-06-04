package com.bruhdows.minitext.processor;

import com.bruhdows.minitext.MiniText;
import com.bruhdows.minitext.component.ComponentBuilder;
import com.bruhdows.minitext.component.SegmentParser;
import com.bruhdows.minitext.component.TextSegment;
import com.bruhdows.minitext.formatter.FormatterType;
import net.kyori.adventure.text.Component;

import java.util.List;

public class TextProcessor {
    private final MiniText miniText;
    private final Object context;
    
    public TextProcessor(MiniText miniText, Object context) {
        this.miniText = miniText;
        this.context = context;
    }
    
    public Component process(String input) {
        if (miniText.getEnabledFormatters().contains(FormatterType.CUSTOM)) {
            input = miniText.getBracketFormatter().processCustomComponents(input, context);
        }
        
        if (miniText.getEnabledFormatters().contains(FormatterType.LEGACY)) {
            input = miniText.getLegacyFormatter().processLegacy(input);
        }
        
        if (miniText.getEnabledFormatters().contains(FormatterType.HEX)) {
            input = miniText.getHexFormatter().processHex(input);
        }
        
        SegmentParser parser = new SegmentParser(miniText);
        List<TextSegment> segments = parser.parseSegments(input);
        
        ComponentBuilder builder = new ComponentBuilder();
        return builder.buildComponent(segments);
    }
}
