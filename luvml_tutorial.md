# LUVML Tutorial

A concise guide to using LUVML (Luv Markup Language) - a type-safe Java HTML/XML generation library.

## Maven co-ordinates
```
<dependency>
	<groupId>io.github.luvml</groupId>
	<artifactId>luvml</artifactId>
	<version>2.0</version>
</dependency>
```

GitHub repo : https://github.com/luvml/luvml

### The version is `2.0` and stays `2.0`

luvml and luvs keep the version `2.0` while their source moves. Maven therefore has no version to compare, never re-resolves, and a build can sit on a months-old jar in `~/.m2` indefinitely with nothing reporting it. **When generated output changes for no reason visible in the source, check this first.** It has already produced one wrong diagnosis: a run emitted spurious diffs across 70 files, which matched a known luvml bug exactly — so the bug was blamed, while the real cause was a jar four months older than the `luvml/C.java` that had already fixed it.

Compare the dates before believing any luvml symptom, and rebuild if the source is newer:

```bash
ls -la ~/.m2/repository/io/github/luvml/luvml/2.0/luvml-2.0.jar
ls -la <luvml-checkout>/luvml/src/main/java/luvml/C.java
```

There is no aggregator pom, so rebuild in dependency order, each module its own build — `luvx-base`, `luvx-char_sequence`, `luvml`, `luvs`, each `mvn -o install -DskipTests`.

Then confirm determinism by generating twice into the output directory and comparing hashes of the two runs, rather than by diffing against the last commit — that commit may itself carry an ordering produced by an older jar, which makes a correct build look broken.

```bash
find . -path ./.git -prune -o -type f -print | sort | xargs sha256sum > run1.txt
```

**Related Projects**:
- **[luvue](../luvue/README.md)** - Type-safe Vue.js templates using luvml
- **Component Patterns Guide** - a real-world package-per-page recipe for building Vue-style components with luvml+luvue+luvs, maintained in a separate consumer project rather than here; read it when building/refactoring web UI components, ignore it for pure server-side or non-UI work (project-specific example, not part of the luvml library).

## Looking things up: `jacli`

This tutorial teaches the model. It is not a signature reference. `jacli` answers from the compiler in ~300ms:

```
jacli tree                               packages and type names
jacli api luvml.E                        what a type or package declares, with use counts
jacli api --name div                     search declaration names
jacli def 'luvml.E#div'                  every overload of one member
jacli src 'luvml.C#class_'               a body, without opening the file
jacli refs 'luvx.Frag_I'                 every use
jacli impls luvx.Frag_I --scope luvml    the type hierarchy, derived
```

Run it inside the project; `-p <dir>` points elsewhere. `--scope luvml` spans luvml, luvs and luvx-base and is required for anything crossing them. `jacli` alone prints its own tutorial.

Use it at the moment you are about to give up type safety — before writing a raw string where a typed call might exist, and before every escape hatch below. Those are marked **Check first**.

## Core Concepts

### Static Imports
```java
import static luvml.E.*;  // Elements: div(), span(), p(), etc.
import static luvml.A.*;  // Attributes: id(), class_(), href(), etc.
import static luvml.T.*;  // Text: text(), comment(), cdata()
import static luvml.D.*;  // Doctypes: html5(), xhtml1_1()
import static luvml.Frags.*;  // Fragment utilities
```

## Handling html tags or attributes which conflict with java keywords 
The html attribute `class` is a reserved keyword in Java. So that is the why dsl method for html class attribute is not class but `class_` . This rule applies to other cases also where there might be conflict. Another example is the attribute `for` is actually represented by the dsl method `for_` etc. 

## How to resolve method name conflicts 
Some methods are in multiple class, mostly there are conflicts in certain methods being present both in E (elements dsl methods utility class) and A (attributes dsl methods utility class).
If you do a wild card imports such as 
```
import static luvml.E.*;  // Elements: has all elements dsl methods, also has span() .
import static luvml.A.*;  // Attributes: has all attributes dsl methods, also has span() .
```
`A.title()`, `A.style()`, `A.span()` (attribute factories) shadow `E.title()`, `E.style()`, `E.span()` (element factories).

It will prevent you from writing 
```
imgBlock.____(p(class_("picture_info"),
	span("Url: "), // your intent here is to use the element not the attribute 
	a(href(img.srcUrl()), text(img.srcUrl()))));
```
so as a mitigation please also import 
```
import luvml.E;
```
And do this instead : 
```
imgBlock.____(p(class_("picture_info"),
	E.span("Url: "),
	a(href(img.srcUrl()), text(img.srcUrl()))));
```	
This is short and clean, not too bad, especially considering that span is a fairly commonly used element. 

**Check first:** `jacli def 'span'` lists every declaration sharing the name with its owner, so you can see whether a single-static-import or the short `E.` form settles it before reaching for full qualification.

The default way an AI tends to fix (qualify) using full syntax such as `luvml.E.title()`, `luvml.E.style()`, `luvml.E.span()`. Example : 
```
imgBlock.____(p(class_("picture_info"),
	luvml.E.span("Url: "),
	a(href(img.srcUrl()), luvml.T.text(img.srcUrl()))));
```	

