package org.protege.editor.search.ui;

import com.google.common.base.Objects;
import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.core.ui.list.MListSectionHeader;
import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.core.ui.util.VerifiedInputEditor;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class ExportDialogPanel extends JPanel implements VerifiedInputEditor {
    private static final long serialVersionUID = 2895257460444140568L;
    private OWLEditorKit editorKit;
    private final List<OWLEntity> results;
    private JLabel fileLocationLbl, propertiesLbl, fileDelimLbl, propertyValuesDelimLbl;
    private JTextField fileLocationTxtField, fileDelim, propertyValuesDelim;
    private JCheckBox includePropertyNames, includeEntityTypes, useCurrentRendering, includeSuperclasses, includeCustomText;
    private JButton browseBtn, editCustomTextBtn;
    private MList propertiesList;
    private List<InputVerificationStatusChangedListener> listeners = new ArrayList<>();
    private boolean currentlyValid = false;
    private File selectedFile;
    private PropertyListItem selectedListItem;
    private String customText;

    /**
     * Constructor
     *
     * @param editorKit OWL Editor Kit
     * @param results  List of OWL entities in the query results
     */
    public ExportDialogPanel(OWLEditorKit editorKit, String queryAlgebra, List<OWLEntity> results) {
        this.editorKit = checkNotNull(editorKit);
        this.results = checkNotNull(results);
        this.customText = checkNotNull(queryAlgebra);
        initUi();
    }

    private void initUi() {
        setLayout(new GridBagLayout());
        setupList();

        fileLocationLbl = new JLabel("Export to file:");
        propertiesLbl = new JLabel("Properties to export:");
        fileDelimLbl = new JLabel("File delimiter:");
        propertyValuesDelimLbl = new JLabel("Property values delimiter:");

        fileLocationTxtField = new JTextField();
        fileDelim = new JTextField(",");
        propertyValuesDelim = new JTextField("\t");

        fileDelim.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        fileDelim.addKeyListener(keyListener);
        propertyValuesDelim.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        propertyValuesDelim.addKeyListener(keyListener);

        browseBtn = new JButton("Browse");
        browseBtn.addActionListener(browseBtnListener);
        editCustomTextBtn = new JButton("Edit text");
        editCustomTextBtn.addActionListener(editCustomTextBtnListener);

        includePropertyNames = new JCheckBox("Include headers in first line");
        includeEntityTypes = new JCheckBox("Include entity types");
        useCurrentRendering = new JCheckBox("Use current rendering instead of IRIs");
        includeSuperclasses = new JCheckBox("Include superclasses");
        includeCustomText = new JCheckBox("Include custom text in last line");

        JScrollPane propertiesScrollpane = new JScrollPane(propertiesList);
        propertiesScrollpane.setBorder(LuceneUiUtils.MATTE_BORDER);

        Insets insets = new Insets(2, 2, 2, 2);
        int rowIndex = 0;
        add(fileLocationLbl, new GridBagConstraints(0, rowIndex, 2, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(8, 2, 2, 2), 0, 0));
        rowIndex++;
        add(fileLocationTxtField, new GridBagConstraints(0, rowIndex, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, insets, 0, 0));
        add(browseBtn, new GridBagConstraints(1, rowIndex, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        rowIndex += 2;
        add(propertiesLbl, new GridBagConstraints(0, rowIndex, 2, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(15, 2, 2, 2), 0, 0));
        rowIndex++;
        add(propertiesScrollpane, new GridBagConstraints(0, rowIndex, 2, 1, 1.0, 1.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.BOTH, insets, 0, 0));
        rowIndex += 2;
        add(fileDelimLbl, new GridBagConstraints(0, rowIndex, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(15, 2, 2, 0), 0, 0));
        add(fileDelim, new GridBagConstraints(1, rowIndex, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(15, 0, 2, 2), 0, 0));
        rowIndex++;
        add(propertyValuesDelimLbl, new GridBagConstraints(0, rowIndex, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
        add(propertyValuesDelim, new GridBagConstraints(1, rowIndex, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 2, 2), 0, 0));
        rowIndex += 2;
        add(includePropertyNames, new GridBagConstraints(0, rowIndex, 2, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(15, 0, 2, 2), 0, 0));
        rowIndex++;
        add(includeEntityTypes, new GridBagConstraints(0, rowIndex, 2, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 2, 2), 0, 0));
        rowIndex++;
        add(useCurrentRendering, new GridBagConstraints(0, rowIndex, 2, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 2, 2), 0, 0));
        rowIndex++;
        add(includeSuperclasses, new GridBagConstraints(0, rowIndex, 2, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 2, 2), 0, 0));
        rowIndex++;
        add(includeCustomText, new GridBagConstraints(0, rowIndex, 1, 1, 1.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 2, 2), 0, 0));
        add(editCustomTextBtn, new GridBagConstraints(1, rowIndex, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, new Insets(2, 0, 2, 2), 0, 0));
    }

    private void setupList() {
        propertiesList = new MList() {
            protected void handleAdd() {
                addProperty();
            }

            protected void handleDelete() {
                deleteProperty();
            }
        };
        propertiesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propertiesList.addListSelectionListener(listSelectionListener);
        propertiesList.setCellRenderer(new OwlPropertyListCellRenderer(editorKit));
        propertiesList.addKeyListener(keyAdapter);
        propertiesList.addMouseListener(mouseAdapter);
        propertiesList.setVisibleRowCount(10);
        propertiesList.setBorder(new EmptyBorder(2, 2, 0, 2));

        List<Object> data = new ArrayList<>();
        data.add(new PropertyListHeaderItem());
        propertiesList.setListData(data.toArray());
    }

    private KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_TAB) {
                if(e.getSource().equals(fileDelim)) {
                    fileDelim.setText(fileDelim.getText() + "\t");
                } else if(e.getSource().equals(propertyValuesDelim)) {
                    propertyValuesDelim.setText(propertyValuesDelim.getText() + "\t");
                }
            } else {
                super.keyReleased(e);
            }
        }
    };

    private ListSelectionListener listSelectionListener = e -> {
        if(propertiesList.getSelectedValue() != null && !e.getValueIsAdjusting()) {
            if(propertiesList.getSelectedValue() instanceof PropertyListItem) {
                selectedListItem = (PropertyListItem) propertiesList.getSelectedValue();
            }
        }
    };

    private KeyAdapter keyAdapter = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                if(propertiesList.getSelectedValue() instanceof PropertyListHeaderItem) {
                    addProperty();
                }
            }
        }
    };

    private MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount() == 2) {
                if(propertiesList.getSelectedValue() instanceof PropertyListHeaderItem) {
                    addProperty();
                }
            }
        }
    };

    private ActionListener browseBtnListener = e -> {
        selectedFile = UIUtil.saveFile(this, "Choose CSV file location", "CSV file", null, "lucene-export.csv");
        if(selectedFile != null) {
            String filename;
            if(!selectedFile.getName().endsWith(".csv")) {
                filename = selectedFile.getAbsolutePath() + ".csv";
                selectedFile = new File(filename);
            }
            fileLocationTxtField.setText(selectedFile.getAbsolutePath());
            checkInputs();
        }
    };

    private ActionListener editCustomTextBtnListener = e -> {
        customText = CustomTextDialogPanel.showDialog(editorKit, customText);
    };

    private void checkInputs() {
        boolean allValid = true;
        if (selectedFile == null) {
            allValid = false;
        }
        setValid(allValid);
    }

    private void setValid(boolean valid) {
        currentlyValid = valid;
        for (InputVerificationStatusChangedListener l : listeners) {
            l.verifiedStatusChanged(currentlyValid);
        }
    }

    private void addProperty() {
        Optional<List<OWLEntity>> entitiesOpt = AddPropertyDialogPanel.showDialog(editorKit, getProperties());
        if(entitiesOpt.isPresent()) {
            List<OWLEntity> entities = entitiesOpt.get();
            List items = getPropertyListItems();
            for (OWLEntity e : entities) {
                items.add(new PropertyListItem(e));
            }
            propertiesList.setListData(items.toArray());
        }
    }

    private void deleteProperty() {
        List items = getPropertyListItems();
        items.remove(selectedListItem);
        propertiesList.setListData(items.toArray());
    }

    private void exportToCsv() throws IOException {
        CsvExporter csvExporter = new CsvExporterBuilder()
                .setEditorKit(editorKit)
                .setOutputFile(selectedFile)
                .setResults(results)
                .setProperties(getProperties())
                .setFileDelimiter(fileDelim.getText())
                .setPropertyValuesDelimiter(propertyValuesDelim.getText())
                .setIncludeEntityTypes(includeEntityTypes.isSelected())
                .setIncludeCustomText(includeCustomText.isSelected())
                .setIncludeHeaders(includePropertyNames.isSelected())
                .setIncludeSuperclasses(includeSuperclasses.isSelected())
                .setUseCurrentRendering(useCurrentRendering.isSelected())
                .setCustomText(customText)
                .createCsvExporter();
        csvExporter.export();
    }

    private List<OWLEntity> getProperties() {
        List<OWLEntity> entities = new ArrayList<>();
        for(Object obj : getPropertyListItems()) {
            if(obj instanceof PropertyListItem) {
                entities.add(((PropertyListItem)obj).getProperty());
            }
        }
        return entities;
    }

    private List<?> getPropertyListItems() {
        List<Object> properties = new ArrayList<>();
        ListModel model = propertiesList.getModel();
        for(int i = 0; i < model.getSize(); i++) {
            properties.add(model.getElementAt(i));
        }
        return properties;
    }

    public static boolean showDialog(OWLEditorKit editorKit, String queryAlgebra, List<OWLEntity> results) throws IOException {
        ExportDialogPanel panel = new ExportDialogPanel(editorKit, queryAlgebra, results);
        int response = JOptionPaneEx.showValidatingConfirmDialog(
                editorKit.getOWLWorkspace(), "Export results to CSV file", panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
        if (response == JOptionPane.OK_OPTION) {
            panel.exportToCsv();
            return true;
        }
        return false;
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


    /**
     * Property list header item
     */
    public class PropertyListHeaderItem implements MListSectionHeader {

        @Override
        public String getName() {
            return "Properties";
        }

        @Override
        public boolean canAdd() {
            return true;
        }
    }

    /**
     * Property list item
     */
    public class PropertyListItem implements MListItem {
        private OWLEntity property;

        /**
         * Constructor
         *
         * @param property OWL property
         */
        public PropertyListItem(OWLEntity property) {
            this.property = checkNotNull(property);
        }

        public OWLEntity getProperty() {
            return property;
        }

        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public void handleEdit() {

        }

        @Override
        public boolean isDeleteable() {
            return true;
        }

        @Override
        public boolean handleDelete() {
            return true;
        }

        @Override
        public String getTooltip() {
            return property.getIRI().toQuotedString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PropertyListItem)) {
                return false;
            }
            PropertyListItem that = (PropertyListItem) o;
            return Objects.equal(property, that.property);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(property);
        }
    }
}
