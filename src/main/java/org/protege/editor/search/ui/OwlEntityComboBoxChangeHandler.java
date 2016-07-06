package org.protege.editor.search.ui;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLOntologyChangeVisitorAdapter;

public class OwlEntityComboBoxChangeHandler extends OWLOntologyChangeVisitorAdapter {

    private OwlEntityComboBox comboBox;

    public OwlEntityComboBoxChangeHandler(OwlEntityComboBox comboBox) {
        this.comboBox = comboBox;
    }

    @Override
    public void visit(RemoveAxiom change) {
        OWLAxiom axiom = change.getAxiom();
        if (axiom instanceof OWLDeclarationAxiom) {
            OWLEntity entity = ((OWLDeclarationAxiom) axiom).getEntity();
            if (entity instanceof OWLProperty) {
                comboBox.removeItem(entity);
            }
        }
    }

    @Override
    public void visit(AddAxiom change) {
        OWLAxiom axiom = change.getAxiom();
        if (axiom instanceof OWLDeclarationAxiom) {
            OWLEntity entity = ((OWLDeclarationAxiom) axiom).getEntity();
            if (entity instanceof OWLProperty) {
                comboBox.addItem(entity);
            }
        }
    }
}
