package luvml;

import luvx.*;
import luvx.composable.HasTextContent;

/**
 * UNSAFE. Abstract base for a text node whose content bypasses both whitespace
 * normalization and HTML escaping -- the renderer emits {@link #wholeText()} verbatim.
 * Only use with content you already trust or have already escaped yourself; passing
 * unescaped user input here is an XSS vector, no different than string-concatenating
 * HTML by hand. Prefer {@code T.text(...)} unless you have specifically measured that
 * escaping this content is a real cost and you can guarantee it is safe.
 */
abstract class RawText_A<T extends RawText_A<T>> implements Text_I<T>, HasTextContent<T> {

    private final String wholeTextContent;

    protected RawText_A(String content) {
        this.wholeTextContent = content;
    }

    @Override
    public final String wholeText() {
        return wholeTextContent;
    }

    @Override
    public final String text() {
        return wholeTextContent; // no normalization -- raw means raw
    }

    @Override
    public final String textContent() {
        return wholeTextContent;
    }

    @Override
    public final boolean isRaw() {
        return true;
    }
}
