package com.bruhdows.minitext.serialization;

import net.kyori.adventure.text.Component;

public interface MiniTextSerializer {
    Component deserialize(String input);
    Component deserialize(String input, Object context);
    String serialize(Component component);
}
