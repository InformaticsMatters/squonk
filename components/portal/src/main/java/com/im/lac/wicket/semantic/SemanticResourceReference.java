package com.im.lac.wicket.semantic;

import org.apache.wicket.markup.head.*;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;

import java.util.Arrays;

/**
 * @author simetrias
 */
public class SemanticResourceReference extends JavaScriptResourceReference {

    private static final SemanticResourceReference instance = new SemanticResourceReference();

    private SemanticResourceReference() {
        super(SemanticResourceReference.class, "resources/semantic.min.js");
    }

    public static final SemanticResourceReference get() {
        return instance;
    }

    @Override
    public Iterable<? extends HeaderItem> getDependencies() {
        JavaScriptReferenceHeaderItem jquery = JavaScriptHeaderItem.forReference(JQueryResourceReference.get());
        CssReferenceHeaderItem style = CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/semantic.min.css"));
        return Arrays.asList(jquery, style);
    }

}
