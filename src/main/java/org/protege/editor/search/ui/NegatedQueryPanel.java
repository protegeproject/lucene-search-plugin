package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class NegatedQueryPanel extends QueryPanel {
    private static final long serialVersionUID = 8236262202815041235L;
    private static final String NAME = "Negated Query";
    private QueryEditorPanel editorPanel;

    /**
     * Constructor
     *
     * @param editorKit OWL Editor Kit
     */
    public NegatedQueryPanel(OWLEditorKit editorKit) {
        super(editorKit);
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());
        setBorder(LuceneUiUtils.EMPTY_BORDER);

        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(new MatteBorder(1, 1, 1, 1, LuceneUiUtils.MATTE_BORDER_COLOR));
        topPanel.setPreferredSize(new Dimension(0, 36));

        JLabel title = new JLabel("<html><b>" + NAME + " (NOT)</b></html>");
        title.setForeground(new Color(173, 12, 12));
        topPanel.add(title, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(3, 5, 0, 5), 0, 0));
        topPanel.add(getCloseButton(), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        add(topPanel, BorderLayout.NORTH);

        editorPanel = new QueryEditorPanel(editorKit, true, false);
        add(editorPanel, BorderLayout.CENTER);
    }

    @Override
    public boolean isBasicQuery() {
        return false;
    }

    @Override
    public boolean isNegatedQuery() {
        return true;
    }

    @Override
    public boolean isNestedQuery() {
        return false;
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
