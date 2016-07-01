package org.protege.editor.search.lucene;

import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 18/02/2016
 */
public class SearchPreferencesPanel extends OWLPreferencesPanel {

    private static final long serialVersionUID = -818021477356581474L;

    private JTextField txtIndexLocation = new JTextField(40);

    private JRadioButton rbUserHomeDir = new JRadioButton("User home directory");
    private JRadioButton rbTempDir = new JRadioButton("System temporary directory");
    private JRadioButton rbCustomLocation = new JRadioButton("User custom location");

    @Override
    public void initialise() throws Exception {
        setLayout(new BorderLayout());

        PreferencesLayoutPanel panel = new PreferencesLayoutPanel();
        add(panel, BorderLayout.NORTH);

        panel.addGroup("Index location");
        panel.addGroupComponent(txtIndexLocation);
        txtIndexLocation.setText(LuceneSearchPreferences.getBaseDirectory());
        txtIndexLocation.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!rbCustomLocation.isSelected()) {
                    rbCustomLocation.setSelected(true);
                    LuceneSearchPreferences.setCustomDirectoryAsBaseDirectory();
                }
            }
        });

        rbUserHomeDir.setSelected(LuceneSearchPreferences.useUserHomeDirectoryAsBaseDirectory());
        rbTempDir.setSelected(LuceneSearchPreferences.useTempDirectoryAsBaseDirectory());
        rbCustomLocation.setSelected(LuceneSearchPreferences.useCustomDirectoryAsBaseDirectory());

        panel.addGroupComponent(rbUserHomeDir);
        panel.addGroupComponent(rbTempDir);
        panel.addGroupComponent(rbCustomLocation);

        ButtonGroup indexLocationGroup = new ButtonGroup();
        indexLocationGroup.add(rbUserHomeDir);
        indexLocationGroup.add(rbTempDir);
        indexLocationGroup.add(rbCustomLocation);
        panel.addVerticalPadding();

        rbUserHomeDir.addActionListener(e -> {
            LuceneSearchPreferences.setUserHomeDirectoryAsBaseDirectory();
            txtIndexLocation.setText(LuceneSearchPreferences.getUserHomeDirectory());
        });
        rbTempDir.addActionListener(e -> {
            LuceneSearchPreferences.setTempDirectoryAsBaseDirectory();
            txtIndexLocation.setText(LuceneSearchPreferences.getTempDirectory());
        });
        rbCustomLocation.addActionListener(e -> {
            LuceneSearchPreferences.setCustomDirectoryAsBaseDirectory();
            txtIndexLocation.requestFocus();
            txtIndexLocation.selectAll();
        });
        
        panel.addGroup("Stored indexes");
        IndexInfoTable tblIndexLocation = new IndexInfoTable();
        JScrollPane scpIndexLocation = new JScrollPane(tblIndexLocation);
        scpIndexLocation.setPreferredSize(new Dimension(650, 300));
        panel.addGroupComponent(scpIndexLocation);
        
        JButton btnRemoveSelected = new JButton("Remove selected index");
        btnRemoveSelected.addActionListener(l -> {
            int selectedRow = tblIndexLocation.getSelectedRow();
            if (selectedRow != -1) {
                String locationKey = (String) tblIndexLocation.getValueAt(selectedRow, 0); // 0 = first column
                String msg = String.format("<html>Are you sure want to remove index for ontology '%s'?<br/>"
                        + "<br/>"
                        + "This action will remove the index directory on the disk.<br/>"
                        + "<br/>"
                        + "Index directory location:<br/>"
                        + "%s</html>", locationKey, (String) tblIndexLocation.getValueAt(selectedRow, 1));
                int answer = JOptionPaneEx.showConfirmDialog(this, "Remove selected index", new JLabel(msg),
                        JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
                if (answer == JOptionPane.OK_OPTION) {
                    tblIndexLocation.removeIndex(selectedRow);
                }
            }
        });
        panel.addGroupComponent(btnRemoveSelected);
    }

    @Override
    public void dispose() throws Exception {
        // NO-OP
    }

    @Override
    public void applyChanges() {
        LuceneSearchPreferences.setBaseDirectory(txtIndexLocation.getText());
    }
}
