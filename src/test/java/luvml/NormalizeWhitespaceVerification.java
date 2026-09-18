package luvml;

import luvml.o.HtmlRenderer;

import static luvml.E.p;

public class NormalizeWhitespaceVerification {

    public static void main(String[] args) {
        check("clean text unchanged", "hello world", "hello world");
        check("single space runs preserved", "a b c", "a b c");
        check("tab becomes space", "a\tb", "a b");
        check("newline becomes space", "a\nb", "a b");
        check("cr becomes space", "a\rb", "a b");
        check("nbsp becomes space", "a b", "a b");
        check("consecutive spaces collapse", "a   b", "a b");
        check("consecutive mixed whitespace collapses", "a \t\n b", "a b");
        check("leading whitespace normalized", " \t hello", " hello");
        check("trailing whitespace normalized", "hello \t ", "hello ");
        check("invisible chars removed", "a​b­c", "abc");
        check("empty string", "", "");

        System.out.println("all normalization checks passed");
    }

    private static void check(String label, String input, String expectedNormalized) {
        var actual = HtmlRenderer.asString(p(T.text(input)));
        var expected = "<p>" + expectedNormalized + "</p>";
        if (!actual.equals(expected)) {
            throw new AssertionError(label + ": expected [" + expected + "] but got [" + actual + "]");
        }
        System.out.println("OK: " + label);
    }
}
