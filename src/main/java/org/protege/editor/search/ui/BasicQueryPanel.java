package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.search.nci.QueryType;
import org.semanticweb.owlapi.model.OWLEntity;
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
    private static final long serialVersionUID = -2169622017720350105L;
    private OwlEntityComboBox propertyComboBox;
    private JComboBox<QueryType> queryTypeComboBox;
    private JComboBox<Boolean> valueComboBox;
    private JTextField valueTextField;
    private JComponent value;
    private JLabel valueLbl;

    /**
     * Constructor
     *
     * @param editorKit
     */
    public BasicQueryPanel(OWLEditorKit editorKit) {
        super(editorKit);
        initUi();
    }

    private void initUi() {
        setLayout(new GridBagLayout());
        setBorder(new MatteBorder(1, 1, 1, 1, LuceneUiHelper.Utils.MATTE_BORDER_COLOR));

        JLabel propertyLbl = new JLabel("Property");
        JLabel queryTypeLbl = new JLabel("Query Type");
        valueLbl = new JLabel("Value");

        List<OWLEntity> properties = getProperties();
        propertyComboBox = new OwlEntityComboBox(editorKit, properties);
        propertyComboBox.setPreferredSize(new Dimension(250, 20));
        propertyComboBox.addItemListener(itemListener);

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
    }

    private ItemListener itemListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Object obj = e.getItem();
                if (obj != null && obj instanceof OWLEntity) {
                    OWLEntity selectedEntity = (OWLEntity) obj;
                    List<QueryType> queryTypes = QueryType.getTypesForOWLObject(selectedEntity);
                    queryTypeComboBox.setModel(new DefaultComboBoxModel(queryTypes.toArray()));
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

    private JButton getCloseButton() {
        JButton closeBtn = new JButton(LuceneUiHelper.Utils.getIcon(LuceneUiHelper.Utils.CLOSE_ICON_FILENAME, 11, 11));
        closeBtn.addActionListener(e -> {
            JPanel btnHoldingPanel = (JPanel) closeBtn.getParent();
            JPanel queriesPanel = (JPanel) btnHoldingPanel.getParent();
            queriesPanel.remove(btnHoldingPanel);
            Container lastPanel = queriesPanel.getParent();
            while(lastPanel != null) {
                lastPanel.revalidate();
                lastPanel.repaint();
                lastPanel = lastPanel.getParent();
            }
        });
        return closeBtn;
    }

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

    private List<OWLEntity> getProperties() {
        return LuceneUiHelper.getInstance(editorKit).getPropertiesInSignature();
    }

    @Override
    boolean isBasicQuery() {
        return true;
    }

    @Override
    boolean isNegatedQuery() {
        return false;
    }

    @Override
    boolean isNestedQuery() {
        return false;
    }
}
