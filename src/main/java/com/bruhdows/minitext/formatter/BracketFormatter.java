package com.bruhdows.minitext.formatter;

import com.bruhdows.minitext.processor.ComponentProcessor;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BracketFormatter {
    private static final Pattern BRACKET_PATTERN = Pattern.compile("(?<!\\\\)\\[([^]]+)]");
    private final Map<String, ComponentProcessor> customProcessors;

    public BracketFormatter(Map<String, ComponentProcessor> customProcessors) {
        this.customProcessors = customProcessors;
    }

    public String processCustomComponents(String input, Object context) {
        Matcher matcher = BRACKET_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String tag = matcher.group(1);
            String[] parts = tag.split(":", 2);
            String tagType = parts[0].toLowerCase();

            ComponentProcessor processor = customProcessors.get(tagType);
            if (processor != null) {
                String content = parts.length > 1 ? parts[1] : "";
                String replacement = processor.process(tagType, content, context);
                if (replacement != null) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                    continue;
                }
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}