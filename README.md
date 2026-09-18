# luvml

**A type-safe, fluent, composable Java DSL for generating HTML/XHTML — Java code mirrors the (X)HTML syntax it produces almost exactly.**

```xml
<dependency>
    <groupId>io.github.luvml</groupId>
    <artifactId>luvml</artifactId>
    <version>2.1</version>
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

## Performance

luvml streams straight to its output instead of concatenating strings, and holds up well against comparable Java HTML libraries — 2nd of three on a fair fresh-tree comparison against htmlflow and j2html, and outright faster than htmlflow on one real workload once you use the same tricks htmlflow itself uses internally (verified against htmlflow's own source, not assumed). A real, previously-broken feature (`DynamicFrag`) got found and fixed along the way, and a genuine, permanent `T.raw(...)` escape hatch was added for when you've measured that escaping a specific piece of content is a real cost. Full numbers, method, and every honest caveat: **[PERFORMANCE.md](PERFORMANCE.md)**.

### We're already more than performant enough, so we don't obsess over it

We are still very highly performant compared to many other engines — it's actually that we are more than enough performant, so we didn't go too deep into obsession along this line. Yes, more improvements can still be made (see PERFORMANCE.md above for exactly where), but we don't chase them for their own sake — our own real websites built with luvml are already, and really, very snappy.

Where the effort went instead: bringing CSS into the same typed DSL ([luvs](https://github.com/luvml/luvs)), a typed Vue layer on top ([luvue](https://github.com/luvml/luvue)), and working on the client side too — none of which are priorities for htmlflow, whose own performance is not unachievable for luvml, we simply haven't chased it. That is not to belittle htmlflow — in fact it is our source of inspiration. The tricky bits of the DSL, the self-referencing generics, the design of the DSL itself, are heavily inspired by htmlflow. We dropped a lot of that complexity (while adding a bit of our own), and what came out of it is, we think, quite interesting, powerful and promising.

## What people are saying

> "It's interesting to see the JavalinVue philosophy taken to its logical conclusion with full type-safety."
>
> — David Åse ([@tipsy](https://github.com/tipsy)), creator of [Javalin](https://javalin.io) and [j2html](https://github.com/tipsy/j2html), on [luvml's origin story](https://github.com/javalin/javalin/issues/2582#issuecomment-4255228002)

## Related Projects

- **[luvs](https://github.com/luvml/luvs)** — type-safe CSS generation, the same DSL approach applied to stylesheets
- **[luvue](https://github.com/luvml/luvue)** — type-safe Vue.js template directives on top of luvml
- **[luvml-jsoup](https://github.com/luvml/luvml-jsoup)** — parse arbitrary HTML with JSoup, then work with it as luvml's typed DSL
- **[luvdocx](https://github.com/luvml/luvdocx)** — the same fluent-DSL idea, applied to DOCX generation via docx4j
- **[luvjfx](https://github.com/luvml/luvjfx)** — a construction DSL in the same spirit, for JavaFX

## License

Apache License 2.0 — see [LICENSE](LICENSE).
