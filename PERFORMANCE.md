# Performance

Full method, numbers, and every honest caveat behind the one-paragraph conclusion in the [README](README.md#performance).

## Why performance was never the main focus

We are still very highly performant compared to many other engines — it's actually that we are more than enough performant, so we didn't go too deep into obsession along this line. Yes, more improvements can still be made (this whole document is exactly that: where we looked, and what we found), but we don't chase them for their own sake — our own real websites built with luvml are already, and really, very snappy.

Where the effort went instead: bringing CSS into the same typed DSL ([luvs](https://github.com/luvml/luvs)), a typed Vue layer on top ([luvue](https://github.com/luvml/luvue)), and working on the client side too — none of which are priorities for htmlflow, whose own performance is not unachievable for luvml, we simply haven't chased it. That is not to belittle htmlflow — in fact it is our source of inspiration. The tricky bits of the DSL, the self-referencing generics, the design of the DSL itself, are heavily inspired by htmlflow. We dropped a lot of that complexity (while adding a bit of our own), and what came out of it is, we think, quite interesting, powerful and promising.

## Streaming vs building one big String first

luvml's renderer never builds a whole document as one big `String` before writing it out — it streams straight to a `StringBuilder`, a `Writer`, or your own sink, one write at a time. Measured directly against the naive alternative (return a `String` at every recursion level and concatenate), best-of-3 on JDK 25:

| rows | streaming | naive concatenation | ratio |
|---:|---:|---:|---:|
| 200 | 0.8 ms | 2.1 ms | 2.6x |
| 800 | 1.4 ms | 8.6 ms | 6.3x |
| 3,200 | 5.7 ms | 230.9 ms | 40.5x |
| 12,800 | 27.4 ms | 2,141.8 ms | 78.3x |
| 51,200 | 115.1 ms | 36,111.1 ms | 313.6x |

Naive string concatenation is quadratic in document size (every `+=` copies everything accumulated so far); streaming is linear. The gap is barely visible at 200 rows and 313x at 51,200 — the exact shape "large pages are very slow" tends to take.

## Against other Java HTML libraries

Measured with JMH on [htmlflow](https://github.com/xmlet/HtmlFlow)'s own published benchmark workloads via [xmlet/template-benchmark](https://github.com/xmlet/template-benchmark):

| Benchmark (fresh tree per render) | stocks (ops/s) | presentations (ops/s) |
|---|---:|---:|
| htmlflow | 64,961 | 282,465 |
| **luvml** | 18,970 | 18,617 |
| j2html | 6,695 | 14,112 |

htmlflow compiles its element tree once and only re-binds leaf values per render; luvml and j2html both build a fresh tree every call, which is the fairer comparison for luvml's default style — and luvml is clearly 2nd there, 1.3–2.7x j2html.

## On htmlflow, honestly

So, given the above, we went and checked properly, rather than just asserting it:

**The same effect as htmlflow's compiled view is reachable in luvml today by hand** — cache the static shell as a `String` once, stream only what actually changes per render:

```
Benchmark (cached shell, hand-streamed dynamic part)   stocks (ops/s)   presentations (ops/s)
htmlflow                                                       64,961                282,465
luvml (cached-shell style)                                    112,252                 83,929
```

On the stocks page — mostly-static markup with a small dynamic table — this **beats htmlflow**. On presentations, htmlflow still wins clearly; its comparator also renders that page's dynamic text with `.raw()` (unescaped), while luvml's version properly HTML-escapes it, which is real extra work htmlflow's number isn't paying for.

**Before trusting that "beats htmlflow" number, we checked it wasn't a cheap trick.** Caching a whole rendered shell as one `String` sounds like it might be spending memory to buy speed in a way htmlflow doesn't need to. So we read htmlflow's own source rather than guess. It does the same thing internally: on first render it runs a one-time "preprocessing" pass that turns the view into a chain of `HtmlContinuation` objects, and `HtmlContinuationSyncStatic` holds a precomputed `String staticHtmlBlock` (even `.intern()`-ed) whose entire job on every render is `visitor.write(staticHtmlBlock)`. htmlflow does not regenerate its static markup by walking objects on every render either — it also caches pre-rendered text for everything that doesn't change, and only the genuinely dynamic parts run fresh code each time. So this is not a cheat relative to htmlflow's real mechanism — it independently landed on the same technique, just by hand and only at one split point, where htmlflow automates it and can interleave many dynamic points in one chain.

**Then we asked the harder question: what would it take for luvml to do this automatically, staying inside the typed DSL the whole time, instead of hand-splitting a string?** There already was a class for exactly this, `luvml.DynamicFrag` — build a tree once, and one slot inside it re-evaluates on every render. It had never actually been used anywhere, and turned out to be broken: the container that held it was resolving it eagerly, at tree-build time, not at render time, so the "dynamic" part only ever ran once and then froze. We found this by writing a test that actually exercised it, not by reading the class and assuming. **Fixed it** — the fix lives entirely in one class, `MutableContainerElement_A`, no change needed to the lower-level `luvx-base` module — and verified the fix with a test that renders the same tree object twice with different data and checks both renders come out right.

With that fixed, a genuinely dynamic, fully-typed render is possible:

```java
private static final ThreadLocal<List<Stock>> CURRENT = new ThreadLocal<>();
private static final Frag_I<?> TEMPLATE = html(head(...), body(h1(...),
    table(thead(...), tbody(dynamicFrag(() -> buildRows(CURRENT.get()))))
));

public static String render(List<Stock> stocks) {
    CURRENT.set(stocks);
    try { return HtmlRenderer.asString(TEMPLATE); } finally { CURRENT.remove(); }
}
```

Measured honestly, this does **not** reach htmlflow-level throughput — it lands close to plain fresh-tree luvml, sometimes marginally behind it. We built a second, isolated benchmark to find out why rather than just report a disappointing number: hold a large fixed static part and one small dynamic part, and grow the static part from 50 to 12,800 paragraphs. Reusing the object tree wins only 1.1x–3.6x — nowhere near htmlflow's margin, and not growing the way avoiding allocation should predict. The reason: luvml's renderer always does a full generic walk of the tree — type dispatch, attribute-map iteration, text escaping — on every render, for every node, whether that node's object was just built or reused. Reusing objects skips allocation; it does not skip the walk, and the walk is what actually dominates. htmlflow's continuation chain skips the walk entirely for static content, because by the time it renders, the static parts aren't objects to walk anymore — they're already text.

**So, plainly: `DynamicFrag` is now a real, correctly-scoped, working feature** — build a tree once, safely rebind a subtree of it per render, without ever leaving the typed DSL — **and that's worth having on its own terms. It is not, by itself, htmlflow's performance.** Actually closing that gap would mean giving luvml its own version of htmlflow's compile-to-a-continuation-chain step, automatically. That's a genuine, bigger design project, and it isn't done here. The cached-shell pattern above remains the fastest option available today — and now we know it's a legitimate technique, not a shortcut around the DSL.

## `presentations` was slow because of correctness, and now there's an escape hatch

We went looking for exactly why the `presentations` page lagged so far behind htmlflow, rather than leaving it as an unexplained gap. Diagnosed directly: that page has 10 panels of long free-text (some 500-800 characters, already containing real HTML entities and `<br/>` tags), and **escaping + whitespace-normalizing that text accounted for 70% of total render time.** `stocks`' cells are short numbers, where escaping is nearly free — that's the entire reason the two pages behaved so differently, and it's also exactly why htmlflow wins presentations by such a margin: its own comparator for that page uses `.raw(...)` and pays none of that cost, while luvml's did, correctly.

We made the escaping code itself faster (bulk-copying unchanged runs instead of rebuilding character-by-character — a real, verified win with no behavior change), but that reduces the cost, it doesn't remove it. So we added `T.raw(content)` — a plain, explicit, opt-in escape hatch: content goes through byte-for-byte, no escaping, no normalization. Several real luvml users had already been hand-rolling their own version of exactly this, which is the clearest sign a library is missing something it should just provide. Using it on the one field where it matters, matching what htmlflow's own comparator does for that same page:

```
Benchmark                                stocks (ops/s)   presentations (ops/s)
htmlflow                                       61,642                  224,834
luvml, safe (text())                           15,754                   19,547
luvml, raw() on the long field                      —                   56,088
luvml+DynamicFrag, safe                        12,549                   16,351
luvml+DynamicFrag, raw() on the long field           —                   45,525
luvml, cached-shell style                     103,442                   71,311
```

Opting into `raw(...)` where it matters is a ~2.8-2.9x win on this page. htmlflow still wins presentations even against the raw variant — and that remaining gap is now honestly architecture (its continuation-chain rendering vs luvml's generic tree walk), not luvml paying a safety cost htmlflow skips. That's the clean answer this whole investigation was for.

**`raw(...)` is unsafe by design** — it is the same risk as string-concatenating HTML by hand, and exists for exactly the case above: content you already trust or have already escaped yourself, where you've actually measured that escaping it is a real cost. `text(...)` remains the default, and should stay the default in your code too, unless you have the same kind of evidence this section does.

Full walkthrough, including the streaming/`<pre>` bug fixes and the naive-concatenation benchmark method: [`luvml_tutorial.md`](luvml_tutorial.md#streaming-vs-building-one-big-string-first).
