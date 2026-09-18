package luvml;

import luvx.rendering_behavior.BlockMarkupRendering;

/** UNSAFE -- see {@link RawText_A}. Block variant: creates structural boundaries with newlines. */
public final class BlockRawText extends RawText_A<BlockRawText> {

    public BlockRawText(String content) {
        super(content);
    }

    @Override
    public BlockMarkupRendering markupRenderingBehavior() {
        return BlockMarkupRendering.I;
    }

    @Override
    public BlockRawText self() {
        return this;
    }
}
