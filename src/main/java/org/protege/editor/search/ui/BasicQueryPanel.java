package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.search.nci.QueryType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLProperty;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class BasicQueryPanel extends QueryPanel {
    private static final long serialVersionUID = 3201978298089022559L;
    private OwlEntityComboBox propertyComboBox;
    private JComboBox<QueryType> queryTypeComboBox;
    private JComboBox<Boolean> valueComboBox;
    private JTextField valueTextField;
    private JComponent value;
    private JLabel valueLbl;
    private OwlEntityComboBoxChangeHandler comboBoxChangeHandler;

    /**
     * Constructor
     *
     * @param editorKit OWL Editor Kit
     */
    public BasicQueryPanel(OWLEditorKit editorKit) {
        super(editorKit);
        this.editorKit.getModelManager().addOntologyChangeListener(ontologyEditingListener);
        initUi();
    }

    private void initUi() {
        setLayout(new GridBagLayout());
        setBorder(new MatteBorder(1, 1, 1, 1, LuceneUiUtils.MATTE_BORDER_COLOR));

        JLabel propertyLbl = new JLabel("Property");
        JLabel queryTypeLbl = new JLabel("Query Type");
        valueLbl = new JLabel("Value");

        propertyComboBox = new OwlEntityComboBox(editorKit);
        propertyComboBox.addItemListener(itemListener);
        propertyComboBox.addItems(getProperties());
        comboBoxChangeHandler = new OwlEntityComboBoxChangeHandler(propertyComboBox);

        queryTypeComboBox = new JComboBox<>();
        queryTypeComboBox.setPrototypeDisplayValue(QueryType.PROPERTY_RESTRICTION_PRESENT);
        queryTypeComboBox.addActionListener(queryTypeComboBoxListener);

        valueTextField = new JTextField();
        valueComboBox = new JComboBox<>(new Boolean[]{true, false});
        value = valueTextField;

        Insets insets = new Insets(0, 5, 2, 5);
        int rowIndex = 0;
        add(getCloseButton(), new GridBagConstraints(3, rowIndex, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, insets, 0, 0));
        rowIndex++;
        add(propertyLbl, new GridBagConstraints(0, rowIndex, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        add(queryTypeLbl, new GridBagConstraints(1, rowIndex, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 9, 2, 5), 0, 0));
        add(valueLbl, new GridBagConstraints(2, rowIndex, 2, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, insets, 0, 0));
        rowIndex++;
        add(propertyComboBox, new GridBagConstraints(0, rowIndex, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        add(queryTypeComboBox, new GridBagConstraints(1, rowIndex, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        add(value, new GridBagConstraints(2, rowIndex, 2, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, insets, 0, 0));

        propertyComboBox.setSelectedItem(TabPreferences.getDefaultProperty(editorKit));
    }

    private OWLOntologyChangeListener ontologyEditingListener = changes -> {
        for (OWLOntologyChange change : changes) {
            change.accept(comboBoxChangeHandler);
        }
    };

    private ItemListener itemListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Object obj = e.getItem();
                if (obj != null && obj instanceof OWLEntity) {
                    OWLEntity selectedEntity = (OWLEntity) obj;
                    List<QueryType> queryTypes = QueryType.getTypesForOWLObject(selectedEntity);
                    queryTypeComboBox.setModel(new DefaultComboBoxModel(queryTypes.toArray()));
                    queryTypeComboBox.setSelectedItem(TabPreferences.getDefaultQueryType());
                    if (selectedEntity.isOWLObjectProperty()) {
                        value.setVisible(false);
                        valueLbl.setVisible(false);
                    } else if (!value.isVisible()) {
                        value.setVisible(true);
                        valueLbl.setVisible(true);
                    }
                } else if (obj instanceof String) {
                    ((JTextField)propertyComboBox.getEditor().getEditorComponent()).setText(obj.toString());
                }
            }
        }
    };

    private ActionListener queryTypeComboBoxListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            QueryType queryType = (QueryType) queryTypeComboBox.getSelectedItem();
            if(queryType.isNonValueType()) {
                value.setVisible(false);
                valueLbl.setVisible(false);
            } else if(queryType.isBooleanType()) {
                value = valueComboBox;
                if(!value.isVisible()) {
                    value.setVisible(true);
                    valueLbl.setVisible(true);
                }
            } else if(queryType.isValueType()) {
                if(!value.isVisible()) {
                    value.setVisible(true);
                }
                if(!valueLbl.isVisible()) {
                    valueLbl.setVisible(true);
                }
            }
        }
    };

    public OWLProperty getSelectedProperty() {
        return (OWLProperty) propertyComboBox.getSelectedItem();
    }

    public QueryType getSelectedQueryType() {
        return (QueryType) queryTypeComboBox.getSelectedItem();
    }

    public String getInputStringValue() {
        if(value instanceof JTextField) {
            return ((JTextField)value).getText();
        } else if(value instanceof JComboBox) {
            return ((JComboBox) value).getSelectedItem().toString();
        }
        return "";
    }

    @Override
    public boolean isBasicQuery() {
        return true;
    }

    @Override
    public boolean isNegatedQuery() {
        return false;
    }

    @Override
    public boolean isNestedQuery() {
        return false;
    }

    @Override
    public void dispose() {
        propertyComboBox.removeItemListener(itemListener);
        queryTypeComboBox.removeActionListener(queryTypeComboBoxListener);
        editorKit.getModelManager().removeOntologyChangeListener(ontologyEditingListener);
        closeBtn.removeActionListener(closeBtnListener);
    }
}
