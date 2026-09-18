package luvml.o;

import luvx.Frag_I;

import java.util.ArrayList;
import java.util.function.Supplier;

import static luvml.E.*;
import static luvml.A.class_;

// Ad-hoc timing comparison (no JMH dependency in this project) between the current
// streaming HtmlRenderer (writes directly to a StringBuilderOut, one pass, no
// intermediate copies) and NaiveConcatRenderer (returns a String at every level and
// concatenates — the pattern PRP 03 replaced). The point is not a precise throughput
// number but the SHAPE: streaming is O(n) in document size, naive concatenation of a
// wide container is O(n^2), and the two diverge sharply as row count grows.
public class StreamingVsConcatBenchmark {

    private static final String FILLER =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor.";

    private static long blackhole; // prevents dead-code-eliminating the render result

    public static void main(String[] args) {
        System.out.printf("%8s  %14s  %14s  %10s%n", "rows", "streaming(ms)", "naive-concat(ms)", "ratio");
        for (var rows : new int[]{200, 800, 3200, 12800, 51200}) {
            var doc = buildDoc(rows);

            var streamingMs = time(() -> HtmlRenderer.asString(doc));
            var naiveMs = time(() -> NaiveConcatRenderer.render(doc));

            System.out.printf("%8d  %14.1f  %14.1f  %9.1fx%n",
                rows, streamingMs, naiveMs, naiveMs / streamingMs);
        }
        System.out.println("(blackhole=" + blackhole + ")");
    }

    private static Frag_I<?> buildDoc(int rows) {
        var children = new ArrayList<Frag_I<?>>(rows);
        for (var i = 0; i < rows; i++) {
            children.add(p(class_("row"), text("Row " + i + ": " + FILLER)));
        }
        return body(div(children));
    }

    private static double time(Supplier<String> work) {
        // warm up the JIT on this exact size/shape before timing, then take the best of 3
        blackhole += work.get().length();
        var best = Long.MAX_VALUE;
        for (var i = 0; i < 3; i++) {
            var start = System.nanoTime();
            blackhole += work.get().length();
            best = Math.min(best, System.nanoTime() - start);
        }
        return best / 1_000_000.0;
    }
}
