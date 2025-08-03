package com.bruhdows.minitext;

import com.bruhdows.minitext.processor.TextProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class FormattedText {
    private final String originalInput;
    private final Component component;
    private final String legacyString;

    public FormattedText(String input, MiniText miniText) {
        this(input, miniText, null);
    }
    
    public FormattedText(String input, MiniText miniText, Object context) {
        this.originalInput = input;

        TextProcessor processor = new TextProcessor(miniText, context);
        this.component = processor.process(input);
        this.legacyString = LegacyComponentSerializer.builder()
                .character('§')
                .hexColors()
                .build()
                .serialize(component);
    }
    
    public Component component() {
        return component;
    }
    
    public String legacyString() {
        return legacyString;
    }
    
    public String originalInput() {
        return originalInput;
    }
    
    @Override
    public String toString() {
        return legacyString;
    }
}
