package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class OwlEntityComboBoxEditor extends BasicComboBoxEditor {
    private final OWLEditorKit editorKit;

    /**
     * No-arguments Constructor
     */
    public OwlEntityComboBoxEditor(OWLEditorKit editorKit) {
        super();
        this.editorKit = checkNotNull(editorKit);
    }

    @Override
    public Component getEditorComponent() {
        return super.getEditorComponent();
    }

    @Override
    public void setItem(Object object) {
        if(object != null && object instanceof OWLEntity) {
            super.setItem(editorKit.getOWLModelManager().getRendering((OWLEntity) object));
        } else {
            super.setItem("");
        }
    }

    @Override
    public Object getItem() {
        return super.getItem();
    }
}
