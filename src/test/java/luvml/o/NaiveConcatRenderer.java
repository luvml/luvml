package luvml.o;

import luvx.Doctype_I;
import luvx.Element_I;
import luvx.Frag_I;
import luvx.Node_I;
import luvx.ftype.*;

import java.util.Map;

// Deliberately naive baseline for the streaming-vs-string-concatenation comparison
// (see luvml/prp/03-prp-streaming_instead_of_string_concatenate.md): every level
// returns a freshly built String and the caller concatenates it in, exactly the
// pattern the streaming HtmlRenderer replaced. Mirrors HtmlRenderer's traversal
// and escaping rules so the two produce identical output for the same tree.
public class NaiveConcatRenderer {

    public static String render(Frag_I<?> frag) {
        return switch (frag.fragType()) {
            case Attr_T a -> "";
            case Node_T n -> renderNode(n.node());
            case Frags_T f -> {
                var result = "";
                for (var fragment : f.frags().fragments()) {
                    result += render((Frag_I) fragment);
                }
                yield result;
            }
        };
    }

    private static String renderNode(Node_I<?> node) {
        return switch (node.nodeType()) {
            case Element_T e -> renderElement(e.element());
            case AttributelessNode_T an -> renderAttributelessNode(an);
        };
    }

    private static String renderElement(Element_I<?> element) {
        var openTag = element instanceof luvx.ProcessingInstruction_I<?>
            ? "<?" + element.tagName()
            : "<" + element.tagName();

        if (!(element instanceof luvx.ProcessingInstruction_I<?>)) {
            Map<String, String> attributes = element.attributes();
            for (Map.Entry<String, String> attr : attributes.entrySet()) {
                openTag += " " + attr.getKey() + "=\"" + escapeAttributeValue(attr.getValue()) + "\"";
            }
        }

        return switch (element.elementType()) {
            case SelfClosingElement_T sc -> switch (sc.selfClosingElementType()) {
                case VoidElement_T ve -> openTag + " />";
                case ProcessingInstruction_T pi -> openTag + " " + pi.processingInstruction().data() + "?>";
            };
            case RawTextElement_T rt ->
                openTag + ">" + rt.rawTextElement().rawTextContent() + "</" + element.tagName() + ">";
            case EscapableRawTextElement_T ert ->
                openTag + ">" + escapeTextContent(ert.escapableRawTextElement().escapableTextContent()) + "</" + element.tagName() + ">";
            case ContainerElement_T c -> {
                var childrenHtml = "";
                if (c.containerElement().hasChildNodes()) {
                    for (var child : c.containerElement().childNodes()) {
                        childrenHtml += renderNode((Node_I<?>) child);
                    }
                }
                yield openTag + ">" + childrenHtml + "</" + element.tagName() + ">";
            }
        };
    }

    private static String renderAttributelessNode(AttributelessNode_T an) {
        return switch (an.attributelessNodeType()) {
            case StringNode_T s -> switch (s.stringNodeType()) {
                case Text_T t -> escapeTextContent(t.text().textContent());
                case Comment_T c -> "<!--" + c.comment().textContent() + "-->";
                case CData_T cd -> "<![CDATA[" + cd.cdata().textContent() + "]]>";
            };
            case Doctype_T d -> renderDoctype(d.doctype());
        };
    }

    private static String renderDoctype(Doctype_I<?> doctype) {
        var result = "<!DOCTYPE " + doctype.name();
        if (doctype.publicId() != null) {
            result += " PUBLIC \"" + doctype.publicId() + "\" \"" + doctype.systemId() + "\">";
        } else if (doctype.systemId() != null) {
            result += " SYSTEM \"" + doctype.systemId() + "\">";
        } else {
            result += ">";
        }
        return result;
    }

    private static String escapeTextContent(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String escapeAttributeValue(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
