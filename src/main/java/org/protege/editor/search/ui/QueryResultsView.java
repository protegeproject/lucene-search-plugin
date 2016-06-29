package org.protege.editor.search.ui;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

import java.awt.*;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class QueryResultsView extends AbstractOWLViewComponent {
    private static final long serialVersionUID = -5585469048445371353L;
    private QueryResultsPanel resultsPanel;

    @Override
    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout());
        setBorder(LuceneUiHelper.Utils.MATTE_BORDER);
        resultsPanel = new QueryResultsPanel(getOWLEditorKit());
        add(resultsPanel, BorderLayout.CENTER);
    }

    @Override
    protected void disposeOWLView() {
        resultsPanel.dispose();
    }
}
