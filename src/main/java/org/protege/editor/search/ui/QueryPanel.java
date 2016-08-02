package org.protege.editor.search.ui;

import org.protege.editor.core.Disposable;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public abstract class QueryPanel extends JPanel implements Disposable {
    protected OWLEditorKit editorKit;
    protected JButton closeBtn;

    /**
     * Constructor
     *
     * @param editorKit OWL Editor Kit
     */
    public QueryPanel(OWLEditorKit editorKit) {
        this.editorKit = checkNotNull(editorKit);
    }

    protected abstract boolean isBasicQuery();

    protected abstract boolean isNegatedQuery();

    protected abstract boolean isNestedQuery();

    protected JButton getCloseButton() {
        closeBtn = new JButton(LuceneUiUtils.getIcon(LuceneUiUtils.CLOSE_ICON_FILENAME, 11, 11));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setOpaque(false);
        closeBtn.addActionListener(closeBtnListener);
        return closeBtn;
    }

    protected ActionListener closeBtnListener = e -> {
        dispose();
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
    };

    protected List<OWLEntity> getProperties() {
        return LuceneUiUtils.getProperties(editorKit);
    }

    public abstract void dispose();

}
