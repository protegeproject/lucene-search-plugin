package org.protege.editor.search.ui;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

import java.awt.*;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class CombinedLuceneQueryView extends AbstractOWLViewComponent {
    private static final long serialVersionUID = 5210865494288919388L;
    private CombinedLuceneQueryPanel panel;

    @Override
    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout());
        setBorder(LuceneUiHelper.Utils.EMPTY_BORDER);
        panel = new CombinedLuceneQueryPanel(getOWLEditorKit());
        add(panel, BorderLayout.CENTER);
    }

    @Override
    protected void disposeOWLView() {
        panel.dispose();
    }
}
