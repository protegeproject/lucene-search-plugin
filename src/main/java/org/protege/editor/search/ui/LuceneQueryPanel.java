package org.protege.editor.search.ui;

import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class LuceneQueryPanel extends JPanel {
    private static final long serialVersionUID = 1998678969701208859L;
    private OWLEditorKit editorKit;
    private QueryResultsPanel resultsPanel;
    private QueryEditorPanel editorPanel;

    /**
     * Constructor
     *
     * @param editorKit OWL Editor Kit
     */
    public LuceneQueryPanel(OWLEditorKit editorKit) {
        this.editorKit = checkNotNull(editorKit);
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        editorPanel = new QueryEditorPanel(editorKit);
        resultsPanel = new QueryResultsPanel(editorKit);
        editorPanel.setBorder(LuceneUiHelper.Utils.MATTE_BORDER);
        resultsPanel.setBorder(LuceneUiHelper.Utils.MATTE_BORDER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, editorPanel, resultsPanel);
        splitPane.setPreferredSize(new Dimension(1300, 600));
        splitPane.setBorder(LuceneUiHelper.Utils.EMPTY_BORDER);
        add(splitPane, BorderLayout.CENTER);
        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);
    }

    public QueryResultsPanel getResultsPanel() {
        return resultsPanel;
    }

    public OWLEntity getSelectedEntity() {
        return resultsPanel.getSelectedEntity();
    }

    public static Optional<OWLEntity> showDialog(OWLEditorKit editorKit) {
        LuceneQueryPanel panel = new LuceneQueryPanel(editorKit);
        int response = JOptionPaneEx.showConfirmDialog(
                editorKit.getOWLWorkspace(), "Lucene Query Dialog", panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
        if (response == JOptionPane.OK_OPTION) {
            return Optional.ofNullable(panel.getSelectedEntity());
        } else {
            return Optional.empty();
        }
    }

    public void dispose() {
        resultsPanel.dispose();
        editorPanel.dispose();
    }
}