This is also technically correct but verbose and spoils the elegance of the dsl. 

Also notice `text` lives in **both** `E` and `T` with identical signatures. If you wildcard-import only **one** of them (the usual case — e.g. `import static luvml.E.*;` and not `T.*`), bare `text(...)` resolves fine. But if you wildcard-import **both** `E.*` and `T.*`, then `text(...)` is genuinely ambiguous — the compiler errors with *"reference to text is ambiguous"*. The clean fix is a **single-static-import**, which shadows the wildcards: add `import static luvml.T.text;` and bare `text(...)` works again. (Do not "fix" it by writing `luvml.T.text(...)` everywhere — that's the verbose anti-pattern.)

Alternatively if you import one-one method which are going to use and then use .. the dsl will not need situations like `E.span` but writing so many imports is even more verbose which again defeats the purpose of the terse element dsl. So the best solution for this sample code is to write it like this.

### Naming your own constants next to luvml and luvs

The same collision arises for the names *you* choose, not just the ones you call. A project that static-imports luvml alongside luvs is importing a large namespace: `luvs.V` carries CSS value constants (`BLUE`, `TEXT`, …), `luvs.HtmlTag` carries tag constants (`footer`, `content`, …), and your own helper methods named `h1` or `meta` will hide the `E` imports in the same file.

**Check before you name, not after the compiler complains** — the answer is one query:

```
jacli api luvs.V --scope luvml            what value constants already exist
jacli api --name footer --scope luvml     is this name taken, and by whom
jacli def 'h1'                            every declaration of a name, with its owner
```

Then pick a name that does not collide. A project palette naming its brand colour `BRAND` rather than `BLUE`, and its body colour `INK` rather than `TEXT`, costs nothing and keeps the wildcard imports usable; a CSS class holder using `foot` and `maincol` rather than `footer` and `content` does the same. Renaming your own constant is always cheaper than qualifying every luvs reference in the file.

### Special Case: Enum Method Conflicts

When using luvml inside enum classes, additional conflicts arise with enum methods like `name()`.

**Problem:**
```java
import static luvml.A.*;

public enum MyComponent implements CssClass {
    my_component;

    public static Frag_I<?> render() {
        return input(
            name("username")  // ❌ Ambiguous - enum.name() vs A.name()
        );
    }
}
```

**Solution 1 (Recommended) - Use `name_()` variant:**

luvml provides `name_()` as an alternative to `name()` specifically to avoid enum conflicts:

```java
import static luvml.A.*;

public enum MyComponent implements CssClass {
    my_component;

    public static Frag_I<?> render() {
        return input(
            name_("username")  // ✅ Uses A.name_() - no conflict with enum.name()
        );
    }
}
```

**Solution 2 - Qualify with `A.`:**
```java
import luvml.A;
import static luvml.A.*;

public enum MyComponent implements CssClass {
    my_component;

    public static Frag_I<?> render() {
        return input(
            A.name("username")  // ✅ Explicit - short and clear
        );
    }
}
```

**❌ NEVER use full qualification:**
```java
// ❌ WRONG - too verbose, breaks DSL elegance
luvml.A.name("username")
```

**Note:** `name_()` is functionally identical to `name()` - it's purely a convenience method to avoid enum conflicts.

### Basic Element Creation

```java
// Simple elements with text
div("Hello World")
span("Click here")

// Multiple text nodes
p("Hello", " ", "World")

// Nested elements
div(
    h1("Title"),
    p("Paragraph content")
)

// With attributes
div(id("main"), class_("container"),
    p("Content")
)

// Attributes only
img(src("/logo.png"), alt("Logo"))
```

### Indentation Conventions

**Recommended indentation patterns for readable luvml code:**

**Few attributes (same line):**
```java
// ✅ Good - attributes on same line, children on next line
div(class_(foo), id("bar"),
    text("content"),
    p(text("child"))
)
```

**Many attributes (next line with blank line separator):**
```java
// ✅ Good - attributes on next line, blank line before children
div(
    class_(foo),
    id("bar"),
    vFor("item in items"),
    v$key("item.id"),

    text("content"),
    p(text("child"))
)
```

**Self-closing elements (inline):**
```java
// ✅ Good - no children, keep inline
img(src("/image.jpg"), alt("desc"))
input(typeText(), name_("username"), required())
```

**❌ Avoid excessive indentation:**
```java
// ❌ Not recommended - double indentation makes code too wide
div(class_(foo), id("bar"),
        text("content"),  // ← Extra indentation not needed
        p(text("child"))
)
```

**Rationale:** The blank line separator between attributes and children provides visual clarity without excessive indentation. This keeps the code readable even with deeply nested structures.

### VERY IMPORTANT Consideration with regard to mixing string/text and fragments in the dsl varargs 

We cannot mix Frag_I (or even Attr_I) with String in the same varargs. We need to use `luvml.E.text()` (or `luvml.T.text()` ) to wrap plain strings inside elements that have attributes or elements.

For further clarity, we will taking `div` as an example (but the principle is general and applies to all others as well) to explain how we have several overloaded functions for each element/node and how to proper mix text and fragments : 
```
  // most commonly used dsl factory method to write java code which mirrors the actual html produced 
  public static BlockContainerElement div(Frag_I<?>... fragments) {
    return blockContainer("div", fragments);
  }

  // used to add a collection/list of items in a single call. 
  public static BlockContainerElement div(Iterable<Frag_I<?>> fragments) {
    return blockContainer("div", fragments);
  }

  // when all child elements which we want to add are plain text/string, it makes it very easy to write things like `h2("some heading 2")` and also concatenations such as `h2("My name is - ",obj.getName())`
  public static BlockContainerElement div(String... textContent) {
    return blockContainer("div", textContent);
  }

  // when we just want to create a new object without any child elements. 
  public static BlockContainerElement div() {
    return blockContainer("div");
  }
```
particularly notice `div(Frag_I<?>... fragments)` and `div(String... textContent)` this allows us to have either a varargs of fragments or text, we cannot mix both. If we want to mix both we need to wrap textContent around a `luvml.E.text()` or `luvml.T.text()` which is essentially a text node.

To be even more clear.
This is valid : `div("plain text node 1","textnode2","textnode3");` and so is this value `div(h2("heading",p("first para"),p("second para"));` 
BUT this is NOT valid - `div(h2("heading",p("first para"),p("second para"),"plain text node");` 

CORRECT IS `div(h2("heading",p("first para"),p("second para"),text("plain text node"));` 

Here `text` is `luvml.E.text()` or `luvml.T.text()` — identical methods. Bare `text(...)` works as long as you wildcard-import only one of `E`/`T`; if you import **both** `E.*` and `T.*`, `text(...)` is ambiguous (compile error) — resolve it with a single-static-import `import static luvml.T.text;` (see "How to resolve method name conflicts" above), not with full qualification everywhere. 

### Element Types

- **BlockContainerElement**: `div`, `p`, `section`, `article`, etc. (Note: `div` is always block, never inline)
- **InlineContainerElement**: `span`, `a`, `strong`, `em`, etc.
- **BlockVoidElement**: `hr`, `meta`, `link`, `br` in certain contexts
- **InlineVoidElement**: `img`, `input`, `br` in inline contexts

### Attributes

```java
// Standard attributes
div(id("main"), class_("container"))

// Boolean attributes
input(typeCheckbox(), checked())
input(typeCheckbox(), checked(false))  // omitted when false

// Multiple class names (space-separated)
div(class_("btn", "btn-primary", "active"))

// Style attribute
div(style("color: red; font-size: 14px;"))

// Custom/data attributes
div(data("user-id", "123"))
div(event("click", "handleClick()"))

// XML namespaces (already built-in)
div(xmlns("http://www.w3.org/1999/xhtml"))
```

#### Special case of type attribute

The `type` attribute is context-sensitive and appears across many HTML elements
(input, button, link, script, form, etc.), so LUVML exposes it as named factory
methods rather than a generic `type(String)` call. Full list:

- Input types: typeText(), typePassword(), typeEmail(), typeUrl(), typeTel(), typeNumber(), typeRange(), typeDate(), typeTime(), typeMonth(), typeWeek(), typeDatetimeLocal(), typeCheckbox(), typeRadio(), typeFile(), typeColor(), typeSearch(), typeHidden(), typeImage(), typeSubmit(), typeReset(), typeButton()

- MIME/media types:   typeTextJavascript(), typeTextCss(), typeApplicationRssXml(), typeImageXIcon()

- Scoped (value required): \
	- typeLink(String value)  — restricted to <link> elements
	- typeForm(String value)  — restricted to <form>-related elements

## Working with Fragments

### The `Frags` Container

```java
// Create fragment collection with constructor (preferred when possible)
var items = frags(
    li("Apple"),
    li("Banana"),
    li("Cherry")
);

// Use ____() only when constructor won't work (e.g., in loops)
var items = frags();
for (var fruit : fruits) {
    items.____(li(fruit));  // ____ keeps visual focus on the HTML structure
                            // rather than the imperative "add" operation
}

// Empty fragments
frags()  // or
Frags.empty()
```

**PLEASE NOTE** : Sometimes AI coding agents default training behavious makes it use List<Frag<?>> pattern - please do not use. Instead use Frags as shown. You cannot directly add List<Frag<?>> in the luvml DOM, but you can add a Frags.

### Migration anti-pattern: don't transcribe `append(...)` into `____(...)`

When porting existing `StringBuilder`/string-concatenation HTML to luvml, AI agents tend to map **one append → one `.____()`**. That is mechanically correct but throws away the whole point of the DSL: the result reads like imperative "add" calls instead of an HTML tree. (This bias is specific to *migration* — when writing fresh, agents usually nest naturally.) Reserve `____()` for what a constructor genuinely cannot express: **loops** and a conditional that sits *mid-sequence*.

**Before** — the original string-building code:
```java
StringBuilder b = new StringBuilder();
b.append("<head><meta charset=\"utf-8\">");
if (autoRefresh > 0) b.append("<meta http-equiv=refresh content=").append(autoRefresh).append(">");
b.append("<title>").append(esc(title)).append("</title>");
b.append("</head>");
```

**Intermediate (AVOID)** — each `append` blindly rewritten as `____()`; still reads as imperative appends:
```java
var head = frags();
head.____(meta(charset("utf-8")));
if (autoRefresh > 0) head.____(meta(httpEquiv("refresh"), content("" + autoRefresh)));
head.____(title(esc(title)));   // (also redundant: luvml escapes for you — drop esc())
return head(head);
```

**Idiomatic** — build the tree with the constructor; `if_(...)` keeps the conditional inline and in order:
```java
return head(frags(
    meta(charset("utf-8")),
    if_(autoRefresh > 0, () -> meta(httpEquiv("refresh"), content("" + autoRefresh))),
    title(title)                 // no esc(): text is escaped at render time
));
```

`____()` still earns its place for loops (and for an incremental builder when a conditional truly interrupts the sequence):
```java
var rows = frags();
for (var u : users) rows.____(tr(td(u.name()), td(u.email())));   // loop → ____ is right
var table = table(thead(tr(th("Name"), th("Email"))), tbody(rows));
```

**Caveat (why the inline form sometimes won't compile):** `frags(x)` infers its element type `T` from the seed `x`, so `var h = frags(meta(...))` pins `T` to that element type and a later `h.____(title(...))` (a different type) is rejected. Either keep the whole thing in one constructor call (as above), or declare the accumulator broadly — `Frags<Frag_I<?>> h = frags();` — so `____()` accepts any fragment. Don't "solve" the compile error by reverting to one-append-per-line.

### Conditional Content

```java
// Conditional fragments
var content = frags(
    h1("Title"),
    if_(isLoggedIn, () -> div("Welcome back!")),
    p("Content")
);

// Optional attributes
var attrs = frags(
    id("main"),
    optionalAttr("data-user", () -> user != null ? user.getId() : null)
);
```

**`if_` works for attributes too — there is no separate `attrIf`.** This trips people up, so it is worth stating outright: an attribute *is* a fragment (`Attr_I extends Frag_I`), and an element sorts incoming fragments by type, routing `Attr_T` into its attribute map. So the same `if_` that guards an element guards an attribute, and when the condition is false it emits nothing at all — no attribute, no node, no marker:

```java
li(a(href(url), if_(current, () -> class_("sidebar_on")), text(label)))
```

Without that, the element has to be written out twice, once per branch — `current ? a(href(url), class_("sidebar_on"), text(label)) : a(href(url), text(label))` — which duplicates every unrelated attribute and grows combinatorially with a second condition. There is no no-op attribute value to put in a ternary, and none is needed.

The supplier can be a variable rather than an inline lambda, and it can be typed as narrowly as you like — `Supplier<HtmlAttribute>` and `Supplier<BlockContainerElement>` both fit, alongside the older `Supplier<Frag_I>`:

```java
Supplier<HtmlAttribute> highlight = () -> class_("sidebar_on");
li(a(href(url), if_(current, highlight), text(label)))
```

`if_` and `optionalAttr` are not interchangeable. `optionalAttr` takes an attribute *name* plus a `Supplier<String>` for its value, and on a null/blank value it emits an HTML comment marker (`<!--data-user=null-->`) into the body rather than nothing. Reach for `optionalAttr` when the attribute is fixed and only its value is in doubt, and for `if_` when the whole attribute is conditional or when the output must stay clean.

## Rendering

Consider a luvml DOM tree
```Java
var page = html(
    head(title("My Page")),
    body(h1("Hello"), p("Content"))
);
```

### The DOCTYPE is a node, never a string

A common anti-pattern (especially when migrating string-built HTML) is to glue the doctype on by hand: `"<!DOCTYPE html>\n" + renderer.render(page)`. **Don't.** That re-introduces exactly the string-mixing luvml exists to remove. The doctype is a first-class fragment — `D.html5()` returns a `DocType` node — so put it in the tree and render the whole document in one call:

```java
import static luvml.D.html5;      // luvml.D also has xhtml1_1(), html4_01_Strict(), ...
import static luvml.Frags.frags;

var document = frags(html5(), page);                 // <!DOCTYPE html> then <html>…</html>
var out = XHtmlStringRenderer.asFormatted(document, "  ");
```

The renderer emits `<!DOCTYPE html>` from the node itself — no concatenation, no manual escaping, one consistent pipeline.

### What the renderer does for you (the contract)

- **Escaping is automatic.** Text nodes escape `&` `<` `>`; attribute values also escape `"`. Never pre-escape your strings — there is no `esc()` step in luvml, and passing already-escaped text double-escapes it.
- **`<style>` and `<script>` are raw text.** Their content is emitted verbatim (not escaped), so a luvs `CssRules`/`CssRule` (both are `CharSequence`) goes straight into `E.style(...)`, and JS goes straight into `E.script(...)`.
- **Void elements self-close in XHTML mode** (`<meta … />`) and not in HTML mode (`<meta …>`). `asString` / `asFormattedString` default to XHTML; switch with `RenderConfig.builder().htmlMode()`.
- **`asString` is compact, `asFormattedString` indents.** Same markup, different whitespace — pick compact for bytes-on-the-wire, formatted for human-readable output.

### Don't embed JS/CSS in Java — load it from a resource file

`E.script(...)` and `E.style(...)` take a `CharSequence`, so it is tempting to paste a
blob of JavaScript (or CSS) straight into the Java source — usually inside a text block:

```java
// ❌ ANTI-PATTERN — JS embedded in Java
private static final String JS = """
        function copyTable(id,spec){ ... }
        function toast(msg){ ... }
        """;
...
E.script(JS)
```

**Do not do this**, for two reasons:

1. **Do not rely on Java text blocks (`"""`).** Treat `"""` as unavailable — do not use it
   to carry JS/CSS. It is not a stable foundation for this DSL's code, and the moment your JS
   needs a `\`, a `"`, or a `${}`, you are hand-escaping another language inside a Java literal.
2. **Mixing two languages in one file is exactly what luvml exists to avoid.** A `.java` file
   holding JavaScript gets no JS tooling — no syntax highlighting, no linting, no formatting —
   and the markup-building code drowns in an unrelated language.

**Instead, keep JS and CSS in dedicated files under the Maven resources folder**
(`src/main/resources/…`) and load them at runtime with `getResource(...)` /
`getResourceAsStream(...)`, then hand the loaded string to `E.script(...)` / `E.style(...)`
(which emit it verbatim — see the raw-text contract above):

**Don't repeat the try-with-resources boilerplate in every view — extract a tiny helper** and
reuse it for every resource (JS, CSS, templates). A one-method utility is enough:

```java
// ✅ Resources.java — write this once, reuse everywhere
final class Resources {
    private Resources() {}

    /** Read a UTF-8 text resource from the classpath root, e.g. "/html-view.js". */
    static String text(String path) {
        try (InputStream in = Resources.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Missing classpath resource: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read classpath resource: " + path, e);
        }
    }
}
```

Each view then becomes a single, readable line:

```java
// ✅ src/main/resources/html-view.js holds the real JavaScript (proper .js file, JS tooling applies)

// ✅ in the Java view — load once at class-init, serve verbatim
private static final String JS = Resources.text("/html-view.js");
...
E.script(JS)   // raw text — emitted unescaped, exactly as authored in the .js file
```

The resource path is classpath-absolute (leading `/`), and Maven copies anything under
`src/main/resources` onto the classpath automatically. The same rule applies to CSS — a `.css`
resource loaded into `E.style(...)` — though with luvs you usually build CSS type-safely in Java
rather than as a string. (Note that `\t`/`\n` live as real escape sequences in a `.js`/`.css`
file; you no longer double-escape them as `\\t`/`\\n` the way an embedded Java string forced you to.)

### Choosing a renderer

Two entry points. Both are static and both take the fragment as their first argument:

- **`XHtmlStringRenderer`** — the XHTML path, and the more thoroughly tested one. `asFormatted(page, "    ")` indents with the string you supply, `asFormatted(page)` indents with a single space, `asSingleLine(page)` emits one line.
- **`HtmlRenderer`** — the general path. `asString(page)` is compact, `asFormattedString(page)` indents, `toWriter(page, writer, config)` streams. `render(page, out)` is the core method the other three call; it is what you use with an `Out_I` you built yourself.

```java
import luvml.o.XHtmlStringRenderer;
import luvml.o.HtmlRenderer;

var xhtml     = XHtmlStringRenderer.asFormatted(page, "    ");   // indentation string
var compact   = HtmlRenderer.asString(page);
var formatted = HtmlRenderer.asFormattedString(page);
```

**Check first:** `jacli api luvml.o.HtmlRenderer` and `jacli api luvml.o.XHtmlStringRenderer` list the exact signatures in ~300ms, which is faster and safer than recalling them.

### An `Out` is a destination, never something you render *with*

The renderer takes the fragment; the `Out` receives bytes. `StringBuilderOut.render()` takes **no arguments** — it hands back the buffer already accumulated — and `WriterOut` has no `render` method at all. So the page is always an argument to `HtmlRenderer`, and the `Out` is the second argument:

```java
import luvml.o.StringBuilderOut;
import luvml.o.Out_A;

var out = new StringBuilderOut(Out_A.formatted());   // configure the destination
HtmlRenderer.render(page, out);                      // frag first, out second
var html = out.render();                             // no argument — hand back the buffer
```

### HTML mode instead of XHTML

The mode lives on `RenderConfig`, and an `Out` is configured at construction — the config is not passed to the render call. In XHTML mode void elements self-close (`<meta … />`); in HTML mode they do not (`<meta …>`). XHTML is the default.

```java
import luvml.o.RenderConfig;

var config = RenderConfig.builder().htmlMode().build();
HtmlRenderer.toWriter(page, writer, config);          // renders and flushes for you
```

### Streaming to a socket or a file

For server responses and large documents, stream rather than build a string — it avoids holding the whole document in memory. `toWriter` is the one-liner; the expanded form is there for when you want to hold the `Out` yourself:

```java
import luvml.o.WriterOut;
import java.io.OutputStreamWriter;

// HTTP response — toWriter flushes for you
HtmlRenderer.toWriter(page, new OutputStreamWriter(httpResponse.getOutputStream()), config);

// holding the Out yourself: configure it, render into it, flush it
try (var fileWriter = new FileWriter("output.html")) {
    var out = new WriterOut(fileWriter, b -> b.htmlMode());
    HtmlRenderer.render(page, out);
    out.flush();
}
```

### The doctype goes in the tree

Render `frags(html5(), page)` with `import static luvml.D.html5;` and the renderer emits `<!DOCTYPE html>` from the node itself — one pipeline, no concatenation. See "The DOCTYPE is a node, never a string" above.

## Verify the output, not the compile

luvml guarantees the markup is what you wrote. It cannot tell you the page is right. All of these compiled clean and shipped wrong: a link that 404s, a `<span>` given `margin-top`, body text inheriting a link colour from a wrapping `<a>`, two surfaces sharing a background so a boundary vanished, a missing margin, a doubled rule.

After generating, in this order:

**1. Read the generated file when the markup is in doubt.** The `.html` and `.css` on disk are the answer. Do not reason about what the DSL should have emitted — open it and look.

**2. Open the page and screenshot it, then read the image.**

```
chrome --headless=new --disable-gpu --hide-scrollbars \
  --virtual-time-budget=4000 --window-size=1440,1500 \
  --screenshot=shot.png "file:///abs/path/index.html"
```

`--virtual-time-budget` is required or web fonts have not loaded. `--window-size` height is the capture height. Chrome clamps its minimum *width* to ~489px and then crops the PNG to what you asked for, so a narrow `--window-size` looks exactly like horizontal overflow and is not — test narrow layouts with `<iframe width="390">` inside a large window, and settle overflow with `document.documentElement.scrollWidth` vs `clientWidth` rather than by eye.

Say what you expect to see *before* looking, then check whether the screenshot confirms it. An unstated assumption is not tested by a screenshot you glance at.

**3. Walk the links.** For each `.html`, resolve every relative `href`/`src` against its own directory and assert the file exists.

## Custom Semantic Elements

Define custom elements with automatic tag name mapping:

```java
import luvml.element.SemanticBlockContainerElement;
import luvml.element.SemanticInlineVoidElement;

// Different mapping strategies available via marker interfaces:

// 1. CamelCase_E: MyButton_E -> <myButton>
public class MyButton_E extends SemanticInlineContainerElement<MyButton_E> 
    implements CamelCase_E {
    public MyButton_E() { super(MyButton_E.class); }
}

// 2. LowerKebabFromCamelCase_E: MyButton_E -> <my-button> (web components)
public class MyButton_E extends SemanticInlineContainerElement<MyButton_E>
    implements LowerKebabFromCamelCase_E {
    public MyButton_E() { super(MyButton_E.class); }
}

// 3. LowerKebabAtUnderscores_E: My_Button_E -> <my-button>
public class My_Button_E extends SemanticInlineContainerElement<My_Button_E>
    implements LowerKebabAtUnderscores_E {
    public My_Button_E() { super(My_Button_E.class); }
}

// 4. XmlNamespaceColonAtUnderscores_E: Svg_Circle_E -> <svg:circle>
public class Svg_Circle_E extends SemanticInlineVoidElement<Svg_Circle_E>
    implements XmlNamespaceColonAtUnderscores_E {
    public Svg_Circle_E() { super(Svg_Circle_E.class); }
}

// 5. XmlNamespaceColonFromParentPackage_E: 
//    package myapp.svg; class Circle_E -> <svg:circle>
package myapp.svg;
public class Circle_E extends SemanticInlineVoidElement<Circle_E>
    implements XmlNamespaceColonFromParentPackage_E {
    public Circle_E() { super(Circle_E.class); }
}

// 6. LowerCase_E: MyButton_E -> <mybutton>
// 7. UpperCase_E: MyButton_E -> <MYBUTTON>
// 8. PreserveCase_E: MyButton_E -> <MyButton>
// 9. PreserveClassName: MyButton_E -> <MyButton_E>
```

### Composable Naming Strategies

The mapping strategies are **composable marker interfaces**. Your custom element class can implement any of them to control how the Java class name maps to the HTML/XML tag name. This is useful for:

- **Web Components**: Use `LowerKebabFromCamelCase_E` for standard kebab-case
- **XML Namespaces**: Use `XmlNamespaceColonAtUnderscores_E` or `XmlNamespaceColonFromParentPackage_E`
- **Custom DSLs**: Choose the strategy that fits your domain

Usage example:
```java
// After defining MyButton_E with LowerKebabFromCamelCase_E
var button = new MyButton_E()
    .addAttributes(id("submit"), class_("primary"))
    .addFragments(text("Click Me"));

// Renders as: <my-button id="submit" class="primary">Click Me</my-button>
```

## Complete Examples

### Simple HTML Page

```java
var page = html(
    head(
        meta(charset("UTF-8")),
        title("My Page")
    ),
    body(
        div(id("app"),
            h1("Welcome"),
            p("Hello World")
        )
    )
);
```

### Form Example

```java
var loginForm = form(action("/login"), method().Post(),
    div(class_("form-group"),
        label(for_("username"), "Username"),
        input(typeText(), id("username"), name("username"), required())
    ),
    div(class_("form-group"),
        label(for_("password"), "Password"),
        input(typePassword(), id("password"), name("password"), required())
    ),
    button(typeSubmit(), "Login")
);

```

### Dynamic List

```java
List<String> items = List.of("Apple", "Banana", "Cherry");

var list = frags();
for (var item : items) {
    // Use ____() for adding in loops - maintains visual focus on HTML structure
    list.____(li(item));
}

var page = ul(list);
```

### Table Generation

```java
record User(String name, String email) {}
List<User> users = getUsers();

var rows = frags();
for (var user : users) {
    rows.____(
        tr(
            td(user.name()),
            td(user.email())
        )
    );
}

var table = table(class_("users"),
    thead(
        tr(th("Name"), th("Email"))
    ),
    tbody(rows)
);
```


## Key Conventions

1. **Use constructor varargs when possible**: `frags(li("A"), li("B"), li("C"))`
2. **Use `____()` only when necessary**: Inside loops or when building incrementally
3. **The `____()` method**: Visually de-emphasizes the operation to keep focus on the HTML structure being built
4. **Varargs accept Frag_I or String**: `div("text1", "text2")` works directly
5. **Stream for production**: Use `WriterOut` to stream directly to sockets/responses for better performance

## Advanced Concepts
These are not needed if you just wish to use luvml for creating html/xhtml/xml, but the following records the internal implementation of luvml. LuxX is a the core concept on top of which luvml is made. Everything in any structured luml/luvx DOM tree is a **fragment** (`Frag_I`):

```java
package luvx;

public interface Frag_I<I extends Frag_I<I>> {
    FragType_I<I> fragType(); // Union type discrimination
}
```

### Frag_I Hierarchy and Exhaustive Pattern Matching

This creates a flexible hierarchy that adapts to any grammar. **Derive it, do not trust a copy** — `jacli impls luvx.Frag_I --scope luvml` prints the live tree, concrete classes included. The interface skeleton below is a summary of that output:

```
Frag_I<I>                                  // Root: any structured piece
├── Attr_I<I>                              // Key-value pairs: id="main", color="red"
├── Node_I<I>                              // Content nodes
│   ├── AttributelessNode_I<I>             // Nodes that cannot have attributes
│   │   ├── StringNode_I<I>                // Nodes containing string content
│   │   │   ├── Text_I<I>                  // Plain text content (HTML-escaped)
│   │   │   ├── Comment_I<I>               // <!-- comments -->
│   │   │   └── CData_I<I>                 // <![CDATA[ ]]> sections (not escaped)
│   │   └── Doctype_I<I>                   // <!DOCTYPE> declarations
│   └── Element_I<I>                       // Structured elements with attributes
│       ├── SelfClosingElement_I<I>        // Elements that cannot contain children
│       │   ├── VoidElement_I<I>           // HTML5 void elements: <br/>, <img/>
│       │   └── ProcessingInstruction_I<I> // <?xml ?>, <?php ?>
│       ├── RawTextElement_I<I>            // Text-only, NOT escaped: <style>, <script>
│       ├── EscapableRawTextElement_I<I>   // Text-only, escaped: <title>, <textarea>
│       └── ContainerElement_I<I>          // Elements that can contain child nodes
└── Frags_I<I>                             // Collections of fragments
```

Note that `RawTextElement_I` and `EscapableRawTextElement_I` are siblings of `ContainerElement_I` under `Element_I`, not children of it — they hold text, not child nodes, so they are a peer kind of element rather than a flavour of container. That split is what the `ElementType_I` union permits, and it is what the switch below has to match.

### Exhaustive Pattern Matching Example

The following demonstrates exhaustive and safe pattern matching with type casting on a `Frag_I`. The grammar is solid, type-safe, and prevents conflicts. This is particularly useful when writing custom renderers, parsers, or printers (to String, network socket, etc.).

The exhaustive pattern matching is responsible for making it possible to mix `Attr_I` and `Element_I` in the same varargs. It's not possible to have a class that implements both `Attr_I` and `Element_I` because the function signatures would conflict (intentional API design).

**Notice**: We are not using `default` case for the switch expression, it is because the pattern matching is exhaustive so default implementation is not required. It is also a way to validate that we indeed evaluated and considered all cases.

```java
String processFragment(Frag_I<?> frag) {
    return switch (frag.fragType()) {
        case Attr_T a -> {
            var attr = a.attr(); // Type: Attr_I - NO CASTING!
            yield "Attribute: " + attr.name() + "=" + attr.value();
        }
        case Node_T n -> switch (n.nodeType()) {
            case Element_T e -> switch (e.elementType()) {
                case SelfClosingElement_T sce -> switch (sce.selfClosingElementType()) {
                    case VoidElement_T ve -> {
                        var voidElement = ve.voidElement(); // Type: VoidElement_I - NO CASTING!
                        yield "Void Element: <" + voidElement.tagName() + "/>";
                    }
                    case ProcessingInstruction_T pi -> {
                        var procInstr = pi.processingInstruction(); // Type: ProcessingInstruction_I - NO CASTING!
                        yield "Processing Instruction: <?" + procInstr.target() + " " + procInstr.data() + "?>";
                    }
                };
                case RawTextElement_T rte -> {
                    var rawElement = rte.rawTextElement(); // Type: RawTextElement_I - NO CASTING!
                    yield "Raw Text Element: <" + rawElement.tagName() + ">" + rawElement.rawTextContent() + "</" + rawElement.tagName() + ">";
                }
                case EscapableRawTextElement_T erte -> {
                    var escElement = erte.escapableRawTextElement(); // Type: EscapableRawTextElement_I - NO CASTING!
                    yield "Escapable Raw Text Element: <" + escElement.tagName() + ">" + escElement.escapableTextContent() + "</" + escElement.tagName() + ">";
                }
                case ContainerElement_T ce -> {
                    var element = ce.containerElement(); // Type: ContainerElement_I - NO CASTING!
                    yield "Container Element: <" + element.tagName() + ">...</" + element.tagName() + ">";
                }
            };
            case AttributelessNode_T an -> switch (an.attributelessNodeType()) {
                case StringNode_T sn -> switch (sn.stringNodeType()) {
                    case Text_T t -> {
                        var text = t.text(); // Type: Text_I - NO CASTING!
                        yield "Text: " + text.text();
                    }
                    case Comment_T c -> {
                        var comment = c.comment(); // Type: Comment_I - NO CASTING!
                        yield "Comment: <!-- " + comment.comment() + " -->";
                    }
                    case CData_T cd -> {
                        var cdata = cd.cdata(); // Type: CData_I - NO CASTING!
                        yield "CDATA: <![CDATA[" + cdata.cdata() + "]]>";
                    }
                };
                case Doctype_T dt -> {
                    var doctype = dt.doctype(); // Type: Doctype_I - NO CASTING!
                    yield "Doctype: <!DOCTYPE " + doctype.name() + ">";
                }
            };
        };
        case Frags_T f -> {
            var frags = f.frags(); // Type: Frags_I - NO CASTING!
            yield "Collection: " + frags.fragments().size() + " items";
        }
    };
}
```

### Simplified Examples

For common use cases, you can use partial pattern matching:

#### Example 1: Processing Only Elements

```java
String processElements(Frag_I<?> frag) {
    return switch (frag.fragType()) {
        case Node_T n -> switch (n.nodeType()) {
            case Element_T e -> {
                var element = e.element(); // Type: Element_I - NO CASTING!
                yield "Element: <" + element.tagName() + ">";
            }
            default -> "Not an element";
        };
        default -> "Not a node";
    };
}
```

#### Example 2: Extracting Text Content

```java
String extractText(Frag_I<?> frag) {
    return switch (frag.fragType()) {
        case Node_T n -> switch (n.nodeType()) {
            case AttributelessNode_T an -> switch (an.attributelessNodeType()) {
                case StringNode_T sn -> switch (sn.stringNodeType()) {
                    case Text_T t -> t.text().text();
                    case CData_T cd -> cd.cdata().cdata();
                    default -> "";
                };
                default -> "";
            };
            default -> "";
        };
        default -> "";
    };
}
```

#### Example 3: Processing Container Elements

```java
void processContainers(Frag_I<?> frag) {
    switch (frag.fragType()) {
        case Node_T n -> {
            switch (n.nodeType()) {
                case Element_T e -> {
                    switch (e.elementType()) {
                        case ContainerElement_T ce -> {
                            var container = ce.containerElement(); // Type: ContainerElement_I - NO CASTING!
                            System.out.println("Container: <" + container.tagName() + ">");
                            System.out.println("Children: " + container.children().size());
                        }
                        default -> System.out.println("Not a container");
                    }
                }
                default -> System.out.println("Not an element");
            }
        }
        default -> System.out.println("Not a node");
    }
}
```

### Key Benefits

1. **Type Safety**: No explicit casting required - the type system ensures correctness
2. **Exhaustiveness**: Compiler enforces handling of all cases when you choose exhaustive matching
3. **No Conflicts**: The API design prevents classes from implementing conflicting interfaces
4. **Flexibility**: Can drill down to any level of specificity needed
5. **Extensibility**: The unsealed interfaces allow adding new implementations while maintaining type discrimination

### Design Principles

- **Self-bounded types**: Each interface uses `<I extends Interface<I>>` for type-safe self-reference
- **Type carriers**: The `_T` classes carry the typed instance without casting
- **Sealed hierarchy at type level**: The type carrier classes form a sealed hierarchy
- **Unsealed interfaces**: The actual interfaces are unsealed for extensibility
- **Intentional conflicts**: The API is designed so that it's impossible to implement both `Attr_I` and `Node_I` in the same class


#### Helpful tools for coding agents like claude-code
Coding agents like Claude code might not be familiar with luvml, so they must be provided with the source code of this project, and a tool called jacli ( https://github.com/littlejlib/jacli on need to visit this page ) which it must then use instead of javap or jad. Jacli (java code assistant cli) is an agentic cli tool which gives provides accurate insights into the code and increases claude-code's confidence and reduces API errors close to zero.