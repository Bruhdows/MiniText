# MiniText

MiniText is a lightweight text formatting library built on top of the [Kyori Adventure](https://github.com/KyoriPowered/adventure) API. It provides a compact tag syntax for colors, decorations, gradients, hover events, click actions, and legacy code support.

## Highlights

- Supports legacy color codes like `&cHello`
- Supports hex colors like `&#ff0000`, `&#f00`, `[#ff0000]`, and `[#f00]`
- Supports named colors and decorations like `[red]`, `[b]`, `[i]`, and `[u]`
- Supports gradients and rainbow effects
- Supports hover and click events
- Serializes Adventure `Component` trees back into MiniText syntax
- Allows custom processors for project-specific tags

## Requirements

- Java 21 or newer for building
- A Java 21 runtime is recommended for CI and release builds

## Installation

Add the JitPack repository:

```kotlin
repositories {
    maven("https://jitpack.io")
}
```

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add the dependency:

```kotlin
dependencies {
    implementation("com.github.Bruhdows:MiniText:VERSION")
}
```

```xml
<dependency>
    <groupId>com.github.Bruhdows</groupId>
    <artifactId>MiniText</artifactId>
    <version>VERSION</version>
</dependency>
```

Replace `VERSION` with the latest published release or JitPack build you want to consume.

## Quick Start

```java
import com.bruhdows.minitext.FormattedText;
import com.bruhdows.minitext.MiniText;
import net.kyori.adventure.text.Component;

MiniText miniText = MiniText.miniText();

FormattedText formatted = miniText.deserialize("[red][b]Hello [#00d0ff]world");
Component component = formatted.component();
String legacy = formatted.legacyString();
String roundTrip = miniText.serialize(component);
```

## Supported Syntax

```text
[red]Red text
[bold]Bold text [i]italic
&#ff0000Hex color [#f00]short hex
[gradient:red:blue]Gradient text
[rainbow]Rainbow text
[hover:show_text:'Tooltip']Hover me
[click:open_url:'https://example.com']Click me
Line 1[n]Line 2
```

## Configuration

Enable only selected formatter groups:

```java
import com.bruhdows.minitext.formatter.FormatterType;

MiniText chatSafe = MiniText.builder()
        .enableOnly(
                FormatterType.LEGACY,
                FormatterType.NAMED_COLORS,
                FormatterType.DECORATIONS
        )
        .build();
```

Register a custom processor:

```java
MiniText withVars = MiniText.builder()
        .addCustomProcessor("var", (tag, content, context) -> switch (content) {
            case "server_name" -> "My Server";
            case "player_count" -> "123";
            default -> null;
        })
        .build();
```

## Development

Use the Gradle wrapper:

```bash
./gradlew build
```
