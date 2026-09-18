package luvml;

import luvml.o.HtmlRenderer;
import luvx.Frag_I;

import java.util.ArrayList;
import java.util.function.Supplier;

import static luvml.E.*;
import static luvml.DynamicFrag.dynamicFrag;
import static luvml.Frags.frags;

// Isolates the ONE thing DynamicFrag actually buys you: not rebuilding a LARGE, unchanging
// part of a page when only a SMALL part of it varies per render. The Stocks/Presentations
// benchmarks in the template-benchmark comparison are dominated by their dynamic rows, so
// DynamicFrag barely shows a difference there (see 03-prp.status.md). This benchmark
// instead holds the dynamic part small and fixed, and grows the static part, to show the
// shape of the win directly rather than asserting it.
public class DynamicFragBenchmark {

    private static final ThreadLocal<Integer> COUNTER = new ThreadLocal<>();

    public static void main(String[] args) {
        System.out.printf("%12s  %14s  %14s  %10s%n", "staticPs", "rebuild(ms)", "dynamicFrag(ms)", "ratio");
        for (var staticCount : new int[]{50, 200, 800, 3200, 12800}) {
            var dynamicTemplate = buildTemplate(staticCount); // built ONCE, reused across all renders below

            var rebuildMs = time(() -> renderByRebuilding(staticCount));
            var dynamicMs = time(() -> renderReusingTemplate(dynamicTemplate));

            System.out.printf("%12d  %14.3f  %14.3f  %9.2fx%n",
                staticCount, rebuildMs, dynamicMs, rebuildMs / dynamicMs);
        }
    }

    private static Frag_I<?> buildTemplate(int staticCount) {
        var statics = new ArrayList<Frag_I<?>>(staticCount);
        for (var i = 0; i < staticCount; i++) {
            statics.add(p("Static paragraph number " + i + " — never changes between renders."));
        }
        statics.add(dynamicFrag(() -> p("Dynamic: request #" + COUNTER.get())));
        return body(statics);
    }

    private static String renderReusingTemplate(Frag_I<?> template) {
        COUNTER.set(1);
        return HtmlRenderer.asString(template);
    }

    private static String renderByRebuilding(int staticCount) {
        COUNTER.set(1);
        return HtmlRenderer.asString(buildTemplate(staticCount));
    }

    private static double time(Supplier<String> work) {
        var blackhole = 0L;
        blackhole += work.get().length(); // warm up
        var best = Long.MAX_VALUE;
        for (var i = 0; i < 5; i++) {
            var start = System.nanoTime();
            blackhole += work.get().length();
            best = Math.min(best, System.nanoTime() - start);
        }
        if (blackhole < 0) System.out.print(""); // keep the JIT honest without printing noise
        return best / 1_000_000.0;
    }
}
