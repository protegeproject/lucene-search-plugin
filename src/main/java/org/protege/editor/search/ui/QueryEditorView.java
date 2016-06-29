package org.protege.editor.search.ui;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

import java.awt.*;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class QueryEditorView extends AbstractOWLViewComponent {
    private static final long serialVersionUID = -470939888306548492L;
    private QueryEditorPanel editorPanel;

    @Override
    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout());
        setBorder(LuceneUiHelper.Utils.MATTE_BORDER);
        editorPanel = new QueryEditorPanel(getOWLEditorKit());
        add(editorPanel, BorderLayout.CENTER);
    }

    @Override
    protected void disposeOWLView() {
        editorPanel.dispose();
    }
}
