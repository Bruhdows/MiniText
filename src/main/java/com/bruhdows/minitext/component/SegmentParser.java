package com.bruhdows.minitext.component;

import com.bruhdows.minitext.MiniText;
import com.bruhdows.minitext.formatter.FormatterType;
import com.bruhdows.minitext.util.ColorHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SegmentParser {
    private static final Pattern BRACKET_PATTERN = Pattern.compile("\\[([^]]+)]");

    private final MiniText miniText;
    private final ColorHelper colorHelper;
    private final ThreadLocal<Matcher> matcher = ThreadLocal.withInitial(() -> BRACKET_PATTERN.matcher(""));

    public SegmentParser(MiniText miniText) {
        this.miniText = miniText;
        this.colorHelper = new ColorHelper();
    }

    public List<TextSegment> parseSegments(String input) {
        List<TextSegment> segments = new ArrayList<>();
        TextSegment currentSegment = SegmentPool.acquire();

        Matcher m = matcher.get();
        m.reset(input);
        int lastEnd = 0;

        try {
            while (m.find()) {
                String beforeTag = input.substring(lastEnd, m.start());
                if (!beforeTag.isEmpty()) {
                    currentSegment.text = beforeTag;
                    segments.add(currentSegment);
                    currentSegment = SegmentPool.acquire().copyFrom(currentSegment);
                }

                String tag = m.group(1);
                TextSegment result = processTag(tag, currentSegment);

                if (isNewlineTag(tag)) {
                    TextSegment newlineSegment = SegmentPool.acquire().copyFrom(currentSegment);
                    newlineSegment.text = "\n";
                    segments.add(newlineSegment);
                } else {
                    currentSegment = result;
                }

                lastEnd = m.end();
            }

            String remainingText = input.substring(lastEnd);
            if (!remainingText.isEmpty()) {
                currentSegment.text = remainingText;
                segments.add(currentSegment);
            } else {
                SegmentPool.release(currentSegment);
            }
        } catch (Exception e) {
            SegmentPool.release(currentSegment);
            throw e;
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

        return switch (tagType) {
            case "reset" -> miniText.getEnabledFormatters().contains(FormatterType.RESET)
                    ? SegmentPool.acquire() : currentSegment;
            case "rainbow" -> miniText.getEnabledFormatters().contains(FormatterType.RAINBOW)
                    ? processRainbow(parts, currentSegment) : currentSegment;
            case "gradient" -> miniText.getEnabledFormatters().contains(FormatterType.GRADIENTS)
                    ? processGradient(parts, currentSegment) : currentSegment;
            case "hover" -> miniText.getEnabledFormatters().contains(FormatterType.HOVER_EVENTS)
                    ? processHover(parts, currentSegment) : currentSegment;
            case "click" -> miniText.getEnabledFormatters().contains(FormatterType.CLICK_EVENTS)
                    ? processClick(parts, currentSegment) : currentSegment;
            default -> processSimpleTag(tagType, currentSegment);
        };
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
        currentSegment.gradient = false;
        currentSegment.gradientColors = null;
        currentSegment.color = null;
        return currentSegment;
    }

    private TextSegment processGradient(String[] parts, TextSegment currentSegment) {
        if (parts.length > 1) {
            String[] colors = parts[1].split(":");
            if (colors.length >= 2) {
                List<TextColor> gradientColors = colorHelper.parseGradientColors(colors);
                if (gradientColors.size() >= 2) {
                    currentSegment.gradient = true;
                    currentSegment.gradientColors = gradientColors;
                    currentSegment.rainbow = false;
                    currentSegment.color = null;
                }
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

                currentSegment.clickEvent = switch (clickType) {
                    case "open_url" -> ClickEvent.openUrl(clickValue);
                    case "run_command" -> ClickEvent.runCommand(clickValue);
                    case "suggest_command" -> ClickEvent.suggestCommand(clickValue);
                    case "copy_to_clipboard" -> ClickEvent.copyToClipboard(clickValue);
                    default -> currentSegment.clickEvent;
                };
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

        if (miniText.getEnabledFormatters().contains(FormatterType.HEX)) {
            TextColor hexColor = colorHelper.parseHexFromTag(tagType);
            if (hexColor != null) {
                currentSegment.color = hexColor;
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
