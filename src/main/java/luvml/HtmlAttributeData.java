package luvml;

import java.util.*;
import static java.util.Collections.EMPTY_SET;

import static java.util.Set.of;
import static luvml.AttributeCategory.*;
import static luvml.AttributeType.*;
import static luvml.AttributeScope.*;

public record HtmlAttributeData(
    String attribute,
    Set<AttributeCategory> categories,
    AttributeType type,
    AttributeScope scope,
    String description,
    Set<String> enumValues  // For ENUM type attributes
) {
    
    private static Set<AttributeCategory> categories(AttributeCategory... categories) {
        if(categories==null || categories.length<1)return EMPTY_SET;
        return of(categories);
    }
    
    
    
    private static Set<String> enumValues(String... values) {
        return values.length == 0 ? Set.of() : of(values);
    }
    
    private static HtmlAttributeData attribute(String name, AttributeType type, AttributeScope scope,
                                             Set<AttributeCategory> categories, String description) {
        return new HtmlAttributeData(name, categories, type, scope, description, Set.of());
    }
    
    private static HtmlAttributeData enumAttribute(String name, AttributeScope scope,
                                                 Set<AttributeCategory> categories, String description,
                                                 String... enumValues) {
        return new HtmlAttributeData(name, categories, ENUM, scope, description, of(enumValues));
    }
    
    // List containing ALL attributes including those with duplicate names (e.g., multiple "type" attributes)
    public static final List<HtmlAttributeData> ALL_ATTRIBUTES_LIST = allAttributesList();

    // Map for convenient lookup by name (may lose duplicates - use ALL_ATTRIBUTES_LIST for complete data)
    public static final Map<String, HtmlAttributeData> ALL_ATTRIBUTES = allattributes();

    private static final List<HtmlAttributeData> allAttributesList(){
        var list = new ArrayList<HtmlAttributeData>();
        // List preserves ALL entries including duplicates

        list.add(
        // Global attributes
        attribute("accesskey", STRING, UNIVERSAL, categories(GLOBAL), "Keyboard shortcut for element"));
        list.add(attribute("class", TOKEN_LIST, UNIVERSAL, categories(GLOBAL), "CSS class names"));
        list.add(enumAttribute("contenteditable", UNIVERSAL, categories(GLOBAL), "Whether content is editable", "true", "false", "plaintext-only"));
        list.add(enumAttribute("dir", UNIVERSAL, categories(GLOBAL), "Text directionality", "ltr", "rtl", "auto"));
        list.add(attribute("draggable", BOOLEAN, UNIVERSAL, categories(GLOBAL), "Whether element is draggable"));
        list.add(attribute("hidden", BOOLEAN, UNIVERSAL, categories(GLOBAL), "Whether element is hidden"));
        list.add(attribute("id", STRING, UNIVERSAL, categories(GLOBAL), "Unique identifier"));
        list.add(attribute("lang", LANGUAGE, UNIVERSAL, categories(GLOBAL), "Language of element content"));
        list.add(attribute("spellcheck", BOOLEAN, UNIVERSAL, categories(GLOBAL), "Whether to check spelling"));
        list.add(attribute("style", STRING, UNIVERSAL, categories(GLOBAL), "Inline CSS styles"));
        list.add(attribute("tabindex", NUMBER, UNIVERSAL, categories(GLOBAL), "Tab order for keyboard navigation"));
        list.add(attribute("title", STRING, UNIVERSAL, categories(GLOBAL), "Advisory information about element"));
        list.add(enumAttribute("translate", UNIVERSAL, categories(GLOBAL), "Whether content should be translated", "yes", "no"));

        // Data attributes (template)
        list.add(attribute("data-*", STRING, UNIVERSAL, categories(GLOBAL, DATA), "Custom data attributes"));

        // Event handler attributes (common ones)
        list.add(attribute("onclick", SCRIPT, UNIVERSAL, categories(GLOBAL, EVENT), "Click event handler"));
        list.add(attribute("onload", SCRIPT, UNIVERSAL, categories(GLOBAL, EVENT), "Load event handler"));
        list.add(attribute("onchange", SCRIPT, UNIVERSAL, categories(GLOBAL, EVENT), "Change event handler"));
        list.add(attribute("onsubmit", SCRIPT, UNIVERSAL, categories(GLOBAL, EVENT), "Submit event handler"));
        list.add(attribute("onfocus", SCRIPT, UNIVERSAL, categories(GLOBAL, EVENT), "Focus event handler"));
        list.add(attribute("onblur", SCRIPT, UNIVERSAL, categories(GLOBAL, EVENT), "Blur event handler"));

        // ARIA attributes (selection)
        list.add(attribute("aria-label", STRING, UNIVERSAL, categories(ACCESSIBILITY), "Accessible name for element"));
        list.add(attribute("aria-labelledby", TOKEN_LIST, UNIVERSAL, categories(ACCESSIBILITY), "IDs of elements that label this element"));
        list.add(attribute("aria-describedby", TOKEN_LIST, UNIVERSAL, categories(ACCESSIBILITY), "IDs of elements that describe this element"));
        list.add(attribute("aria-hidden", BOOLEAN, UNIVERSAL, categories(ACCESSIBILITY), "Whether element is hidden from assistive technology"));
        list.add(enumAttribute("aria-expanded", UNIVERSAL, categories(ACCESSIBILITY), "Whether collapsible element is expanded", "true", "false", "undefined"));

        // Form attributes
        list.add(attribute("accept", COMMA_LIST, FORM_ELEMENTS, categories(FORM), "File types the server accepts"));
        list.add(attribute("accept-charset", TOKEN_LIST, FORM_ELEMENTS, categories(FORM), "Character encodings for form submission"));
        list.add(attribute("action", URL, FORM_ELEMENTS, categories(FORM), "URL for form submission"));
        list.add(enumAttribute("autocomplete", FORM_ELEMENTS, categories(FORM), "Whether form control should have autocomplete", "on", "off", "name", "email", "username", "current-password", "new-password"));
        list.add(attribute("autofocus", BOOLEAN, FORM_ELEMENTS, categories(FORM), "Whether element should be focused on page load"));
        list.add(attribute("checked", BOOLEAN, FORM_ELEMENTS, categories(FORM), "Whether input is checked"));
        list.add(attribute("disabled", BOOLEAN, FORM_ELEMENTS, categories(FORM), "Whether form control is disabled"));
        list.add(enumAttribute("enctype", FORM_ELEMENTS, categories(FORM), "Encoding type for form submission", "application/x-www-form-urlencoded", "multipart/form-data", "text/plain"));
        list.add(attribute("for", STRING, FORM_ELEMENTS, categories(FORM), "ID of form control this label is for"));
        list.add(attribute("form", STRING, FORM_ELEMENTS, categories(FORM), "ID of form this element belongs to"));
        list.add(attribute("formaction", URL, FORM_ELEMENTS, categories(FORM), "URL for form submission (overrides form action)"));
        list.add(enumAttribute("formenctype", FORM_ELEMENTS, categories(FORM), "Encoding type (overrides form enctype)", "application/x-www-form-urlencoded", "multipart/form-data", "text/plain"));
        list.add(enumAttribute("formmethod", FORM_ELEMENTS, categories(FORM), "HTTP method (overrides form method)", "get", "post"));
        list.add(attribute("formnovalidate", BOOLEAN, FORM_ELEMENTS, categories(FORM), "Skip form validation on submission"));
        list.add(attribute("formtarget", STRING, FORM_ELEMENTS, categories(FORM), "Target for form submission (overrides form target)"));
        list.add(attribute("max", STRING, FORM_ELEMENTS, categories(FORM), "Maximum value for input"));
        list.add(attribute("maxlength", NUMBER, FORM_ELEMENTS, categories(FORM), "Maximum number of characters"));
        list.add(enumAttribute("method", FORM_ELEMENTS, categories(FORM), "HTTP method for form submission", "get", "post"));
        list.add(attribute("min", STRING, FORM_ELEMENTS, categories(FORM), "Minimum value for input"));
        list.add(attribute("minlength", NUMBER, FORM_ELEMENTS, categories(FORM), "Minimum number of characters"));
        list.add(attribute("multiple", BOOLEAN, FORM_ELEMENTS, categories(FORM), "Whether multiple values are allowed"));
        list.add(attribute("name", STRING, FORM_ELEMENTS, categories(FORM), "Name of form control"));
        list.add(attribute("novalidate", BOOLEAN, FORM_ELEMENTS, categories(FORM), "Skip form validation"));
        list.add(attribute("pattern", REGEX, FORM_ELEMENTS, categories(FORM), "Regular expression for input validation"));
        list.add(attribute("placeholder", STRING, FORM_ELEMENTS, categories(FORM), "Placeholder text for input"));
        list.add(attribute("readonly", BOOLEAN, FORM_ELEMENTS, categories(FORM), "Whether form control is read-only"));
        list.add(attribute("required", BOOLEAN, FORM_ELEMENTS, categories(FORM), "Whether form control is required"));
        list.add(attribute("selected", BOOLEAN, FORM_ELEMENTS, categories(FORM), "Whether option is selected"));
        list.add(attribute("size", NUMBER, FORM_ELEMENTS, categories(FORM), "Size of form control"));
        list.add(attribute("step", STRING, FORM_ELEMENTS, categories(FORM), "Step value for numeric inputs"));
        list.add(enumAttribute("target", FORM_ELEMENTS, categories(FORM), "Target for form submission", "_blank", "_self", "_parent", "_top"));
        list.add(enumAttribute("type", FORM_ELEMENTS, categories(FORM), "Type of input control", 
                "text", "password", "email", "url", "tel", "search", "number", "range", "date", "time", "datetime-local", 
                "month", "week", "color", "file", "hidden", "checkbox", "radio", "submit", "reset", "button", "image"));
        list.add(attribute("value", STRING, FORM_ELEMENTS, categories(FORM), "Value of form control"));

        // Media attributes
        list.add(attribute("alt", STRING, MEDIA_ELEMENTS, categories(MEDIA), "Alternative text for image"));
        list.add(attribute("autoplay", BOOLEAN, MEDIA_ELEMENTS, categories(MEDIA), "Whether media should autoplay"));
        list.add(attribute("controls", BOOLEAN, MEDIA_ELEMENTS, categories(MEDIA), "Whether media controls should be shown"));
        list.add(enumAttribute("crossorigin", MEDIA_ELEMENTS, categories(MEDIA), "CORS settings for media", "anonymous", "use-credentials"));
        list.add(attribute("height", DIMENSION, MEDIA_ELEMENTS, categories(MEDIA), "Height of media element"));
        list.add(attribute("loop", BOOLEAN, MEDIA_ELEMENTS, categories(MEDIA), "Whether media should loop"));
        list.add(attribute("muted", BOOLEAN, MEDIA_ELEMENTS, categories(MEDIA), "Whether media should be muted"));
        list.add(enumAttribute("preload", MEDIA_ELEMENTS, categories(MEDIA), "How media should be preloaded", "none", "metadata", "auto"));
        list.add(attribute("poster", URL, MEDIA_ELEMENTS, categories(MEDIA), "Poster image for video"));
        list.add(attribute("src", URL, MEDIA_ELEMENTS, categories(MEDIA), "Source URL for media"));
        list.add(attribute("srcset", STRING, MEDIA_ELEMENTS, categories(MEDIA), "Set of source images with descriptors"));
        list.add(attribute("width", DIMENSION, MEDIA_ELEMENTS, categories(MEDIA), "Width of media element"));

        // Link attributes
        list.add(attribute("download", STRING, LINK_ELEMENTS, categories(LINK), "Filename for download"));
        list.add(attribute("href", URL, LINK_ELEMENTS, categories(LINK), "Hyperlink reference"));
        list.add(attribute("hreflang", LANGUAGE, LINK_ELEMENTS, categories(LINK), "Language of linked resource"));
        list.add(attribute("ping", TOKEN_LIST, LINK_ELEMENTS, categories(LINK), "URLs to ping when link is followed"));
        list.add(enumAttribute("referrerpolicy", LINK_ELEMENTS, categories(LINK), "Referrer policy for link", 
                "no-referrer", "no-referrer-when-downgrade", "origin", "origin-when-cross-origin", "same-origin", "strict-origin", "strict-origin-when-cross-origin", "unsafe-url"));
        list.add(enumAttribute("rel", LINK_ELEMENTS, categories(LINK), "Relationship to linked resource",
                "alternate", "author", "bookmark", "canonical", "dns-prefetch", "external", "help", "icon", "license", "manifest", "next", "nofollow", "noopener", "noreferrer", "opener", "prev", "preconnect", "prefetch", "preload", "prerender", "search", "stylesheet", "tag"));
        list.add(attribute("sizes", TOKEN_LIST, LINK_ELEMENTS, categories(LINK), "Sizes of linked resource (for icons)"));
        list.add(enumAttribute("type", LINK_ELEMENTS, categories(LINK), "MIME type of linked resource", "text/css", "text/javascript", "image/x-icon", "application/rss+xml"));

        // Table attributes
        list.add(attribute("colspan", NUMBER, TABLE_ELEMENTS, categories(TABLE), "Number of columns cell spans"));
        list.add(attribute("rowspan", NUMBER, TABLE_ELEMENTS, categories(TABLE), "Number of rows cell spans"));
        list.add(attribute("headers", TOKEN_LIST, TABLE_ELEMENTS, categories(TABLE), "IDs of header cells for this cell"));
        list.add(enumAttribute("scope", TABLE_ELEMENTS, categories(TABLE), "Scope of header cell", "row", "col", "rowgroup", "colgroup"));
        list.add(attribute("span", NUMBER, TABLE_ELEMENTS, categories(TABLE), "Number of columns in column group"));

        // Interactive attributes
        list.add(attribute("open", BOOLEAN, INTERACTIVE_ELEMENTS, categories(INTERACTIVE), "Whether details element is open"));

        // Metadata attributes
        list.add(attribute("charset", CHARSET, METADATA_ELEMENTS, categories(METADATA), "Character encoding"));
        list.add(attribute("content", STRING, METADATA_ELEMENTS, categories(METADATA), "Value of meta element"));
        list.add(attribute("http-equiv", STRING, METADATA_ELEMENTS, categories(METADATA), "HTTP header name"));
        list.add(attribute("media", STRING, METADATA_ELEMENTS, categories(METADATA), "Media query for linked resource"));

        // Microdata attributes
        list.add(attribute("itemid", URL, UNIVERSAL, categories(MICRODATA), "Global identifier for microdata item"));
        list.add(attribute("itemprop", TOKEN_LIST, UNIVERSAL, categories(MICRODATA), "Microdata property names"));
        list.add(attribute("itemref", TOKEN_LIST, UNIVERSAL, categories(MICRODATA), "IDs of additional microdata properties"));
        list.add(attribute("itemscope", BOOLEAN, UNIVERSAL, categories(MICRODATA), "Whether element is microdata item"));
        list.add(attribute("itemtype", URL, UNIVERSAL, categories(MICRODATA), "Microdata vocabulary URL"));

        // Specific element attributes
        list.add(attribute("coords", COMMA_LIST, SPECIFIC_ELEMENTS, categories(null), "Coordinates for area element"));
        list.add(enumAttribute("shape", SPECIFIC_ELEMENTS, categories(null), "Shape of area element", "rect", "circle", "poly", "default"));
        list.add(attribute("usemap", STRING, SPECIFIC_ELEMENTS, categories(null), "Name of image map to use"));
        list.add(enumAttribute("wrap", SPECIFIC_ELEMENTS, categories(null), "How text should wrap in textarea", "soft", "hard"));
        list.add(attribute("rows", NUMBER, SPECIFIC_ELEMENTS, categories(null), "Number of rows in textarea"));
        list.add(attribute("cols", NUMBER, SPECIFIC_ELEMENTS, categories(null), "Number of columns in textarea"));
        list.add(attribute("start", NUMBER, SPECIFIC_ELEMENTS, categories(null), "Starting number for ordered list"));
        list.add(attribute("reversed", BOOLEAN, SPECIFIC_ELEMENTS, categories(null), "Whether ordered list is reversed"));
        list.add(enumAttribute("kind", SPECIFIC_ELEMENTS, categories(null), "Kind of text track", "subtitles", "captions", "descriptions", "chapters", "metadata"));
        list.add(attribute("srclang", LANGUAGE, SPECIFIC_ELEMENTS, categories(null), "Language of text track"));
        list.add(attribute("label", STRING, SPECIFIC_ELEMENTS, categories(null), "User-readable title for text track"));
        list.add(attribute("default", BOOLEAN, SPECIFIC_ELEMENTS, categories(null), "Whether track should be enabled by default"));

        // Deprecated attributes (common ones still encountered)
        list.add(attribute("align", STRING, SPECIFIC_ELEMENTS, categories(DEPRECATED), "Alignment (deprecated, use CSS)"));
        list.add(attribute("bgcolor", COLOR, SPECIFIC_ELEMENTS, categories(DEPRECATED), "Background color (deprecated, use CSS)"));
        list.add(attribute("border", NUMBER, SPECIFIC_ELEMENTS, categories(DEPRECATED), "Border width (deprecated, use CSS)"));
        list.add(attribute("cellpadding", NUMBER, SPECIFIC_ELEMENTS, categories(DEPRECATED), "Cell padding (deprecated, use CSS)"));
        list.add(attribute("cellspacing", NUMBER, SPECIFIC_ELEMENTS, categories(DEPRECATED), "Cell spacing (deprecated, use CSS)"));
        list.add(attribute("color", COLOR, SPECIFIC_ELEMENTS, categories(DEPRECATED), "Text color (deprecated, use CSS)"));
        list.add(attribute("face", STRING, SPECIFIC_ELEMENTS, categories(DEPRECATED), "Font face (deprecated, use CSS)"));
        list.add(attribute("size", NUMBER, SPECIFIC_ELEMENTS, categories(DEPRECATED), "Font size (deprecated, use CSS)"));

        return Collections.unmodifiableList(list);
    }

    private static final Map<String, HtmlAttributeData> allattributes(){
        var map = new LinkedHashMap<String, HtmlAttributeData>();
        // Build map from list - note: duplicate keys will be overwritten with last occurrence
        for (var attr : ALL_ATTRIBUTES_LIST) {
            map.put(attr.attribute(), attr);
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Get attribute data by attribute name.
     */
    public static HtmlAttributeData get(String attributeName) {
        return ALL_ATTRIBUTES.get(attributeName);
    }
}