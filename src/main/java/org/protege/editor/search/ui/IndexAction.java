package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.protege.editor.search.nci.SearchTabManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class IndexAction extends ProtegeOWLAction {
    private OWLEditorKit editorKit;

    @Override
    public void initialise() throws Exception {
        this.editorKit = getOWLEditorKit();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SearchTabManager searchManager = getSearchManager();
        if (searchManager != null) {
            searchManager.rebuildIndex(editorKit.getOWLModelManager().getActiveOntology());
        } else {
            JOptionPane.showMessageDialog(editorKit.getOWLWorkspace(), new JLabel("Unable to index ontology. Ensure that" +
                            " 'Lucene search' is selected in the Protégé preferences (under the 'General' tab, in the 'Search type' option)."),
                    "Invalid search manager", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private SearchTabManager getSearchManager() {
        SearchTabManager searchManager = null;
        if (editorKit.getSearchManager() instanceof SearchTabManager) {
            searchManager = (SearchTabManager) editorKit.getSearchManager();
        }
        return searchManager;
    }

    @Override
    public void dispose() throws Exception {
        /* do nothing */
    }
}
