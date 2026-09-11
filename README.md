# luvml

**A type-safe, fluent, composable Java DSL for generating HTML/XHTML — Java code mirrors the (X)HTML syntax it produces almost exactly.**

```xml
<dependency>
    <groupId>io.github.luvml</groupId>
    <artifactId>luvml</artifactId>
    <version>2.0</version>
</dependency>
```

**Requires:** JDK 21+

## Quick Start

```java
import static luvml.E.*;
import static luvml.A.*;
import static luvml.T.text;

var page = html(
    head(meta(charset("UTF-8")), title("Hello")),
    body(
        div(id("app"),
            h1("Hello World"),
            p("Built with luvml.")
        )
    )
);

System.out.println(XHtmlStringRenderer.asFormatted(page, "  "));
```

## Why luvml?

- **Type-safe** — an element or attribute that doesn't exist in HTML is a compile error, not a typo you find at runtime.
- **IDE-native** — rename-symbol, find-all-references and ctrl+click work on tags and attributes the same way they work on any other Java identifier.
- **No template syntax** — it's Java: conditionals, loops, methods and composition all work exactly as they do everywhere else in your codebase.

## Core Concepts

Static imports give the compact, HTML-like reading order:

```java
import static luvml.E.*;      // Elements: div(), span(), p(), a(), ...
import static luvml.A.*;      // Attributes: id(), class_(), href(), ...
import static luvml.T.*;      // Text: text(), comment(), cdata()
import static luvml.D.*;      // Doctypes: html5(), xhtml1_1()
import static luvml.Frags.*;  // Fragment utilities
```

A few HTML names collide with Java keywords or with each other, so they carry a trailing underscore or a small rename — `class_` for the `class` attribute, `for_` for `for`, and so on.

## Documentation

The full tutorial — every concept, the naming-conflict rules, escape hatches, and how to look up an exact signature without guessing — lives in [`luvml_tutorial.md`](luvml_tutorial.md).

## Related Projects

- **[luvs](https://github.com/luvml/luvs)** — type-safe CSS generation, the same DSL approach applied to stylesheets
- **[luvue](https://github.com/luvml/luvue)** — type-safe Vue.js template directives on top of luvml
- **[luvml-jsoup](https://github.com/luvml/luvml-jsoup)** — parse arbitrary HTML with JSoup, then work with it as luvml's typed DSL
- **[luvdocx](https://github.com/luvml/luvdocx)** — the same fluent-DSL idea, applied to DOCX generation via docx4j
- **[luvjfx](https://github.com/luvml/luvjfx)** — a construction DSL in the same spirit, for JavaFX

## License

Apache License 2.0 — see [LICENSE](LICENSE).
