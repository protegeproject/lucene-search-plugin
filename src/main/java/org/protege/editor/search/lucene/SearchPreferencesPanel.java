package org.protege.editor.search.lucene;

import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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

    private JSpinner spnOntologySize = new JSpinner();

    @Override
    public void initialise() throws Exception {
        setLayout(new BorderLayout());

        PreferencesLayoutPanel panel = new PreferencesLayoutPanel();
        add(panel, BorderLayout.NORTH);

        panel.addGroup("Index location");
        panel.addGroupComponent(txtIndexLocation);
        txtIndexLocation.setText(LuceneIndexPreferences.getBaseDirectory());
        txtIndexLocation.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!rbCustomLocation.isSelected()) {
                    rbCustomLocation.setSelected(true);
                    LuceneIndexPreferences.setCustomDirectoryAsBaseDirectory();
                }
            }
        });

        rbUserHomeDir.setSelected(LuceneIndexPreferences.useUserHomeDirectoryAsBaseDirectory());
        rbTempDir.setSelected(LuceneIndexPreferences.useTempDirectoryAsBaseDirectory());
        rbCustomLocation.setSelected(LuceneIndexPreferences.useCustomDirectoryAsBaseDirectory());

        panel.addGroupComponent(rbUserHomeDir);
        panel.addGroupComponent(rbTempDir);
        panel.addGroupComponent(rbCustomLocation);

        ButtonGroup indexLocationGroup = new ButtonGroup();
        indexLocationGroup.add(rbUserHomeDir);
        indexLocationGroup.add(rbTempDir);
        indexLocationGroup.add(rbCustomLocation);
        panel.addVerticalPadding();

        rbUserHomeDir.addActionListener(e -> {
            LuceneIndexPreferences.setUserHomeDirectoryAsBaseDirectory();
            txtIndexLocation.setText(LuceneIndexPreferences.getUserHomeDirectory());
        });
        rbTempDir.addActionListener(e -> {
            LuceneIndexPreferences.setTempDirectoryAsBaseDirectory();
            txtIndexLocation.setText(LuceneIndexPreferences.getTempDirectory());
        });
        rbCustomLocation.addActionListener(e -> {
            LuceneIndexPreferences.setCustomDirectoryAsBaseDirectory();
            txtIndexLocation.requestFocus();
            txtIndexLocation.selectAll();
        });
        
        panel.addGroup("Stored indexes");
        IndexRecordTable tblIndexRecord = new IndexRecordTable();
        JScrollPane scpIndexLocation = new JScrollPane(tblIndexRecord);
        scpIndexLocation.setPreferredSize(new Dimension(600, 250));
        panel.addGroupComponent(scpIndexLocation);
        
        JButton btnRemoveSelected = new JButton("Remove selected index");
        btnRemoveSelected.addActionListener(l -> {
            int selectedRow = tblIndexRecord.getSelectedRow();
            if (selectedRow != -1) {
                String directoryLocation = (String) tblIndexRecord.getValueAt(selectedRow, IndexRecordTable.INDEX_DIRECTORY_LOCATION_COLUMN);
                String msg = String.format("<html>Are you sure want to remove the selected index?<br/>"
                        + "<br/>"
                        + "This action will remove the index directory on the disk.<br/>"
                        + "<br/>"
                        + "Index directory location:<br/>"
                        + "%s</html>", directoryLocation);
                int answer = JOptionPaneEx.showConfirmDialog(this, "Remove selected index", new JLabel(msg),
                        JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
                if (answer == JOptionPane.OK_OPTION) {
                    tblIndexRecord.removeIndex(selectedRow);
                }
            }
        });
        panel.addGroupComponent(btnRemoveSelected);
        panel.addVerticalPadding();

        JPanel pnlAdvancedOption = new JPanel();
        JCheckBox useCustomInDiskIndexing = new JCheckBox("Set Lucene to store the index in memory when the ontology file size is less than");
        useCustomInDiskIndexing.setSelected(LuceneIndexPreferences.useInMemoryIndexStoring());
        useCustomInDiskIndexing.addActionListener(evt -> {
            LuceneIndexPreferences.setInMemoryIndexStoring(useCustomInDiskIndexing.isSelected());
            spnOntologySize.setEnabled(useCustomInDiskIndexing.isSelected());
        });
        pnlAdvancedOption.add(useCustomInDiskIndexing);
        spnOntologySize.setEnabled(useCustomInDiskIndexing.isSelected());
        JFormattedTextField txtOntologySize = ((JSpinner.NumberEditor) spnOntologySize.getEditor()).getTextField();
        txtOntologySize.setText(LuceneIndexPreferences.getMaxSizeForInMemoryIndexStoring()+"");
        ((NumberFormatter) txtOntologySize.getFormatter()).setAllowsInvalid(false);
        spnOntologySize.addChangeListener(e -> {
            SpinnerNumberModel model = (SpinnerNumberModel) spnOntologySize.getModel();
            int value = model.getNumber().intValue();
            LuceneIndexPreferences.setMaxSizeForInMemoryIndexStoring(value);
        });
        pnlAdvancedOption.add(spnOntologySize);
        pnlAdvancedOption.add(new JLabel(" MB"));
        panel.addGroupComponent(pnlAdvancedOption);
    }

    @Override
    public void dispose() throws Exception {
        // NO-OP
    }

    @Override
    public void applyChanges() {
        LuceneIndexPreferences.setBaseDirectory(txtIndexLocation.getText());
    }
}
