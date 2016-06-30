package org.protege.editor.search.ui;

import org.protege.editor.core.Disposable;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.editor.owl.model.search.SearchManager;
import org.protege.editor.search.nci.SearchTabManager;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class QueryResultsPanel extends JPanel implements Disposable {
    private static final long serialVersionUID = -7541052466202147909L;
    private OWLEditorKit editorKit;
    private JList<OWLEntity> results;
    private List<OWLEntity> resultsList, txtFieldFilteredResults;
    private JCheckBox classes, properties, individuals, datatypes;
    private JTextField filterTextField;
    private JLabel statusLbl;
    private JButton exportBtn;

    /**
     * Constructor
     *
     * @param editorKit OWL Editor Kit
     */
    public QueryResultsPanel(OWLEditorKit editorKit) {
        this.editorKit = checkNotNull(editorKit);
        SearchManager searchManager = editorKit.getSearchManager();
        if (searchManager instanceof SearchTabManager) {
            ((SearchTabManager) searchManager).addSearchListener(luceneListener);
        }
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        results = new JList<>();
        results.setCellRenderer(new OWLCellRenderer(editorKit));
        results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        results.setFixedCellHeight(21);

        JPanel resultsPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(results);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(LuceneUiHelper.Utils.EMPTY_BORDER);

        resultsPanel.setBorder(LuceneUiHelper.Utils.EMPTY_BORDER);
        resultsPanel.add(scrollPane, BorderLayout.CENTER);

        add(getHeaderPanel(), BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);
        add(getFooterPanel(), BorderLayout.SOUTH);
    }

    private ActionListener exportBtnListener = e -> exportResults();

    private ActionListener entityTypesListener = e -> filterEntityTypes();

    private DocumentListener filterTextListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            filterTextField();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            filterTextField();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            filterTextField();
        }
    };

    private LuceneListener luceneListener = new LuceneListener() {
        @Override
        public void searchStarted(LuceneEvent event) {
            statusLbl.setText("Performing search...");
        }

        @Override
        public void searchFinished(LuceneEvent event) {
            Optional<Collection<OWLEntity>> optCollection = event.getResults();
            if(optCollection.isPresent()) {
                setResults(optCollection.get());
            }
        }
    };

    public OWLEntity getSelectedEntity() {
        return results.getSelectedValue();
    }

    private JPanel getHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new MatteBorder(0, 0, 1, 0, LuceneUiHelper.Utils.MATTE_BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 40));

        statusLbl = new JLabel("Results");
        statusLbl.setBorder(new EmptyBorder(0, 4, 0, 0));
        header.add(statusLbl, BorderLayout.WEST);

        JPanel exportPanel = new JPanel(new FlowLayout());
        exportBtn = new JButton("Export Results");
        exportBtn.addActionListener(exportBtnListener);
        exportPanel.add(exportBtn);
        header.add(exportPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel getFooterPanel() {
        JPanel footer = new JPanel(new GridBagLayout());
        footer.setBorder(new MatteBorder(1, 0, 0, 0, LuceneUiHelper.Utils.MATTE_BORDER_COLOR));
        footer.setPreferredSize(new Dimension(0, 40));

        JLabel filterLbl = new JLabel("Filter:");
        classes = new JCheckBox("Classes");
        properties = new JCheckBox("Properties");
        individuals = new JCheckBox("Individuals");
        datatypes = new JCheckBox("Datatypes");
        classes.setSelected(true);
        properties.setSelected(true);
        individuals.setSelected(true);
        datatypes.setSelected(true);
        classes.addActionListener(entityTypesListener);
        properties.addActionListener(entityTypesListener);
        individuals.addActionListener(entityTypesListener);
        datatypes.addActionListener(entityTypesListener);

        filterTextField = new JTextField();
        filterTextField.setMinimumSize(new Dimension(60, 22));
        filterTextField.getDocument().addDocumentListener(filterTextListener);

        Insets insets = new Insets(2, 4, 2, 4);
        int rowIndex = 0;
        footer.add(filterLbl, new GridBagConstraints(0, rowIndex, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, insets, 0, 0));
        footer.add(filterTextField, new GridBagConstraints(1, rowIndex, 1, 1, 100.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, insets, 0, 0));
        footer.add(classes, new GridBagConstraints(2, rowIndex, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        footer.add(properties, new GridBagConstraints(3, rowIndex, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        footer.add(individuals, new GridBagConstraints(4, rowIndex, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        footer.add(datatypes, new GridBagConstraints(5, rowIndex, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, insets, 0, 0));
        return footer;
    }

    private void filterEntityTypes() {
        List<OWLEntity> entities = new ArrayList<>(getResults()), toRemove = new ArrayList<>();
        if(!classes.isSelected()) {
            toRemove.addAll(entities.stream().filter(OWLEntity::isOWLClass).collect(Collectors.toList()));
        } else {
            if(!filterTextField.getText().isEmpty()) {
                txtFieldFilteredResults.stream().filter(e -> e.isOWLClass() && !entities.contains(e)).forEach(entities::add);
            } else {
                resultsList.stream().filter(e -> e.isOWLClass() && !entities.contains(e)).forEach(entities::add);
            }
        }
        if(!properties.isSelected()) {
            toRemove.addAll(entities.stream().filter(e -> (e.isOWLAnnotationProperty() || e.isOWLDataProperty() || e.isOWLObjectProperty())).collect(Collectors.toList()));
        } else {
            if(!filterTextField.getText().isEmpty()) {
                txtFieldFilteredResults.stream().filter(e -> (e.isOWLAnnotationProperty() || e.isOWLDataProperty() || e.isOWLObjectProperty()) && !entities.contains(e)).forEach(entities::add);
            } else {
                resultsList.stream().filter(e -> !entities.contains(e) && (e.isOWLAnnotationProperty() || e.isOWLDataProperty() || e.isOWLObjectProperty())).forEach(entities::add);
            }
        }
        if(!individuals.isSelected()) {
            toRemove.addAll(entities.stream().filter(OWLEntity::isOWLNamedIndividual).collect(Collectors.toList()));
        } else {
            if(!filterTextField.getText().isEmpty()) {
                txtFieldFilteredResults.stream().filter(e -> e.isOWLNamedIndividual() && !entities.contains(e)).forEach(entities::add);
            } else {
                resultsList.stream().filter(e -> e.isOWLNamedIndividual() && !entities.contains(e)).forEach(entities::add);
            }
        }
        if(!datatypes.isSelected()) {
            toRemove.addAll(entities.stream().filter(OWLEntity::isOWLDatatype).collect(Collectors.toList()));
        } else {
            if(!filterTextField.getText().isEmpty()) {
                txtFieldFilteredResults.stream().filter(e -> e.isOWLDatatype() && !entities.contains(e)).forEach(entities::add);
            } else {
                resultsList.stream().filter(e -> !entities.contains(e) && e.isOWLDatatype()).forEach(entities::add);
            }
        }
        entities.removeAll(toRemove);
        Collections.sort(entities);
        results.setListData(entities.toArray(new OWLEntity[entities.size()]));
        updateResultsLabel(entities);
    }

    private void filterTextField() {
        String toMatch = filterTextField.getText();
        if(toMatch.isEmpty()) {
            results.setListData(resultsList.toArray(new OWLEntity[resultsList.size()]));
            updateResultsLabel(resultsList);
            return;
        }
        OWLEntityFinder finder = editorKit.getOWLModelManager().getOWLEntityFinder();
        List<OWLEntity> output = new ArrayList<>();
        Set<OWLEntity> entities = finder.getMatchingOWLEntities(toMatch);
        for(OWLEntity e : entities) {
            if(resultsList.contains(e)) {
                if(classes.isSelected() && e.isOWLClass()) {
                    output.add(e);
                } else if(properties.isSelected() && (e.isOWLAnnotationProperty() || e.isOWLDataProperty() || e.isOWLObjectProperty())) {
                    output.add(e);
                } else if(individuals.isSelected() && e.isOWLNamedIndividual()) {
                    output.add(e);
                } else if(datatypes.isSelected() && e.isOWLDatatype()) {
                    output.add(e);
                }
            }
        }
        txtFieldFilteredResults = new ArrayList<>(output);
        Collections.sort(txtFieldFilteredResults);
        results.setListData(txtFieldFilteredResults.toArray(new OWLEntity[txtFieldFilteredResults.size()]));
        updateResultsLabel(output);
    }

    private void exportResults() {
        boolean success = false;
        List<OWLEntity> results = getResults();
        if (!results.isEmpty()) {
            try {
                success = ExportDialogPanel.showDialog(editorKit, results);
            } catch (IOException e) {
                ErrorLogPanel.showErrorDialog(e);
            }
            if (success) {
                JOptionPane.showMessageDialog(editorKit.getOWLWorkspace(), new JLabel("The results have been successfully exported to CSV file."),
                        "Results exported to CSV file", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(editorKit.getOWLWorkspace(), new JLabel("There are no results to export."),
                    "No results to export", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void setResults(Collection<OWLEntity> entities) {
        resultsList = new ArrayList<>(checkNotNull(entities));
        Collections.sort(resultsList);
        updateResultsLabel(entities);
        results.setListData(resultsList.toArray(new OWLEntity[entities.size()]));
    }

    private List<OWLEntity> getResults() {
        List<OWLEntity> output = new ArrayList<>();
        ListModel<OWLEntity> model = results.getModel();
        for(int i = 0; i < model.getSize(); i++) {
            output.add(model.getElementAt(i));
        }
        return output;
    }

    private void updateResultsLabel(Collection<OWLEntity> entities) {
        statusLbl.setText(entities.size() + (entities.size() == 1 ? " match" : " matches"));
    }

    @Override
    public void dispose() {
        exportBtn.removeActionListener(exportBtnListener);
        classes.removeActionListener(entityTypesListener);
        properties.removeActionListener(entityTypesListener);
        individuals.removeActionListener(entityTypesListener);
        datatypes.removeActionListener(entityTypesListener);
        filterTextField.getDocument().removeDocumentListener(filterTextListener);
    }
}
