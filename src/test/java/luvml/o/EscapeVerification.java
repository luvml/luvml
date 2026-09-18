package luvml.o;

import static luvml.E.*;
import static luvml.A.href;

public class EscapeVerification {

    public static void main(String[] args) {
        check("no special chars, text", HtmlRenderer.asString(p("plain text")), "<p>plain text</p>");
        check("no special chars, attribute", HtmlRenderer.asString(a(href("plain"), text("x"))), "<a href=\"plain\">x</a>");

        check("ampersand only", HtmlRenderer.asString(p("a & b")), "<p>a &amp; b</p>");
        check("all three, text", HtmlRenderer.asString(p("<a & b>")), "<p>&lt;a &amp; b&gt;</p>");
        check("leading special char", HtmlRenderer.asString(p("<start")), "<p>&lt;start</p>");
        check("trailing special char", HtmlRenderer.asString(p("end>")), "<p>end&gt;</p>");
        check("consecutive special chars", HtmlRenderer.asString(p("<<>>&&")), "<p>&lt;&lt;&gt;&gt;&amp;&amp;</p>");

        check("attribute with quote", HtmlRenderer.asString(a(href("a\"b"), text("x"))), "<a href=\"a&quot;b\">x</a>");
        check("text with quote is NOT escaped", HtmlRenderer.asString(p("say \"hi\"")), "<p>say \"hi\"</p>");

        System.out.println("all escape checks passed");
    }

    private static void check(String label, String actual, String expected) {
        if (!actual.equals(expected)) {
            throw new AssertionError(label + ": expected [" + expected + "] but got [" + actual + "]");
        }
        System.out.println("OK: " + label);
    }
}
