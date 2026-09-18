package luvml;

import luvml.o.HtmlRenderer;
import luvx.Frag_I;

import java.util.List;

import static luvml.E.*;
import static luvml.DynamicFrag.dynamicFrag;
import static luvml.Frags.frags;

public class DynamicFragVerification {

    private static final ThreadLocal<List<String>> CURRENT = new ThreadLocal<>();

    // built exactly once — this is the object identity we assert stays fixed across renders
    private static final Frag_I<?> TEMPLATE = div(
        h1("Static Title"),
        ul(dynamicFrag(() -> frags(CURRENT.get().stream().map(s -> (Frag_I) li(text(s))).toList())))
    );

    public static void main(String[] args) {
        var beforeIdentity = System.identityHashCode(TEMPLATE);

        CURRENT.set(List.of("a", "b"));
        var first = HtmlRenderer.asString(TEMPLATE);
        System.out.println("render 1: " + first);

        CURRENT.set(List.of("x", "y", "z"));
        var second = HtmlRenderer.asString(TEMPLATE);
        System.out.println("render 2: " + second);

        var afterIdentity = System.identityHashCode(TEMPLATE);

        check("same tree object reused across renders", beforeIdentity == afterIdentity);
        check("render 1 reflects first data set", first.contains("<li>a</li>") && first.contains("<li>b</li>") && !first.contains(">x<"));
        check("render 2 reflects second data set, not the first", second.contains("<li>x</li>") && second.contains("<li>y</li>") && second.contains("<li>z</li>") && !second.contains(">a<"));
        check("static title present in both", first.contains("Static Title") && second.contains("Static Title"));
    }

    private static void check(String label, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + label);
        System.out.println("OK: " + label);
    }
}
