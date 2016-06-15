package org.protege.editor.search.lucene;

import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
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
