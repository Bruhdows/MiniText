package com.bruhdows.minitext.component;

import com.bruhdows.minitext.MiniText;
import com.bruhdows.minitext.formatter.FormatterType;
import com.bruhdows.minitext.util.ColorHelper;
import lombok.extern.java.Log;
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

@Log
public class SegmentParser {
    private static final Pattern SEGMENT_PATTERN = Pattern.compile(
            "(?<!\\\\)\\[([^\\]]+)]|" +
                    "\\\\(\\[|\\])|" +
                    "§([0-9a-fklmnor])|" +
                    "§x(§[0-9a-fA-F])(§[0-9a-fA-F])(§[0-9a-fA-F])(§[0-9a-fA-F])(§[0-9a-fA-F])(§[0-9a-fA-F])"
    );

    private final MiniText miniText;
    private final ColorHelper colorHelper;
    private final ThreadLocal<Matcher> matcher = ThreadLocal.withInitial(() -> SEGMENT_PATTERN.matcher(""));

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
                String plainTextBeforeMatch = input.substring(lastEnd, m.start());
                if (!plainTextBeforeMatch.isEmpty()) {
                    currentSegment.text = plainTextBeforeMatch;
                    segments.add(currentSegment);
                    currentSegment = SegmentPool.acquire().copyFrom(currentSegment);
                }

