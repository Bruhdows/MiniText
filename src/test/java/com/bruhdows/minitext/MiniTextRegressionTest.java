package com.bruhdows.minitext;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MiniTextRegressionTest {

    private final MiniText miniText = MiniText.miniText();

    @Test
    void serializerKeepsInheritedParentColor() {
        Component component = Component.text()
                .color(NamedTextColor.RED)
                .append(Component.text("Hello"))
                .append(Component.text(" world").decorate(TextDecoration.BOLD))
                .build();

        assertEquals("[red]Hello[b] world", miniText.serialize(component));
    }

    @Test
    void serializerUsesReadableHoverText() {
        Component component = Component.text("Hover me")
                .hoverEvent(HoverEvent.showText(Component.text("Tooltip")));

        assertEquals("[hover:show_text:'Tooltip']Hover me", miniText.serialize(component));
    }

    @Test
    void serializerEscapesQuotedClickValues() {
        Component component = Component.text("Run")
                .clickEvent(ClickEvent.runCommand("/say it\\'s working"));

        assertEquals("[click:run_command:'/say it\\\\\\'s working']Run", miniText.serialize(component));
    }

    @Test
    void gradientWhitespaceKeepsInteractiveStyle() {
        Component component = miniText.deserialize("[hover:show_text:'Tooltip'][gradient:red:blue]A B").component();

        TextComponent root = (TextComponent) component;
        assertEquals(1, root.children().size());

        Component gradientComponent = root.children().getFirst();
        assertEquals(3, gradientComponent.children().size());
        for (Component child : gradientComponent.children()) {
            assertNotNull(child.style().hoverEvent());
            assertEquals(HoverEvent.showText(Component.text("Tooltip")), child.style().hoverEvent());
        }
    }
}
