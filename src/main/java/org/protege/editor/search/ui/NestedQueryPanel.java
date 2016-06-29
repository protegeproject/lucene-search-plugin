package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class NestedQueryPanel extends QueryPanel {
    private static final long serialVersionUID = 2866861625595434385L;
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
        setBorder(LuceneUiHelper.Utils.EMPTY_BORDER);

        JPanel parent = new JPanel(new GridLayout(0, 1));
        parent.setBorder(LuceneUiHelper.Utils.EMPTY_BORDER);

        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(new MatteBorder(1, 1, 1, 1, LuceneUiHelper.Utils.MATTE_BORDER_COLOR));

        JLabel title = new JLabel("<html><b><i>" + NAME + "</i></b></html>");
        title.setForeground(Color.DARK_GRAY);
        topPanel.add(title, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(3, 5, 0, 5), 0, 0));
        topPanel.add(getCloseButton(), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));

        parent.add(topPanel);
        parent.add(getPropertySelectionPanel());
        add(parent, BorderLayout.NORTH);

        editorPanel = new QueryEditorPanel(editorKit, false, true, false);
        editorPanel.addBasicQuery();
        add(editorPanel, BorderLayout.CENTER);
    }

    private JPanel getPropertySelectionPanel() {
        List<OWLEntity> list = LuceneUiHelper.getInstance(editorKit).getPropertiesInSignature();
        propertyComboBox = new OwlEntityComboBox(editorKit, list);
        JLabel propSelectionLbl = new JLabel("Property");
        JPanel propSelectionPanel = new JPanel(new GridBagLayout());
        propSelectionPanel.add(propSelectionLbl, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, new Insets(13, 11, 6, 3), 0, 0));
        propSelectionPanel.add(propertyComboBox, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(13, 3, 6, 8), 0, 0));
        propSelectionPanel.setBorder(new MatteBorder(0, 1, 0, 1, LuceneUiHelper.Utils.MATTE_BORDER_COLOR));
        return propSelectionPanel;
    }

    private JButton getCloseButton() {
        JButton closeBtn = new JButton(LuceneUiHelper.Utils.getIcon(LuceneUiHelper.Utils.CLOSE_ICON_FILENAME, 11, 11));
        closeBtn.addActionListener(e -> {
            JPanel btnHoldingPanel = (JPanel) closeBtn.getParent().getParent().getParent();
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

    public OWLEntity getSelectedProperty() {
        return (OWLEntity) propertyComboBox.getSelectedItem();
    }

    public QueryEditorPanel getQueryEditorPanel() {
        return editorPanel;
    }
}