                if (m.group(1) != null) {
                    String tag = m.group(1);
                    TextSegment nextSegmentStyle = processBracketTag(tag, currentSegment);

                    if (isNewlineTag(tag)) {
                        TextSegment newlineSegment = SegmentPool.acquire().copyFrom(currentSegment);
                        newlineSegment.text = "\n";
                        segments.add(newlineSegment);
                        SegmentPool.release(currentSegment);
                        currentSegment = SegmentPool.acquire().copyFrom(nextSegmentStyle);
                        currentSegment.text = "";
                    } else {
                        SegmentPool.release(currentSegment);
                        currentSegment = nextSegmentStyle;
                        currentSegment.text = "";
                    }
                } else if (m.group(2) != null) {
                    currentSegment.text = m.group(2);
                    segments.add(currentSegment);
                    currentSegment = SegmentPool.acquire().copyFrom(currentSegment);
                } else if (m.group(3) != null) {
                    String code = m.group(3);
                    processLegacyHexCode(code, currentSegment);
                    currentSegment.text = "";
                } else if (m.group(4) != null) {
                    StringBuilder hexBuilder = new StringBuilder();
                    for (int i = 4; i <= 9; i++) {
                        String part = m.group(i);
                        if (part != null && part.length() == 2 && part.charAt(0) == '§') {
                            hexBuilder.append(part.charAt(1));
                        } else {
                            log.severe("MiniText: Malformed hex part detected: " + m.group(0));
                            hexBuilder.append("0");
                        }
                    }
                    String hexChars = hexBuilder.toString();
                    processLegacyHexCode("x" + hexChars, currentSegment);
                    currentSegment.text = "";
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

    private TextSegment processBracketTag(String tag, TextSegment currentSegment) {
        if (isNewlineTag(tag)) {
            return currentSegment;
        }

        String[] parts = tag.split(":", 2);
        String tagType = parts[0].toLowerCase();

        TextSegment newStyleSegment = SegmentPool.acquire().copyFrom(currentSegment);

        switch (tagType) {
            case "reset" -> {
                if (miniText.getEnabledFormatters().contains(FormatterType.RESET)) {
                    newStyleSegment.reset();
                }
            }
            case "rainbow" -> {
                if (miniText.getEnabledFormatters().contains(FormatterType.RAINBOW)) {
                    processRainbow(parts, newStyleSegment);
                }
            }
            case "gradient" -> {
                if (miniText.getEnabledFormatters().contains(FormatterType.GRADIENTS)) {
                    processGradient(parts, newStyleSegment);
                }
            }
            case "hover" -> {
                if (miniText.getEnabledFormatters().contains(FormatterType.HOVER_EVENTS)) {
                    processHover(parts, newStyleSegment);
                }
            }
            case "click" -> {
                if (miniText.getEnabledFormatters().contains(FormatterType.CLICK_EVENTS)) {
                    processClick(parts, newStyleSegment);
                }
            }
            default -> processSimpleTag(tagType, newStyleSegment);
        }
        return newStyleSegment;
    }

    private void processLegacyHexCode(String code, TextSegment currentSegment) {
        if (code.equals("r")) {
            if (miniText.getEnabledFormatters().contains(FormatterType.RESET)) {
                currentSegment.reset();
            }
            return;
        }

        if (code.startsWith("x") && miniText.getEnabledFormatters().contains(FormatterType.HEX)) {
            try {
                String hex = code.substring(1);
                currentSegment.color = TextColor.fromHexString("#" + hex);
                currentSegment.decorations.clear();
                currentSegment.hoverEvent = null;
                currentSegment.clickEvent = null;
                currentSegment.rainbow = false;
                currentSegment.gradient = false;
                currentSegment.gradientColors = null;
            } catch (IllegalArgumentException e) {
                log.severe("MiniText: Error parsing hex color '" + code + "': " + e.getMessage());
            }
            return;
        }

        char legacyChar = code.charAt(0);
        if (miniText.getEnabledFormatters().contains(FormatterType.LEGACY)) {
            NamedTextColor namedColor = com.bruhdows.minitext.formatter.LegacyFormatter.getLegacyColors().get(legacyChar);
            if (namedColor != null) {
                currentSegment.color = namedColor;
                currentSegment.decorations.clear();
                currentSegment.hoverEvent = null;
                currentSegment.clickEvent = null;
                currentSegment.rainbow = false;
                currentSegment.gradient = false;
                currentSegment.gradientColors = null;
                return;
            }

            TextDecoration decoration = com.bruhdows.minitext.formatter.LegacyFormatter.getLegacyDecorations().get(legacyChar);
            if (decoration != null) {
                currentSegment.decorations.put(decoration, true);
                return;
            }
        }
    }

    private TextSegment processRainbow(String[] parts, TextSegment segmentToModify) {
        int phase = 0;
        if (parts.length > 1) {
            try {
                phase = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }
        segmentToModify.rainbow = true;
        segmentToModify.rainbowPhase = phase;
        segmentToModify.gradient = false;
        segmentToModify.gradientColors = null;
        segmentToModify.color = null;
        segmentToModify.decorations.clear();
        segmentToModify.hoverEvent = null;
        segmentToModify.clickEvent = null;
        return segmentToModify;
    }

    private TextSegment processGradient(String[] parts, TextSegment segmentToModify) {
        if (parts.length > 1) {
            String[] colors = parts[1].split(":");
            if (colors.length >= 2) {
                List<TextColor> gradientColors = colorHelper.parseGradientColors(colors);
                if (gradientColors.size() >= 2) {
                    segmentToModify.gradient = true;
                    segmentToModify.gradientColors = gradientColors;
                    segmentToModify.rainbow = false;
                    segmentToModify.color = null;
                    segmentToModify.decorations.clear();
                    segmentToModify.hoverEvent = null;
                    segmentToModify.clickEvent = null;
                }
            }
        }
        return segmentToModify;
    }

    private TextSegment processHover(String[] parts, TextSegment segmentToModify) {
        if (parts.length > 1) {
            String[] hoverParts = parts[1].split(":", 2);
            if (hoverParts.length == 2) {
                String hoverType = hoverParts[0];
                String hoverValue = hoverParts[1];
                if (hoverValue.startsWith("'") && hoverValue.endsWith("'") && hoverValue.length() > 1) {
                    hoverValue = hoverValue.substring(1, hoverValue.length() - 1);
                }

                if ("show_text".equals(hoverType)) {
                    segmentToModify.hoverEvent = HoverEvent.showText(Component.text(hoverValue));
                }
            }
        }
        return segmentToModify;
    }

    private TextSegment processClick(String[] parts, TextSegment segmentToModify) {
        if (parts.length > 1) {
            String[] clickParts = parts[1].split(":", 2);
            if (clickParts.length == 2) {
                String clickType = clickParts[0];
                String clickValue = clickParts[1];
                if (clickValue.startsWith("'") && clickValue.endsWith("'") && clickValue.length() > 1) {
                    clickValue = clickValue.substring(1, clickValue.length() - 1);
                }

                segmentToModify.clickEvent = switch (clickType) {
                    case "open_url" -> ClickEvent.openUrl(clickValue);
                    case "run_command" -> ClickEvent.runCommand(clickValue);
                    case "suggest_command" -> ClickEvent.suggestCommand(clickValue);
                    case "copy_to_clipboard" -> ClickEvent.copyToClipboard(clickValue);
                    default -> segmentToModify.clickEvent;
                };
            }
        }
        return segmentToModify;
    }

    private void processSimpleTag(String tagType, TextSegment segmentToModify) {
        if (miniText.getEnabledFormatters().contains(FormatterType.NAMED_COLORS)) {
            NamedTextColor namedColor = colorHelper.getNamedColor(tagType);
            if (namedColor != null) {
                segmentToModify.color = namedColor;
                segmentToModify.clearDecorations();
                return;
            }
        }

        if (miniText.getEnabledFormatters().contains(FormatterType.HEX)) {
            TextColor hexColor = colorHelper.parseHexFromTag(tagType);
            if (hexColor != null) {
                segmentToModify.color = hexColor;
                segmentToModify.clearDecorations();
                return;
            }
        }

        if (miniText.getEnabledFormatters().contains(FormatterType.DECORATIONS)) {
            TextDecoration decoration = colorHelper.getDecoration(tagType);
            if (decoration != null) {
                segmentToModify.decorations.put(decoration, true);
                return;
            }
        }
    }
}