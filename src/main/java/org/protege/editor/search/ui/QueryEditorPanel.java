package org.protege.editor.search.ui;

import org.protege.editor.core.Disposable;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.SearchContext;
import org.protege.editor.search.nci.*;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLProperty;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class QueryEditorPanel extends JPanel implements Disposable {
    private static final long serialVersionUID = -8077360412312971485L;
    private JButton addQueryBtn, addNegatedQueryBtn, addNestedQueryBtn, clearBtn, searchBtn, stopBtn;
    private JRadioButton matchAll, matchAny;
    private boolean allowNestedQueries = true, allowNegatedQueries = true, isNested = false;
    private List<QueryPanel> queries = new ArrayList<>();
    private TreeSet<Integer> constraints = new TreeSet<>();
    private JPanel queriesPanel;
    private OWLEditorKit editorKit;

    /**
     * Constructor
     *
     * @param editorKit OWL Editor Kit
     */
    public QueryEditorPanel(OWLEditorKit editorKit) {
        this.editorKit = checkNotNull(editorKit);
        this.editorKit.getModelManager().addListener(activeOntologyChanged);
        initUi();
    }

    /**
     * Constructor for an inner query editor panel
     *
     * @param editorKit OWL Editor Kit
     * @param allowNestedQueries    true if nested queries should be allowed, false otherwise
     * @param allowNegatedQueries   true if negated queries should be allowed, false otherwise
     */
    public QueryEditorPanel(OWLEditorKit editorKit, boolean allowNestedQueries, boolean allowNegatedQueries) {
        this.editorKit = checkNotNull(editorKit);
        this.allowNestedQueries = checkNotNull(allowNestedQueries);
        this.allowNegatedQueries = checkNotNull(allowNegatedQueries);
        isNested = true;
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());
        setBorder(LuceneUiUtils.EMPTY_BORDER);
        JPanel queriesPanelHolder = new JPanel(new BorderLayout());
        queriesPanelHolder.setBackground(Color.WHITE);
        queriesPanel = new JPanel(new GridBagLayout());
        queriesPanel.setBorder(LuceneUiUtils.EMPTY_BORDER);
        queriesPanel.setBackground(Color.WHITE);
        queriesPanelHolder.add(queriesPanel, BorderLayout.NORTH);
        add(getHeaderPanel(), BorderLayout.NORTH);
        if(!isNested) {
            // add scrollpane to the top-level panel
            JScrollPane topScrollPane = new JScrollPane(queriesPanelHolder);
            topScrollPane.setBorder(LuceneUiUtils.EMPTY_BORDER);
            topScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            add(topScrollPane, BorderLayout.CENTER);
            add(getFooterPanel(), BorderLayout.SOUTH);
        } else {
            add(queriesPanelHolder, BorderLayout.CENTER);
        }
        addBasicQuery();
    }

    private OWLModelManagerListener activeOntologyChanged = e -> {
        if (e.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) || e.isType(EventType.ONTOLOGY_LOADED)) {
            clearEditorPanel();
            addBasicQuery();
        }
    };

    private ActionListener searchBtnListener = e -> {
        if (editorKit.getSearchManager() instanceof SearchTabManager) {
            SearchTabManager searchManager = (SearchTabManager) editorKit.getSearchManager();
            BasicQuery.Factory queryFactory = new BasicQuery.Factory(new SearchContext(editorKit), searchManager);
            boolean emptyQueries = false;

            // build a lucene query object from all the query clauses
            FilteredQuery.Builder builder = new FilteredQuery.Builder();
            for(QueryPanel queryPanel : queries) {
                if(queryPanel.isBasicQuery()) {
                    BasicQuery basicQuery = getBasicQuery((BasicQueryPanel) queryPanel, queryFactory);
                    if(basicQuery != null) {
                        builder.add(basicQuery);
                    } else {
                        emptyQueries = true;
                    }
                } else if(queryPanel.isNegatedQuery()) {
                    NegatedQuery negatedQuery = getNegatedQuery((NegatedQueryPanel) queryPanel, queryFactory, searchManager);
                    if(negatedQuery != null && !negatedQuery.getFilters().isEmpty()) {
                        builder.add(negatedQuery);
                    } else {
                        emptyQueries = true;
                    }
                } else if(queryPanel.isNestedQuery()) {
                    NestedQuery nestedQuery = getNestedQuery((NestedQueryPanel) queryPanel, queryFactory, searchManager);
                    if(nestedQuery != null && !nestedQuery.getFillerFilters().isEmpty()) {
                        builder.add(nestedQuery);
                    } else {
                        emptyQueries = true;
                    }
                }
            }
            MatchCriteria match = getMatchCriteria();
            boolean isMatchAll = (match == MatchCriteria.MATCH_ALL);
            FilteredQuery userQuery = builder.build(isMatchAll);
            if(userQuery != null) {
                searchBtn.setVisible(false);
                stopBtn.setVisible(true);
                searchManager.performSearch(userQuery, searchResults -> handleResults(userQuery, searchResults));
            }
            if(emptyQueries) {
                JOptionPane.showMessageDialog(editorKit.getOWLWorkspace(),
                        new JLabel("One or more queries were not completely formulated, and were ignored. To prevent this message, " +
                                "ensure that all query fields are filled or remove empty query panels."),
                        "Detected one or more empty queries", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        else {
            JOptionPane.showMessageDialog(editorKit.getOWLWorkspace(), new JLabel("Unable to perform Lucene search. Ensure that" +
                            " 'Lucene search tab' is selected in the Protege preferences (under the 'General' tab, in the 'Search type' option)."),
                    "Lucene Search Manager not selected", JOptionPane.INFORMATION_MESSAGE);
        }
    };

    private ActionListener stopBtnListener = e -> {
        // TODO stop search
        stopBtn.setVisible(false);
        searchBtn.setVisible(true);
    };

    private void handleResults(FilteredQuery query, Collection<OWLEntity> results) {
        LuceneQueryPanel queryPanel = getLuceneQueryPanel();
        if(queryPanel != null) {
            queryPanel.getResultsPanel().setResults(query, results);
            stopBtn.setVisible(false);
            searchBtn.setVisible(true);
        }
    }

    private LuceneQueryPanel getLuceneQueryPanel() {
        Component comp = this.getParent();
        while(comp != null) {
            if(comp instanceof LuceneQueryPanel) {
                return (LuceneQueryPanel) comp;
            }
            comp = comp.getParent();
        }
        return null;
    }

    private BasicQuery getBasicQuery(BasicQueryPanel queryPanel, BasicQuery.Factory queryFactory) {
        OWLProperty property = queryPanel.getSelectedProperty();
        QueryType queryType = queryPanel.getSelectedQueryType();
        String value = queryPanel.getInputStringValue();
        if(queryType != null) {
            return queryFactory.createQuery(property, queryType, value);
        } else {
            return null;
        }
    }

    private NegatedQuery getNegatedQuery(NegatedQueryPanel queryPanel, BasicQuery.Factory queryFactory, LuceneSearcher searcher) {
        NegatedQuery.Builder builder = new NegatedQuery.Builder(new SearchContext(editorKit));
        QueryEditorPanel editorPanel = queryPanel.getQueryEditorPanel();
        List<QueryPanel> queryList = editorPanel.getQueryPanels();
        for(QueryPanel q : queryList) {
            SearchTabQuery query = null;
            if (q.isBasicQuery()) {
                query = getBasicQuery((BasicQueryPanel) q, queryFactory);
            }
            else if (q.isNestedQuery()) {
                query = getNestedQuery((NestedQueryPanel) q, queryFactory, searcher);
            }
            if (query != null) {
                builder.add(query);
            }
        }
        MatchCriteria match = editorPanel.getMatchCriteria();
        boolean isMatchAll = (match == MatchCriteria.MATCH_ALL) ? true : false;
        return builder.build(isMatchAll);
    }

    private NestedQuery getNestedQuery(NestedQueryPanel queryPanel, BasicQuery.Factory queryFactory, LuceneSearcher searcher) {
        NestedQuery.Builder builder = new NestedQuery.Builder(searcher);
        QueryEditorPanel editorPanel = queryPanel.getQueryEditorPanel();
        List<QueryPanel> queryList = editorPanel.getQueryPanels();
        for(QueryPanel q : queryList) {
            SearchTabQuery query = null;
            if (q.isBasicQuery()) {
                query = getBasicQuery((BasicQueryPanel) q, queryFactory);
            }
            else if (q.isNegatedQuery()) {
                query = getNegatedQuery((NegatedQueryPanel) q, queryFactory, searcher);
            }
            if (query != null) {
                builder.add(query);
            }
        }
        MatchCriteria match = editorPanel.getMatchCriteria();
        boolean isMatchAll = (match == MatchCriteria.MATCH_ALL) ? true : false;
        if(queryPanel.getSelectedProperty() != null) {
            return builder.build(queryPanel.getSelectedProperty(), isMatchAll);
        } else {
            return null;
        }
    }

    public List<QueryPanel> getQueryPanels() {
        return queries;
    }

    public MatchCriteria getMatchCriteria() {
        if(matchAll.isSelected()) {
            return MatchCriteria.MATCH_ALL;
        } else {
            return MatchCriteria.MATCH_ANY;
        }
    }

    public boolean isNested() {
        return isNested;
    }

    public void addBasicQuery() {
        QueryPanel basicQueryPanel = new BasicQueryPanel(editorKit);
        addQuery(basicQueryPanel);
    }

    public void addNestedQuery() {
        QueryPanel nestedQueryPanel = new NestedQueryPanel(editorKit);
        addQuery(nestedQueryPanel);
    }

    public void addNegatedQuery() {
        QueryPanel negatedQueryPanel = new NegatedQueryPanel(editorKit);
        addQuery(negatedQueryPanel);
    }

    private void addQuery(QueryPanel queryPanel) {
        Insets insets = (isNested ? new Insets(2, 25, 0, 0) : new Insets(4, 4, 0, 4));
        int gridy = queries.size();
        if(!constraints.isEmpty()) {
            int max = constraints.last();
            gridy = Math.max((max+1), queries.size());
        }
        GridBagConstraints c = new GridBagConstraints(0, gridy, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, insets, 0, 0);
        constraints.add(c.gridy);
        queries.add(queryPanel);
        queriesPanel.add(queryPanel, c);
        if(!isNested) {
            searchBtn.setEnabled(true);
        }
        clearBtn.setEnabled(true);
        refresh();
    }

    public void removeQueryPanel(QueryPanel queryPanel) {
        // The query panel gets disposed and removed from the UI by the panel's own close button. Here just remove from the list.
        queries.remove(queryPanel);
        if(queries.isEmpty()) {
            if(!isNested) {
                searchBtn.setEnabled(false);
            }
            clearBtn.setEnabled(false);
        }
    }

    private void clearEditorPanel() {
        queries.forEach(QueryPanel::dispose);
        queries.clear();
        queriesPanel.removeAll();
        if(!isNested) {
            searchBtn.setEnabled(false);
        }
        clearBtn.setEnabled(false);
        refresh();
    }

    private void refresh() {
        revalidate();
        repaint();
        if (isNested) {
            Container container = queriesPanel.getParent();
            while(container != null) {
                container.revalidate();
                container.repaint();
                container = container.getParent();
            }
        }
    }

    private JPanel getHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        if(isNested) {
            header.setBorder(new MatteBorder(0, 1, 1, 1, LuceneUiUtils.MATTE_BORDER_COLOR));
        } else {
            header.setBorder(new MatteBorder(0, 0, 1, 0, LuceneUiUtils.MATTE_BORDER_COLOR));
        }
        JPanel queryBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addQueryBtn = new JButton("Basic Query");
        addQueryBtn.addActionListener(e -> addBasicQuery());
        queryBtnPanel.add(addQueryBtn);
        if (allowNegatedQueries) {
            addNegatedQueryBtn = new JButton("Negated Query");
            addNegatedQueryBtn.addActionListener(e -> addNegatedQuery());
            queryBtnPanel.add(addNegatedQueryBtn);
        }
        if (allowNestedQueries) {
            addNestedQueryBtn = new JButton("Nested Query");
            addNestedQueryBtn.addActionListener(e -> addNestedQuery());
            queryBtnPanel.add(addNestedQueryBtn);
        }
        header.add(queryBtnPanel, BorderLayout.WEST);
        if(isNested) {
            header.add(getControlsPanel(false), BorderLayout.EAST);
        }
        return header;
    }

    private JPanel getFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        Border topBorder = new MatteBorder(1, 0, 0, 0, LuceneUiUtils.MATTE_BORDER_COLOR);
        footer.setBorder(topBorder);
        footer.add(getControlsPanel(true), BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchBtn = new JButton("Search");
        searchBtn.addActionListener(searchBtnListener);
        searchBtn.setEnabled(false);
        searchPanel.add(searchBtn);

        stopBtn = new JButton("Stop search");
        stopBtn.addActionListener(stopBtnListener);
        stopBtn.setVisible(false);
        searchPanel.add(stopBtn);

        footer.add(searchPanel, BorderLayout.EAST);
        return footer;
    }

    private JPanel getControlsPanel(boolean lhs) {
        JPanel controlsPanel = new JPanel();
        if(lhs) {
            controlsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        } else {
            controlsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        }
        clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> clearEditorPanel());
        matchAll = new JRadioButton("Match All", true);
        matchAny = new JRadioButton("Match Any");
        ButtonGroup group = new ButtonGroup();
        group.add(matchAll);
        group.add(matchAny);
        controlsPanel.add(clearBtn);
        controlsPanel.add(matchAll);
        controlsPanel.add(matchAny);
        return controlsPanel;
    }

    @Override
    public void dispose() {
        editorKit.getModelManager().removeListener(activeOntologyChanged);
        if(!isNested) {
            searchBtn.removeActionListener(searchBtnListener);
        }
        clearEditorPanel();
    }
}
