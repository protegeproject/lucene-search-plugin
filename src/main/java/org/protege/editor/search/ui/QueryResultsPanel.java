package org.protege.editor.search.ui;

import org.protege.editor.core.Disposable;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.editor.search.nci.FilteredQuery;
import org.semanticweb.owlapi.model.*;
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
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class QueryResultsPanel extends JPanel implements Disposable {
    private static final long serialVersionUID = -9158074562877526184L;
    private static final int MAX_LIST_SIZE = 500;
    private OWLEditorKit editorKit;
    private JList<OWLEntity> results;
    private List<List<OWLEntity>> pagedResultsList;
    private List<OWLEntity> resultsList, txtFieldFilteredResults, entityTypesFilteredResults;
    private JCheckBox classes, properties, individuals, datatypes;
    private JTextField filterTextField;
    private JLabel statusLbl, pageLbl;
    private JButton exportBtn, backBtn, forwardBtn;
    private int currentPage = 0, totalPages;
    private JProgressBar searchProgressBar;
    private Timer visibilityTimer;
    private FilteredQuery answeredQuery;

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
                backBtn.setVisible(false);
                forwardBtn.setVisible(false);
                statusLbl.setText("");
                pageLbl.setText("");
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
            resultsList.clear();
            backBtn.setVisible(false);
            forwardBtn.setVisible(false);
            statusLbl.setText("");
            pageLbl.setText("");
        }
    };

    private ActionListener exportBtnListener = e -> exportResults();

    private ActionListener classesListener = e -> filter(classes, OWLClass.class);

    private ActionListener propertiesListener = e -> filter(properties, OWLProperty.class);

    private ActionListener individualsListener = e -> filter(individuals, OWLNamedIndividual.class);

    private ActionListener datatypesListener = e -> filter(datatypes, OWLDatatype.class);

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
        pageLbl.setText("· Page " + (currentPage+1) + " of " + totalPages + "  (" + MAX_LIST_SIZE  + " results per page)");
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

    private void filter(JCheckBox checkBox, Class cls) {
        List<OWLEntity> entities = new ArrayList<>(getResults()), toRemove = new ArrayList<>();
        if(!checkBox.isSelected()) {
            toRemove.addAll(entities.stream().filter(cls::isInstance).collect(Collectors.toList()));
            entities.removeAll(toRemove);
        } else {
            if(!filterTextField.getText().isEmpty()) {
                txtFieldFilteredResults.stream().filter(e -> cls.isInstance(e) && !entities.contains(e)).forEach(entities::add);
            } else {
                resultsList.stream().filter(e -> cls.isInstance(e) && !entities.contains(e)).forEach(entities::add);
            }
        }
        Collections.sort(entities);
        entityTypesFilteredResults = entities;
        setListData(entities, true);
    }

    private void setListData(List<OWLEntity> list, boolean filteredList) {
        if(list.size() > MAX_LIST_SIZE) {
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
            results.setListData(list.toArray(new OWLEntity[list.size()]));
            if(filteredList) {
                updateStatus(list);
                setPagedResultsList(false);
            }
        }
    }

    private void filterTextField() {
        if(resultsList.isEmpty()) {
            return;
        }
        String toMatch = filterTextField.getText();
        if(toMatch.isEmpty()) {
            setListData(entityTypesFilteredResults, true);
            return;
        }
        OWLEntityFinder finder = editorKit.getModelManager().getOWLEntityFinder();
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
        setListData(txtFieldFilteredResults, true);
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
        answeredQuery = checkNotNull(query);
        resultsList = new ArrayList<>(checkNotNull(entities));
        Collections.sort(resultsList);
        entityTypesFilteredResults = resultsList;
        txtFieldFilteredResults = resultsList;
        setListData(resultsList, true);
    }

    private List<List<OWLEntity>> divideList(List<OWLEntity> list) {
        List<List<OWLEntity>> output = new ArrayList<>();
        int lastIndex = 0;
        while(lastIndex < list.size()) {
            int range = lastIndex + MAX_LIST_SIZE;
            if(range > list.size()) {
                range = list.size();
            }
            List<OWLEntity> sublist = list.subList(lastIndex, range);
            output.add(sublist);
            lastIndex += MAX_LIST_SIZE;
        }
        return output;
    }

    private List<OWLEntity> getResults() {
        List<OWLEntity> output = new ArrayList<>(txtFieldFilteredResults);
        output.retainAll(entityTypesFilteredResults);
        return output;
    }

    private void updateStatus(Collection<OWLEntity> entities) {
        statusLbl.setText(entities.size() + (entities.size() == 1 ? " match" : " matches"));
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
