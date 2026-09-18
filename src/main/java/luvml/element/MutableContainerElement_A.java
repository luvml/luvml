package luvml.element;

import luvx.mutable.*;
import luvx.*;
import luvx.ftype.*;


import java.util.*;
import luvml.DynamicFrag;
import luvml.InlineText;

public sealed abstract class MutableContainerElement_A<I extends MutableContainerElement_A<I>> 
        extends MutableElement_A<I>
        implements MutableContainerElement_I<I> 
        permits 
            BlockContainerElement,
            InlineContainerElement,
            SemanticBlockContainerElement,
            SemanticInlineContainerElement

{
    
    // Ordinarily every entry here is a resolved Node_I<?>, exactly as before -- zero behaviour
    // change for the common case. The one exception is a DynamicFrag specifically (not any
    // Frags_I): it is kept UNRESOLVED so childNodes() re-evaluates it on every call, which is
    // what lets a tree built once still produce different children (e.g. table rows) on each
    // subsequent render. hasDynamicChild lets childNodes() skip the resolve pass entirely
    // when there is nothing dynamic to resolve.
    final List<Object> children = new ArrayList<>();
    private boolean hasDynamicChild = false;

    protected MutableContainerElement_A(String tagName) {
        super(tagName);
    }

    MutableContainerElement_A(Class<? extends SemanticElement_I> clss) {
        super(clss);
    }


    @Override
    @SuppressWarnings("unchecked")
    public final List<? extends Node_I<?>> childNodes() {
        if (!hasDynamicChild) {
            return (List<? extends Node_I<?>>)(List<?>) Collections.unmodifiableList(children);
        }
        var resolved = new ArrayList<Node_I<?>>(children.size());
        for (var entry : children) {
            if (entry instanceof DynamicFrag dynamicFrag) {
                appendResolved(resolved, dynamicFrag);
            } else {
                resolved.add((Node_I<?>) entry);
            }
        }
        return Collections.unmodifiableList(resolved);
    }

    private static void appendResolved(List<Node_I<?>> out, Frags_I<?> frags) {
        for (var fragment : frags.fragments()) {
            switch (((Frag_I) fragment).fragType()) {
                case Attr_T a -> {} // attributes cannot appear as element content; ignore
                case Node_T n -> out.add(n.node());
                case Frags_T f -> appendResolved(out, f.frags());
            }
        }
    }

    @Override
    public final I addContent(Iterable<Frag_I<?>> fragments){
        for (var fragment : fragments) {
            addFragment(fragment);
        }
        return self();
    }
    
    @Override
    public final I addContent(Frag_I<?> ... fragments) {
        for (var fragment : fragments) {
            addFragment(fragment);
        }
        return self();
    }
    
    final I addFragment(Frag_I fragment){
        switch (fragment.fragType()) {
            case Attr_T  a -> attributes.put(a.attr().name(), a.attr().value());
            case Node_T  n -> children.add(n.node());
            case Frags_T f -> {
                if (f.frags() instanceof DynamicFrag dynamicFrag) {
                    hasDynamicChild = true;
                    children.add(dynamicFrag); // deferred - see childNodes()
                } else {
                    addContent(f.frags().__()); // ordinary fragment collection: flatten now, as before
                }
            }
        }
        return self();
    }
    
    @Override
    public final I addContent(String... textContent) {
        for (var text : textContent) {
            children.add(new InlineText(text));
        }
        return self();
    }
    
    public <X extends SemanticElement_I> Optional<X> findChild(Class<X> clzz){
        for (var c : childNodes()) {
            if( clzz.isInstance(c) ){
                return Optional.of((X)c);
            }
        }
        return  Optional.empty();
    }


    public <X extends SemanticElement_I> List<X> findChildren(Class<X> clzz){
        final var r = new LinkedList<X>();
        for (var c : childNodes()) {
            if(clzz.isInstance(c)){
                r.add((X)c);
            }
        }
        return r;
    }

    public <X extends SemanticElement_I> List<Element_I> find(Class<X> clzz){
        final var r = new LinkedList<Element_I>();
        var tgNm = luvml.element.SemanticElementTagNameClassNameMapping.classTagName(clzz);
        for (var c : childNodes()) {
            if(c instanceof Element_I e && tgNm.equals(e.tagName()) ){
                r.add(e);
            }
        }
        return r;
    }
    
}