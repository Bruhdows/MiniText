package com.bruhdows.minitext;

import com.bruhdows.minitext.formatter.BracketFormatter;
import com.bruhdows.minitext.formatter.FormatterType;
import com.bruhdows.minitext.formatter.HexFormatter;
import com.bruhdows.minitext.formatter.LegacyFormatter;
import com.bruhdows.minitext.processor.ComponentProcessor;
import com.bruhdows.minitext.serialization.DefaultMiniTextSerializer;
import com.bruhdows.minitext.serialization.MiniTextSerializer;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.*;

@Getter
public class MiniText {

    private static final MiniText DEFAULT_INSTANCE = new Builder().build();

    private final EnumSet<FormatterType> enabledFormatters;
    private final LegacyFormatter legacyFormatter;
    private final HexFormatter hexFormatter;
    private final BracketFormatter bracketFormatter;

    private MiniText(Builder builder) {
        this.enabledFormatters = EnumSet.copyOf(builder.enabledFormatters);
        Map<String, ComponentProcessor> customProcessors = new HashMap<>(builder.customProcessors);
        this.legacyFormatter = new LegacyFormatter();
        this.hexFormatter = new HexFormatter();
        this.bracketFormatter = new BracketFormatter(customProcessors);
    }

    public static MiniText miniText() {
        return DEFAULT_INSTANCE;
    }

    public static Builder builder() {
        return new Builder();
    }

    public MiniTextSerializer serializer() {
        return new DefaultMiniTextSerializer(this);
    }

    public MiniTextSerializer serializer(boolean useShortHex, boolean preferNamedColors) {
        return new DefaultMiniTextSerializer(this, useShortHex, preferNamedColors);
    }

    public String serialize(Component component) {
        return serializer().serialize(component);
    }

    public FormattedText deserialize(String input) {
        Objects.requireNonNull(input, "Input cannot be null");
        if (input.isEmpty()) {
            return new FormattedText("", this);
        }
        return new FormattedText(input, this);
    }

    public FormattedText deserialize(String input, Object context) {
        Objects.requireNonNull(input, "Input cannot be null");
        if (input.isEmpty()) {
            return new FormattedText("", this, context);
        }
        return new FormattedText(input, this, context);
    }

    public static class Builder {
        private final EnumSet<FormatterType> enabledFormatters = EnumSet.allOf(FormatterType.class);
        private final Map<String, ComponentProcessor> customProcessors = new HashMap<>();

        public Builder enableFormatter(FormatterType... types) {
            Collections.addAll(enabledFormatters, types);
            return this;
        }

        public Builder disableFormatter(FormatterType... types) {
            for (FormatterType type : types) {
                enabledFormatters.remove(type);
            }
            return this;
        }

        public Builder enableOnly(FormatterType... types) {
            enabledFormatters.clear();
            Collections.addAll(enabledFormatters, types);
            return this;
        }

        public Builder addCustomProcessor(String tag, ComponentProcessor processor) {
            customProcessors.put(tag.toLowerCase(), processor);
            return this;
        }

        public Builder removeCustomProcessor(String tag) {
            customProcessors.remove(tag.toLowerCase());
            return this;
        }

        public MiniText build() {
            return new MiniText(this);
        }
    }
}