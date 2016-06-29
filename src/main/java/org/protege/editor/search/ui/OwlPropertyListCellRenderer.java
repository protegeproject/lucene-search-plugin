package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.*;
import java.awt.*;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class OwlPropertyListCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = -7531384037366018820L;
    private OWLCellRenderer owlCellRenderer;

    public OwlPropertyListCellRenderer(OWLEditorKit editorKit) {
        owlCellRenderer = new OWLCellRenderer(editorKit);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if(value instanceof ExportDialogPanel.PropertyListItem) {
            OWLEntity entity = ((ExportDialogPanel.PropertyListItem) value).getProperty();
            label = owlCellRenderer.getListCellRendererComponent(list, entity, index, isSelected, cellHasFocus);
        }
        return label;
    }
}
