package luvml;

import luvx.rendering_behavior.InlineMarkupRendering;

/** UNSAFE -- see {@link RawText_A}. Inline variant: flows with content, no structural boundaries. */
public final class InlineRawText extends RawText_A<InlineRawText> {

    public InlineRawText(String content) {
        super(content);
    }

    @Override
    public InlineMarkupRendering markupRenderingBehavior() {
        return InlineMarkupRendering.I;
    }

    @Override
    public InlineRawText self() {
        return this;
    }
}
