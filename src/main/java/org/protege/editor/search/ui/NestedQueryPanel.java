package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLProperty;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class NestedQueryPanel extends QueryPanel {
    private static final long serialVersionUID = 7560360869250442419L;
    private static final String NAME = "Nested Query";
    private OwlEntityComboBox propertyComboBox;
    private QueryEditorPanel editorPanel;

    /**
     * Constructor
     *
     * @param editorKit OWL Editor Kit
     */
    public NestedQueryPanel(OWLEditorKit editorKit) {
        super(editorKit);
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());
        setBorder(LuceneUiUtils.EMPTY_BORDER);

        JPanel parent = new JPanel(new GridLayout(0, 1));
        parent.setBorder(LuceneUiUtils.EMPTY_BORDER);

        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(new MatteBorder(1, 1, 1, 1, LuceneUiUtils.MATTE_BORDER_COLOR));

        JLabel title = new JLabel("<html><b>" + NAME + "</b></html>");
        title.setForeground(new Color(8, 84, 170));
        topPanel.add(title, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(3, 5, 0, 5), 0, 0));
        topPanel.add(getCloseButton(), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        parent.add(topPanel);
        parent.add(getPropertySelectionPanel());
        add(parent, BorderLayout.NORTH);

        editorPanel = new QueryEditorPanel(editorKit, false, true);
        add(editorPanel, BorderLayout.CENTER);
    }

    private JPanel getPropertySelectionPanel() {
        propertyComboBox = new OwlEntityComboBox(editorKit);
        propertyComboBox.addItems(getProperties());
        JLabel propSelectionLbl = new JLabel("Property");
        JPanel propSelectionPanel = new JPanel(new GridBagLayout());
        propSelectionPanel.add(propSelectionLbl, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, new Insets(13, 11, 6, 3), 0, 0));
        propSelectionPanel.add(propertyComboBox, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(13, 3, 6, 8), 0, 0));
        propSelectionPanel.setBorder(new MatteBorder(0, 1, 0, 1, LuceneUiUtils.MATTE_BORDER_COLOR));
        return propSelectionPanel;
    }

    @Override
    public boolean isBasicQuery() {
        return false;
    }

    @Override
    public boolean isNegatedQuery() {
        return false;
    }

    @Override
    public boolean isNestedQuery() {
        return true;
    }

    public OWLProperty getSelectedProperty() {
        return (OWLProperty) propertyComboBox.getSelectedItem();
    }

    public QueryEditorPanel getQueryEditorPanel() {
        return editorPanel;
    }

    @Override
    public void dispose() {
        closeBtn.removeActionListener(closeBtnListener);
        editorPanel.dispose();
    }
}
