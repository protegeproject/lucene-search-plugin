package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public abstract class QueryPanel extends JPanel {
    protected OWLEditorKit editorKit;

    /**
     * Constructor
     *
     * @param editorKit OWL Editor Kit
     */
    public QueryPanel(OWLEditorKit editorKit) {
        this.editorKit = checkNotNull(editorKit);
    }

    abstract boolean isBasicQuery();

    abstract boolean isNegatedQuery();

    abstract boolean isNestedQuery();

}
