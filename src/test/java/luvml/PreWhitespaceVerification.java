package luvml;

import luvml.o.HtmlRenderer;
import static luvml.E.*;

public class PreWhitespaceVerification {

    public static void main(String[] args) {
        var preContent = "line1\n    line2\nline3";
        var doc = div(pre(text(preContent)));

        var plain = HtmlRenderer.asString(doc);
        var formatted = HtmlRenderer.asFormattedString(doc);

        System.out.println("--- plain ---");
        System.out.println(plain);
        System.out.println("--- formatted ---");
        System.out.println(formatted);

        check("plain output", plain.contains("<pre>" + preContent + "</pre>"), plain);
        check("formatted output", formatted.contains("<pre>" + preContent + "</pre>"), formatted);

        var nested = div(pre(code(text("a\n  b"))));
        var nestedFormatted = HtmlRenderer.asFormattedString(nested);
        System.out.println("--- nested pre>code formatted ---");
        System.out.println(nestedFormatted);
        check("nested pre>code preserves whitespace",
              nestedFormatted.contains("<pre><code>a\n  b</code></pre>"), nestedFormatted);

        var twoParas = div(p(text("first")), p(text("second")));
        var twoParasFormatted = HtmlRenderer.asFormattedString(twoParas);
        System.out.println("--- ordinary block formatting still applies ---");
        System.out.println(twoParasFormatted);
        check("ordinary block elements still get structural newlines",
              twoParasFormatted.contains("first") && twoParasFormatted.lines().count() > 1,
              twoParasFormatted);
    }

    private static void check(String label, boolean ok, String actual) {
        if (!ok) {
            throw new AssertionError(label + " corrupted pre content:\n" + actual);
        }
        System.out.println(label + ": OK");
    }
}
