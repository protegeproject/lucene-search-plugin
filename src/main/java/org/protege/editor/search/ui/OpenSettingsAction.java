package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;

import java.awt.event.ActionEvent;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class OpenSettingsAction extends ProtegeOWLAction {
    private OWLEditorKit editorKit;

    @Override
    public void initialise() throws Exception {
        this.editorKit = getOWLEditorKit();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TabPreferencesDialogPanel.showDialog(editorKit);
    }

    @Override
    public void dispose() throws Exception {
        /* do nothing */
    }
}
