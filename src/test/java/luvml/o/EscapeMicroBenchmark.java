package luvml.o;

import java.util.function.Function;

// Isolates the escape()/normalizeWhitespace() fixes from system-level noise by comparing
// old-vs-new implementations back to back in the SAME JVM invocation, on the same strings.
// A cross-process JMH run of the whole benchmark suite moves with whatever else is running
// on the machine (observed: htmlflow's own number, untouched by these fixes, moved ~20%
// between two runs) -- this does not.
public class EscapeMicroBenchmark {

    private static String oldEscapeText(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String oldNormalizeWhitespace(String text) {
        if (text == null || text.isEmpty()) return text;
        var sb = new StringBuilder(text.length());
        var lastWasWhite = false;
        for (var i = 0; i < text.length(); i++) {
            var c = text.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\f' || c == '\r' || c == 160) {
                if (!lastWasWhite) { sb.append(' '); lastWasWhite = true; }
            } else if (c != 8203 && c != 173) {
                sb.append(c);
                lastWasWhite = false;
            }
        }
        return sb.toString();
    }

    private static final String[] REAL_SUMMARIES = {
        "Are you still using JavaServer Pages as your main template language? With the popularity of template engines for other languages like Ruby and Scala and the shift in doing more MVC in the browser there are quite some new and interesting new template languages available for the JVM. During this session we will take a look at the less known, but quite interesting new template engines and see how they compare with the industries standards. \r<br/>",
        "Legitimate websites such as news sites happen to get compromised by attackers injecting malicious content. The aim of these so-called &#8220;watering hole attacks&#8221; is to infect as many visitors of a website as possible, and are sometimes even targeted at a specific group of individuals. It is increasingly important to detect these infections at an early stage.\r<br/>\r<br/>HoneySpider Network to the rescue! \r<br/>\r<br/>It is a Java based open source framework that automatically scans website urls, analyses the results and reports on any malware detected.\r<br/>Attend this talk to gain a better understanding of malware detection and client honeypots and get an overview of the HoneySpider Network&#8217;s architecture, its code and its plugins it uses. A live demo is also included!",
        "De technische eisen aan webapplicaties veranderen in hoog tempo.\r<br/>De tekortkomingen van de huidige standaard architectuurprincipes kunnen worden opgevangen door een zogenaamde &#8220;reactive architecture&#8221;.",
    };

    public static void main(String[] args) {
        System.out.println("=== cross-check: old vs new produce IDENTICAL output on real content ===");
        for (var i = 0; i < REAL_SUMMARIES.length; i++) {
            var s = REAL_SUMMARIES[i];
            var oldNorm = oldNormalizeWhitespace(s);
            var newNorm = luvx.Text_I.normalizeWhitespace(s);
            if (!oldNorm.equals(newNorm)) throw new AssertionError("normalize mismatch on summary " + i);
            var oldEsc = oldEscapeText(oldNorm);
            var newEsc = HtmlRenderer.escape(newNorm, false);
            if (!oldEsc.equals(newEsc)) throw new AssertionError("escape mismatch on summary " + i);
            System.out.println("OK: summary " + i + " (" + s.length() + " chars) identical old vs new");
        }
        System.out.println();

        var shortClean = "ADBE";
        var longClean = "Are you still using JavaServer Pages as your main template language? "
            + "With the popularity of template engines for other languages like Ruby and Scala "
            + "and the shift in doing more MVC in the browser there are quite some new and "
            + "interesting new template languages available for the JVM.";
        var withSpecialChars = "Legitimate websites & news sites <get> compromised by attackers.";

        System.out.println("=== escapeTextContent (old: 3x String.replace, new: single-pass) ===");
        bench("short, clean (\"ADBE\")", shortClean, EscapeMicroBenchmark::oldEscapeText, s -> HtmlRenderer.escape(s, false));
        bench("long, clean (~270 chars)", longClean, EscapeMicroBenchmark::oldEscapeText, s -> HtmlRenderer.escape(s, false));
        bench("long, with & < > (~65 chars)", withSpecialChars, EscapeMicroBenchmark::oldEscapeText, s -> HtmlRenderer.escape(s, false));

        System.out.println();
        System.out.println("=== normalizeWhitespace (old: always rebuild, new: fast-path clean text) ===");
        bench("short, clean (\"ADBE\")", shortClean, EscapeMicroBenchmark::oldNormalizeWhitespace, luvx.Text_I::normalizeWhitespace);
        bench("long, clean (~270 chars)", longClean, EscapeMicroBenchmark::oldNormalizeWhitespace, luvx.Text_I::normalizeWhitespace);
    }

    private static void bench(String label, String input, Function<String, String> oldFn, Function<String, String> newFn) {
        var iterations = 200_000;
        // warm up both paths equally
        for (var i = 0; i < 20_000; i++) { oldFn.apply(input); newFn.apply(input); }

        var oldStart = System.nanoTime();
        for (var i = 0; i < iterations; i++) oldFn.apply(input);
        var oldNs = System.nanoTime() - oldStart;

        var newStart = System.nanoTime();
        for (var i = 0; i < iterations; i++) newFn.apply(input);
        var newNs = System.nanoTime() - newStart;

        System.out.printf("  %-32s  old=%8.1fms  new=%8.1fms  ratio=%.2fx%n",
            label, oldNs / 1_000_000.0, newNs / 1_000_000.0, (double) oldNs / newNs);
    }
}
