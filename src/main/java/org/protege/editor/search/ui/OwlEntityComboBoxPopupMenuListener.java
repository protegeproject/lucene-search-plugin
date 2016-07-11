package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class OwlEntityComboBoxPopupMenuListener implements PopupMenuListener {
    private OWLEditorKit editorKit;

    /**
     * Constructor
     *
     * @param editorKit OWL Editor Kit
     */
    public OwlEntityComboBoxPopupMenuListener(OWLEditorKit editorKit) {
        super();
        this.editorKit = checkNotNull(editorKit);
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
        /* no-op */
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        /* no-op */
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        JComboBox box = (JComboBox) e.getSource();
        Object comp = box.getUI().getAccessibleChild(box, 0);
        if (!(comp instanceof JPopupMenu)) {
            return;
        }
        JPopupMenu popupMenu = (JPopupMenu) comp;
        if (popupMenu.getComponent(0) instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) popupMenu.getComponent(0);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            if (popupMenu instanceof ComboPopup) {
                ComboPopup popup = (ComboPopup) popupMenu;
                JList list = popup.getList();
                Dimension size = list.getPreferredSize();
                int widestEntity = getWidestEntityStringRendering(box.getFontMetrics(box.getFont()));
                int boxwidth = box.getSize().width - 10;
                size.width = Math.max(box.getPreferredSize().width - 10, (widestEntity > boxwidth ? widestEntity : boxwidth));
                size.height = Math.min(scrollPane.getPreferredSize().height, size.height);
                scrollPane.setPreferredSize(size);
                scrollPane.setMaximumSize(size);
            }
        }
    }

    private int getWidestEntityStringRendering(FontMetrics fontMetrics) {
        List<OWLEntity> entities = new ArrayList<>();
        OWLOntology ont = editorKit.getModelManager().getActiveOntology();
        entities.addAll(ont.getAnnotationPropertiesInSignature());
        entities.addAll(ont.getObjectPropertiesInSignature());
        entities.addAll(ont.getDataPropertiesInSignature());
        int widest = 0;
        OWLModelManagerEntityRenderer renderer = editorKit.getModelManager().getOWLEntityRenderer();
        for(OWLEntity e : entities) {
            String str = renderer.render(e);
            int lineWidth = fontMetrics.stringWidth(str);
            widest = Math.max(widest, lineWidth);
        }
        return widest+50;
    }
}
