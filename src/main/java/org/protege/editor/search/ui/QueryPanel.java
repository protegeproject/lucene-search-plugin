package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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

    JButton getCloseButton() {
        JButton closeBtn = new JButton(LuceneUiUtils.getIcon(LuceneUiUtils.CLOSE_ICON_FILENAME, 11, 11));
        closeBtn.addActionListener(e -> {
            boolean removedPanel = false;
            JPanel queriesPanel = (JPanel) this.getParent();
            queriesPanel.remove(this);
            Container lastPanel = queriesPanel.getParent();
            while(lastPanel != null) {
                if(lastPanel instanceof QueryEditorPanel && !removedPanel) {
                    ((QueryEditorPanel) lastPanel).removeQueryPanel(this);
                    removedPanel = true;
                }
                lastPanel.revalidate();
                lastPanel.repaint();
                lastPanel = lastPanel.getParent();
            }
        });
        return closeBtn;
    }

    List<OWLEntity> getProperties() {
        return LuceneUiUtils.getProperties(editorKit);
    }
}
