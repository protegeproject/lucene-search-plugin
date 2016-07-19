package org.protege.editor.search.ui;

import com.google.common.collect.ImmutableList;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.editor.search.nci.FilteredQuery;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.util.ProgressMonitor;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class QueryResultsPanel extends JPanel implements Disposable {
    private static final long serialVersionUID = 8625864045536664635L;
    private OWLEditorKit editorKit;
    private JList<OWLEntity> results;
    private List<List<OWLEntity>> pagedResultsList;
    private ImmutableList<OWLEntity> resultsList;
    private List<OWLEntity> txtFieldFilteredResults, entityTypesFilteredResults;
    private List<OWLEntity> classesList = new ArrayList<>(), propertiesList = new ArrayList<>(),
            individualsList = new ArrayList<>(), datatypesList = new ArrayList<>();
    private JCheckBox classes, properties, individuals, datatypes;
    private JTextField filterTextField;
    private JLabel statusLbl, pageLbl;
    private JButton exportBtn, backBtn, forwardBtn;
    private int currentPage = 0, totalPages;
    private JProgressBar searchProgressBar;
    private Timer visibilityTimer;
    private FilteredQuery answeredQuery;
    private boolean categorisedEntityTypes = false, paged = false;

    /**
     * Constructor
     *
     * @param editorKit OWL Editor Kit
     */
    public QueryResultsPanel(OWLEditorKit editorKit) {
        visibilityTimer = new Timer(200, e -> searchProgressBar.setVisible(true));
        this.editorKit = checkNotNull(editorKit);
        this.editorKit.getModelManager().addListener(activeOntologyChanged);
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());
        setupProgressBar();

        results = new JList<>();
        results.setCellRenderer(new OWLCellRenderer(editorKit));
        results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        results.setFixedCellHeight(21);
        results.addMouseListener(listMouseListener);
        results.addKeyListener(listKeyListener);

        JPanel resultsPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(results);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(LuceneUiUtils.EMPTY_BORDER);

        resultsPanel.setBorder(LuceneUiUtils.EMPTY_BORDER);
        resultsPanel.add(scrollPane, BorderLayout.CENTER);

        add(getHeaderPanel(), BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);
        add(getFooterPanel(), BorderLayout.SOUTH);
    }

    private void setupProgressBar() {
        searchProgressBar = new JProgressBar();
        searchProgressBar.putClientProperty("JComponent.sizeVariant", "small");
        searchProgressBar.setVisible(false);
        editorKit.getSearchManager().addProgressMonitor(new ProgressMonitor() {
            @Override
            public void setStarted() {
                setPagedResultsList(false);
                statusLbl.setText("");
                searchProgressBar.setValue(0);
                visibilityTimer.restart();
            }

            @Override
            public void setSize(long l) {
                searchProgressBar.setMinimum(0);
                searchProgressBar.setMaximum((int) l);
            }

            @Override
            public void setProgress(long l) {
                searchProgressBar.setValue((int) l);
            }

            @Override
            public void setMessage(String s) {
                searchProgressBar.setToolTipText(s);
                statusLbl.setText(s);
            }

            @Override
            public void setIndeterminate(boolean b) {
                searchProgressBar.setIndeterminate(b);
            }

            @Override
            public void setFinished() {
                visibilityTimer.stop();
                searchProgressBar.setVisible(false);
                statusLbl.setText("");
            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        });
    }

    private OWLModelManagerListener activeOntologyChanged = e -> {
        if (e.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) || e.isType(EventType.ONTOLOGY_LOADED)) {
            results.setListData(new OWLEntity[0]);
            resultsList = null;
            backBtn.setVisible(false);
            forwardBtn.setVisible(false);
            statusLbl.setText("");
            pageLbl.setText("");
            categorisedEntityTypes = false;
            filterTextField.setText("");
            setCheckBoxSelection(true);
            clearBuckets();
        }
    };

    private ActionListener exportBtnListener = e -> exportResults();

    private ActionListener classesListener = e -> filterEntityType(classes, classesList);

    private ActionListener propertiesListener = e -> filterEntityType(properties, propertiesList);

    private ActionListener individualsListener = e -> filterEntityType(individuals, individualsList);

    private ActionListener datatypesListener = e -> filterEntityType(datatypes, datatypesList);

    private DocumentListener filterTextListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            filterTextField(true);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            filterTextField(true);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            filterTextField(true);
        }
    };

    private MouseListener listMouseListener = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
            if(e.getClickCount() == 2) {
                selectEntity();
            }
            super.mouseReleased(e);
        }
    };

    private KeyListener listKeyListener = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            if(e.getKeyChar() == KeyEvent.VK_ENTER) {
                selectEntity();
            }
        }
    };

    private ActionListener backBtnListener = e -> {
        currentPage--;
        if(!forwardBtn.isEnabled()) {
            forwardBtn.setEnabled(true);
        }
        if(currentPage == 0) {
            backBtn.setEnabled(false);
        }
        updatePageLabel();
        setListData(pagedResultsList.get(currentPage), false);
    };

    private ActionListener forwardBtnListener = e -> {
        currentPage++;
        if(!backBtn.isEnabled()) {
            backBtn.setEnabled(true);
        }
        if(currentPage == pagedResultsList.size()-1) {
            forwardBtn.setEnabled(false);
        }
        updatePageLabel();
        setListData(pagedResultsList.get(currentPage), false);
    };

    private void updatePageLabel() {
        pageLbl.setText("· Page " + (currentPage+1) + " of " + totalPages + "  (" + getMaximumResultsSize()  + " results per page)");
    }

    private void selectEntity() {
        OWLEntity selectedEntity = getSelectedEntity();
        if (selectedEntity != null) {
            editorKit.getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(selectedEntity);
            editorKit.getOWLWorkspace().displayOWLEntity(selectedEntity);
        }
    }

    public OWLEntity getSelectedEntity() {
        return results.getSelectedValue();
    }

    public void setPagedResultsList(boolean pagedResultsList) {
        backBtn.setEnabled(false);
        if(pagedResultsList) {
            backBtn.setVisible(true);
            forwardBtn.setVisible(true);
            forwardBtn.setEnabled(true);
        } else {
            backBtn.setVisible(false);
            forwardBtn.setVisible(false);
            pageLbl.setText("");
        }
    }

    private JPanel getHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new MatteBorder(0, 0, 1, 0, LuceneUiUtils.MATTE_BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 40));

        JPanel pagesPanel = new JPanel(new GridBagLayout());
        backBtn = new JButton(LuceneUiUtils.getIcon(LuceneUiUtils.BACK_ICON_FILENAME, 12, 12));
        forwardBtn = new JButton(LuceneUiUtils.getIcon(LuceneUiUtils.FORWARD_ICON_FILENAME, 12, 12));
        backBtn.setVisible(false);
        backBtn.setBackground(Color.WHITE);
        backBtn.setPreferredSize(new Dimension(36, 27));
        backBtn.addActionListener(backBtnListener);
        forwardBtn.setVisible(false);
        forwardBtn.setBackground(Color.WHITE);
        forwardBtn.setPreferredSize(new Dimension(36, 27));
        forwardBtn.addActionListener(forwardBtnListener);
        pagesPanel.add(searchProgressBar,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
        pagesPanel.add(backBtn,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
        pagesPanel.add(forwardBtn,
                new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));

        statusLbl = new JLabel();
        statusLbl.setBorder(new EmptyBorder(0, 4, 0, 0));
        pagesPanel.add(statusLbl,
                new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 1, 0, 0), 0, 0));

        pageLbl = new JLabel("");
        pagesPanel.add(pageLbl,
                new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0));
        header.add(pagesPanel, BorderLayout.WEST);

        JPanel exportPanel = new JPanel(new FlowLayout());
        exportBtn = new JButton("Export Results");
        exportBtn.addActionListener(exportBtnListener);
        exportBtn.setEnabled(false);
        exportPanel.add(exportBtn);
        header.add(exportPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel getFooterPanel() {
        JPanel footer = new JPanel(new GridBagLayout());
        footer.setBorder(new MatteBorder(1, 0, 0, 0, LuceneUiUtils.MATTE_BORDER_COLOR));
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
        classes.addActionListener(classesListener);
        properties.addActionListener(propertiesListener);
        individuals.addActionListener(individualsListener);
        datatypes.addActionListener(datatypesListener);

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

    private void filterEntityType(JCheckBox checkBox, List<OWLEntity> bucket) {
        if(!categorisedEntityTypes) {
            categoriseEntityTypes();
        }
        entityTypesFilteredResults = new ArrayList<>(getResults());
        if(!checkBox.isSelected()) {
            entityTypesFilteredResults.removeAll(bucket);
        } else {
            filterTextField(false);
            List<OWLEntity> l = new ArrayList<>(bucket);
            l.retainAll(txtFieldFilteredResults);
            for(OWLEntity e : l) {
                if(!entityTypesFilteredResults.contains(e)) {
                    entityTypesFilteredResults.add(e);
                }
            }
        }
        Collections.sort(entityTypesFilteredResults);
        setListData(entityTypesFilteredResults, true);
    }

    /**
     * Filter the results list according to the text field filter
     *
     * @param filterEntityTypes true if this filter should take into account the status of entity type filter checkboxes, false otherwise
     */
    private void filterTextField(boolean filterEntityTypes) {
        if(resultsList.isEmpty()) {
            return;
        }
        String toMatch = filterTextField.getText();
        List<OWLEntity> output;
        if(toMatch.isEmpty()) {
            output = new ArrayList<>(resultsList);
        } else {
            OWLEntityFinder finder = editorKit.getModelManager().getOWLEntityFinder();
            Set<OWLEntity> foundEntities = finder.getMatchingOWLEntities(toMatch);
            foundEntities.retainAll(resultsList);
            output = new ArrayList<>(foundEntities);
        }
        if(filterEntityTypes) {
            if (!classes.isSelected()) {
                output.removeAll(classesList);
            }
            if (!properties.isSelected()) {
                output.removeAll(propertiesList);
            }
            if (!individuals.isSelected()) {
                output.removeAll(individualsList);
            }
            if (!datatypes.isSelected()) {
                output.removeAll(datatypesList);
            }
        }
        txtFieldFilteredResults = output;
        Collections.sort(txtFieldFilteredResults);
        setListData(txtFieldFilteredResults, true);
    }

    private void categoriseEntityTypes() {
        for(OWLEntity e : resultsList) {
            if(e.isOWLClass()) {
                classesList.add(e);
            } else if(e instanceof OWLProperty) {
                propertiesList.add(e);
            } else if(e.isOWLNamedIndividual()) {
                individualsList.add(e);
            } else if(e.isOWLDatatype()) {
                datatypesList.add(e);
            }
        }
        categorisedEntityTypes = true;
    }

    private void setListData(List<OWLEntity> list, boolean filteredList) {
        if(list.size() > getMaximumResultsSize()) {
            paged = true;
            pagedResultsList = divideList(list);
            totalPages = pagedResultsList.size();
            currentPage = 0;
            List<OWLEntity> sublist = pagedResultsList.get(0);
            updatePageLabel();
            results.setListData(sublist.toArray(new OWLEntity[sublist.size()]));
            if(filteredList) {
                setPagedResultsList(true);
                updateStatus(list);
            }
        } else {
            paged = false;
            results.setListData(list.toArray(new OWLEntity[list.size()]));
            if(filteredList) {
                updateStatus(list);
                setPagedResultsList(false);
            }
        }
    }

    private void exportResults() {
        boolean success = false;
        List<OWLEntity> results = getResults();
        if (!results.isEmpty()) {
            try {
                success = ExportDialogPanel.showDialog(editorKit, answeredQuery.getAlgebraString(), results);
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

    public void setResults(FilteredQuery query, Collection<OWLEntity> entities) {
        filterTextField.setText("");
        setCheckBoxSelection(true);
        exportBtn.setEnabled(true);
        answeredQuery = checkNotNull(query);
        List<OWLEntity> list = new ArrayList<>(entities);
        Collections.sort(list);
        resultsList = ImmutableList.copyOf(list);
        entityTypesFilteredResults = resultsList;
        txtFieldFilteredResults = resultsList;
        setListData(resultsList, true);
    }

    private List<List<OWLEntity>> divideList(List<OWLEntity> list) {
        List<List<OWLEntity>> output = new ArrayList<>();
        int lastIndex = 0;
        while(lastIndex < list.size()) {
            int range = lastIndex + getMaximumResultsSize();
            if(range > list.size()) {
                range = list.size();
            }
            List<OWLEntity> sublist = list.subList(lastIndex, range);
            output.add(sublist);
            lastIndex += getMaximumResultsSize();
        }
        return output;
    }

    private List<OWLEntity> getResults() {
        List<OWLEntity> output = new ArrayList<>();
        if(paged) {
            pagedResultsList.forEach(output::addAll);
        } else {
            ListModel<OWLEntity> model = results.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                output.add(model.getElementAt(i));
            }
        }
        return output;
    }

    public int getMaximumResultsSize() {
        return TabPreferences.getMaximumResultsPerPage();
    }

    private void updateStatus(Collection<OWLEntity> entities) {
        statusLbl.setText(entities.size() + (entities.size() == 1 ? " match" : " matches"));
    }

    public void setCheckBoxSelection(boolean selected) {
        classes.setSelected(selected);
        properties.setSelected(selected);
        individuals.setSelected(selected);
        datatypes.setSelected(selected);
    }

    private void clearBuckets() {
        classesList.clear();
        propertiesList.clear();
        individualsList.clear();
        datatypesList.clear();
    }

    @Override
    public void dispose() {
        exportBtn.removeActionListener(exportBtnListener);
        classes.removeActionListener(classesListener);
        properties.removeActionListener(propertiesListener);
        individuals.removeActionListener(individualsListener);
        datatypes.removeActionListener(datatypesListener);
        filterTextField.getDocument().removeDocumentListener(filterTextListener);
        results.removeMouseListener(listMouseListener);
        results.removeKeyListener(listKeyListener);
        editorKit.getModelManager().removeListener(activeOntologyChanged);
    }
}
