package org.protege.editor.search.ui;

import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class OwlEntityComboBoxEditor extends BasicComboBoxEditor {

    /**
     * No-arguments Constructor
     */
    public OwlEntityComboBoxEditor() {
        super();
    }

    @Override
    public Component getEditorComponent() {
        return super.getEditorComponent();
    }

    @Override
    public void setItem(Object object) {
        if(object != null && object instanceof OWLEntity) {
            super.setItem(((OWLEntity) object).getIRI().getShortForm());
        } else {
            super.setItem("");
        }
    }

    @Override
    public Object getItem() {
        return super.getItem();
    }
}
