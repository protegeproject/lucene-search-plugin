package org.protege.editor.search.ui;

import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.core.ui.util.VerifiedInputEditor;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.search.lucene.LuceneSearchPreferences;
import org.protege.editor.search.nci.QueryType;
import org.semanticweb.owlapi.model.OWLProperty;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class TabPreferencesDialogPanel extends JPanel implements VerifiedInputEditor {
    private static final long serialVersionUID = 7398389479210568073L;
    private List<InputVerificationStatusChangedListener> listeners = new ArrayList<>();
    private JLabel propertyLbl, queryTypeLbl, maxResultsLbl;
    private OwlEntityComboBox propertyComboBox;
    private JComboBox<QueryType> queryTypes;
    private JFormattedTextField maxResultsField;
    private JSpinner maxResults;
    private OWLEditorKit editorKit;
    private boolean currentlyValid;

    /**
     * Constructor
     *
     * @param editorKit OWL Editor Kit
     */
    public TabPreferencesDialogPanel(OWLEditorKit editorKit) {
        this.editorKit = checkNotNull(editorKit);
        initUi();
    }

    private void initUi() {
        setLayout(new GridBagLayout());

        propertyLbl = new JLabel("Default OWL Property");
        queryTypeLbl = new JLabel("Default Query Type");
        maxResultsLbl = new JLabel("Maximum results per page");

        propertyComboBox = new OwlEntityComboBox(editorKit);
        propertyComboBox.addItems(LuceneUiUtils.getProperties(editorKit));

        queryTypes = new JComboBox<>();
        for(QueryType qt : QueryType.QUERY_TYPES) {
            queryTypes.addItem(qt);
        }

        maxResults = new JSpinner();
        maxResultsField = ((JSpinner.NumberEditor) maxResults.getEditor()).getTextField();
        maxResults.setValue(LuceneSearchPreferences.getMaxSizeForInMemoryIndexStoring());
        maxResultsField.getDocument().addDocumentListener(maxResultsFieldListener);
        ((NumberFormatter) maxResultsField.getFormatter()).setAllowsInvalid(false);

        Insets first = new Insets(5, 0, 2, 0);
        Insets second = new Insets(2, 0, 10, 0);

        add(maxResultsLbl, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, first, 0, 0));
        add(maxResults, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, second, 0, 0));

        add(propertyLbl, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, first, 0, 0));
        add(propertyComboBox, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, second, 0, 0));

        add(queryTypeLbl, new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, first, 0, 0));
        add(queryTypes, new GridBagConstraints(0, 5, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, second, 0, 0));

        setDefaultValues();
    }

    private DocumentListener maxResultsFieldListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            checkInputs();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            checkInputs();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            checkInputs();
        }
    };

    private void setDefaultValues() {
        maxResults.setValue(TabPreferences.getMaximumResultsPerPage());
        OWLProperty defaultProperty = TabPreferences.getDefaultProperty(editorKit);
        propertyComboBox.setSelectedItem(defaultProperty);
        queryTypes.setSelectedItem(TabPreferences.getDefaultQueryType());
    }

    private void updatePreferences() {
        OWLProperty prop = (OWLProperty) propertyComboBox.getSelectedItem();
        TabPreferences.setDefaultProperty(prop.getIRI());

        QueryType qt = (QueryType) queryTypes.getSelectedItem();
        TabPreferences.setDefaultQueryType(qt);

        TabPreferences.setMaximumResultsPerPage(((SpinnerNumberModel) maxResults.getModel()).getNumber().intValue());
    }

    public static void showDialog(OWLEditorKit editorKit) {
        TabPreferencesDialogPanel panel = new TabPreferencesDialogPanel(editorKit);
        int response = JOptionPaneEx.showValidatingConfirmDialog(
                editorKit.getOWLWorkspace(), "Configure Lucene Query tab", panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
        if (response == JOptionPane.OK_OPTION) {
            panel.updatePreferences();
        }
    }

    private void checkInputs() {
        boolean allValid = true;
        if (maxResults.getValue() == null) {
            allValid = false;
        }
        try {
            if (Integer.parseInt(maxResultsField.getText()) < 1) {
                allValid = false;
            }
        } catch(NumberFormatException e) {
            /* do nothing */
        }
        setValid(allValid);
    }

    private void setValid(boolean valid) {
        currentlyValid = valid;
        for (InputVerificationStatusChangedListener l : listeners) {
            l.verifiedStatusChanged(currentlyValid);
        }
    }

    @Override
    public void addStatusChangedListener(InputVerificationStatusChangedListener listener) {
        listeners.add(listener);
        listener.verifiedStatusChanged(currentlyValid);
    }

    @Override
    public void removeStatusChangedListener(InputVerificationStatusChangedListener listener) {
        listeners.remove(listener);
    }
}
