package luvml;

import luvml.o.HtmlRenderer;
import luvx.Frag_I;

import java.util.ArrayList;
import java.util.List;

import static luvml.E.*;
import static luvml.A.class_;

// Diagnoses why the "presentations" workload (10 panels of title + long free-text summary)
// is much slower, relatively, than the "stocks" workload (20 rows of short numbers/strings).
// Compares real render time using normal text() (escaped + whitespace-normalized) against
// InlineCData for the summary field ONLY -- CDATA bypasses both escaping and normalization
// in HtmlRenderer, so it stands in for htmlflow's .raw(...) for this diagnostic. This is NOT
// a real fix (CDATA sections are invalid in HTML5 outside foreign content, and skipping
// escaping is unsafe for real content) -- it's a temporary proxy purely to measure how much
// of the gap is attributable to escaping/normalizing long text.
public class PresentationsSlowdownDiagnostic {

    // Real summaries from the xmlet/template-benchmark Presentation model, representative
    // lengths (short/medium/long) and containing real & and < characters that force the
    // escaping "needs escape" slow path.
    private static final String[] SUMMARIES = {
        "Are you still using JavaServer Pages as your main template language? With the popularity of template engines for other languages like Ruby and Scala and the shift in doing more MVC in the browser there are quite some new and interesting new template languages available for the JVM. During this session we will take a look at the less known, but quite interesting new template engines and see how they compare with the industries standards. \r<br/>",
        "Legitimate websites such as news sites happen to get compromised by attackers injecting malicious content. The aim of these so-called &#8220;watering hole attacks&#8221; is to infect as many visitors of a website as possible, and are sometimes even targeted at a specific group of individuals. It is increasingly important to detect these infections at an early stage.\r<br/>\r<br/>HoneySpider Network to the rescue! \r<br/>\r<br/>It is a Java based open source framework that automatically scans website urls, analyses the results and reports on any malware detected.\r<br/>Attend this talk to gain a better understanding of malware detection and client honeypots and get an overview of the HoneySpider Network&#8217;s architecture, its code and its plugins it uses. A live demo is also included!",
        "De technische eisen aan webapplicaties veranderen in hoog tempo. Enkele jaren geleden nog gebruikten de grootere applicaties enkele tientallen servers en werden response tijden van een seconde en onderhoudsvensters van enkele uren nog geaccepteerd. Tegenwoordig moeten applicaties 100% beschikbaar zijn, terwijl de gebruiker in enkele milliseconden antwoord wil krijgen. Om pieken in gebruik op te kunnen vangen moeten de applicaties op duizenden processoren in een cloud omgeving kunnen draaien.\r<br/>De tekortkomingen van de huidige standaard architectuurprincipes kunnen worden opgevangen door een zogenaamde &#8220;reactive architecture&#8221;.",
        "Opening",
        "Keynote van ING, gepresenteerd door Amir Arooni en Peter Jacobs.",
    };

    public static void main(String[] args) {
        var withText = buildPage(false);
        var withCData = buildPage(true);

        System.out.println("=== full page render, best-of-5, 20000 iterations ===");
        var textMs = time(() -> HtmlRenderer.asString(withText));
        var cdataMs = time(() -> HtmlRenderer.asString(withCData));
        System.out.printf("  text() (escaped+normalized)  : %8.1fms  ->  %.0f ops/s%n", textMs, 20000 / (textMs / 1000));
        System.out.printf("  InlineCData (raw, diagnostic): %8.1fms  ->  %.0f ops/s%n", cdataMs, 20000 / (cdataMs / 1000));
        System.out.printf("  escaping+normalization overhead: %.1f%% of total render time%n", 100.0 * (textMs - cdataMs) / textMs);
    }

    private static Frag_I<?> buildPage(boolean raw) {
        var panels = new ArrayList<Frag_I<?>>(SUMMARIES.length);
        for (var i = 0; i < SUMMARIES.length; i++) {
            var summary = SUMMARIES[i];
            panels.add(div(class_("panel panel-default"),
                div(class_("panel-heading"), h3(class_("panel-title"), text("Talk " + i))),
                div(class_("panel-body"), raw ? new InlineCData(summary) : text(summary))
            ));
        }
        return div(panels).addAttributes(class_("container"));
    }

    private static double time(java.util.function.Supplier<String> work) {
        var iterations = 20_000;
        work.get(); // warm up
        for (var i = 0; i < 5_000; i++) work.get();
        var best = Long.MAX_VALUE;
        for (var run = 0; run < 5; run++) {
            var start = System.nanoTime();
            for (var i = 0; i < iterations; i++) work.get();
            best = Math.min(best, System.nanoTime() - start);
        }
        return best / 1_000_000.0;
    }
}
