package luvml;

import luvml.o.HtmlRenderer;
import luvml.o.XHtmlStringRenderer;

import static luvml.E.div;
import static luvml.E.p;
import static luvml.T.raw;
import static luvml.T.text;

public class RawTextVerification {

    public static void main(String[] args) {
        var dangerous = "<script>alert('x')</script> & \"quotes\" and   extra   spaces\t\n";

        // raw: verbatim, both renderers, no escaping, no whitespace normalization
        check("HtmlRenderer raw is verbatim",
            HtmlRenderer.asString(p(raw(dangerous))),
            "<p>" + dangerous + "</p>");
        check("XHtmlStringRenderer raw is verbatim",
            XHtmlStringRenderer.asSingleLine(p(raw(dangerous))),
            "<p>" + dangerous + "</p>");

        // text: still escaped and normalized as before -- raw must not affect the normal path
        check("HtmlRenderer text() still escapes+normalizes",
            HtmlRenderer.asString(p(text(dangerous))),
            "<p>&lt;script&gt;alert('x')&lt;/script&gt; &amp; \"quotes\" and extra spaces </p>");
        check("XHtmlStringRenderer text() still escapes+normalizes",
            XHtmlStringRenderer.asSingleLine(p(text(dangerous))),
            "<p>&lt;script&gt;alert('x')&lt;/script&gt; &amp; \"quotes\" and extra spaces </p>");

        // mixing raw and normal text in the same tree: raw must not leak its behavior onto siblings
        var mixed = div(p(raw("<b>bold</b>")), p(text("<b>not bold</b>")));
        check("raw does not leak to sibling text nodes",
            HtmlRenderer.asString(mixed),
            "<div><p><b>bold</b></p><p>&lt;b&gt;not bold&lt;/b&gt;</p></div>");

        System.out.println("all raw-text checks passed");
    }

    private static void check(String label, String actual, String expected) {
        if (!actual.equals(expected)) {
            throw new AssertionError(label + ":\n  expected [" + expected + "]\n  actual   [" + actual + "]");
        }
        System.out.println("OK: " + label);
    }
}
