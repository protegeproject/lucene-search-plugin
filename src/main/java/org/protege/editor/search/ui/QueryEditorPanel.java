package org.protege.editor.search.ui;

import org.protege.editor.core.Disposable;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.search.nci.QueryType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class QueryEditorPanel extends JPanel implements Disposable {
    private static final long serialVersionUID = -4412332384143006925L;
    private static Logger logger = LoggerFactory.getLogger(QueryEditorPanel.class.getName());
    private JButton addQueryBtn, addNegatedQueryBtn, addNestedQueryBtn, clearBtn, searchBtn;
    private JRadioButton matchAll, matchAny;
    private boolean allowNestedQueries = true, allowNegatedQueries = true, allowSearch = true, isNested = false;
    private List<QueryPanel> queries = new ArrayList<>();
    private JPanel queriesPanel;
    private OWLEditorKit editorKit;

    /**
     * Constructor
     *
     * @param editorKit OWL Editor Kit
     */
    public QueryEditorPanel(OWLEditorKit editorKit) {
        this.editorKit = checkNotNull(editorKit);
        initUi();
    }

    public QueryEditorPanel(OWLEditorKit editorKit, boolean allowNestedQueries, boolean allowNegatedQueries, boolean allowSearch) {
        this.editorKit = checkNotNull(editorKit);
        this.allowNestedQueries = checkNotNull(allowNestedQueries);
        this.allowNegatedQueries = checkNotNull(allowNegatedQueries);
        this.allowSearch = checkNotNull(allowSearch);
        isNested = true;
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());
        setBorder(LuceneUiHelper.Utils.EMPTY_BORDER);
        JPanel queriesPanelHolder = new JPanel(new BorderLayout());
        queriesPanelHolder.setBackground(Color.WHITE);
        queriesPanel = new JPanel(new GridBagLayout());
        queriesPanel.setBorder(LuceneUiHelper.Utils.EMPTY_BORDER);
        queriesPanel.setBackground(Color.WHITE);
        queriesPanelHolder.add(queriesPanel, BorderLayout.NORTH);
        add(getHeaderPanel(), BorderLayout.NORTH);
        if(!isNested) {
            // add scrollpane to the top-level panel
            JScrollPane topScrollPane = new JScrollPane(queriesPanelHolder);
            topScrollPane.setBorder(LuceneUiHelper.Utils.EMPTY_BORDER);
            topScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            add(topScrollPane, BorderLayout.CENTER);
            add(getFooterPanel(), BorderLayout.SOUTH);
        } else {
            add(queriesPanelHolder, BorderLayout.CENTER);
        }
    }

    private ActionListener searchBtnListener = e -> {
        // build an lucene query object from all the query clauses
        Object luceneQuery;

        for(QueryPanel queryPanel : queries) {
            if(queryPanel.isBasicQuery()) {
                BasicQueryPanel q = (BasicQueryPanel) queryPanel;
                Object query = getBasicQueryObject(q);

                // TODO: add query to luceneQuery

            } else if(queryPanel.isNegatedQuery()) {
                NegatedQueryPanel q = (NegatedQueryPanel) queryPanel;
                Object query = getNegatedQuery(q);

                // TODO: add to luceneQuery

            } else if(queryPanel.isNestedQuery()) {
                NestedQueryPanel q = (NestedQueryPanel) queryPanel;
                QueryEditorPanel editorPanel = q.getQueryEditorPanel();
                List<QueryPanel> queryList = editorPanel.getQueryPanels();

                List queryClauses = new ArrayList<>();
                for(QueryPanel clausePanel : queryList) {
                    Object query = null;
                    if(clausePanel.isBasicQuery()) {
                        query = getBasicQueryObject((BasicQueryPanel) clausePanel);
                    } else if(clausePanel.isNegatedQuery()) {
                        query = getNegatedQuery((NegatedQueryPanel) clausePanel);
                    }
                    queryClauses.add(query);
                }
                MatchCriteria matchCriteria = editorPanel.getMatchCriteria();
                OWLEntity selectedProperty = q.getSelectedProperty();

                // TODO: create nested query with the list of query clauses, match criteria, and selected property, and add to luceneQuery

            }
        }
    };

    private Object getBasicQueryObject(BasicQueryPanel queryPanel) {
        OWLEntity property = queryPanel.getSelectedProperty();
        QueryType queryType = queryPanel.getSelectedQueryType();
        String value = queryPanel.getInputStringValue();

        // TODO: build basic query clause with above details

        return null;
    }

    private Object getNegatedQuery(NegatedQueryPanel queryPanel) {
        QueryEditorPanel editorPanel = queryPanel.getQueryEditorPanel();
        List<QueryPanel> queryList = editorPanel.getQueryPanels();
        List queryClauses = new ArrayList<>();
        for(QueryPanel q : queryList) {
            Object query = getBasicQueryObject((BasicQueryPanel) q);
            queryClauses.add(query);
        }
        MatchCriteria match = editorPanel.getMatchCriteria();

        // TODO: build negated query with the list of query clauses and match criteria

        return null;
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
        GridBagConstraints c = new GridBagConstraints(0, queries.size(), 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, insets, 0, 0);
        queries.add(queryPanel);
        queriesPanel.add(queryPanel, c);
        refresh();
    }

    private void clearQueryPanel() {
        queries.clear();
        queriesPanel.removeAll();
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
            header.setBorder(new MatteBorder(0, 1, 1, 1, LuceneUiHelper.Utils.MATTE_BORDER_COLOR));
        } else {
            header.setBorder(new MatteBorder(0, 0, 1, 0, LuceneUiHelper.Utils.MATTE_BORDER_COLOR));
        }
        JPanel queryBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addQueryBtn = new JButton("Add Query");
        addQueryBtn.addActionListener(e -> addBasicQuery());
        queryBtnPanel.add(addQueryBtn);
        if (allowNegatedQueries) {
            addNegatedQueryBtn = new JButton("Add Negated Query");
            addNegatedQueryBtn.addActionListener(e -> addNegatedQuery());
            queryBtnPanel.add(addNegatedQueryBtn);
        }
        if (allowNestedQueries) {
            addNestedQueryBtn = new JButton("Add Nested Query");
            addNestedQueryBtn.addActionListener(e -> addNestedQuery());
            queryBtnPanel.add(addNestedQueryBtn);
        }
        header.add(queryBtnPanel, BorderLayout.WEST);
        if(!allowSearch) {
            header.add(getControlsPanel(false), BorderLayout.EAST);
        }
        return header;
    }

    private JPanel getFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        Border topBorder = new MatteBorder(1, 0, 0, 0, LuceneUiHelper.Utils.MATTE_BORDER_COLOR);
        footer.setBorder(topBorder);
        footer.add(getControlsPanel(true), BorderLayout.WEST);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // TODO: remove
        JButton testDialog = new JButton("Test Popup Dialog");
        testDialog.addActionListener(e -> {
            Optional<OWLEntity> ent = CombinedLuceneQueryPanel.showDialog(editorKit);
            if(ent.isPresent()) {
                logger.info("[LucenePopupDialog]    Selected entity: " + ent.get().getIRI());
            } else {
                logger.info("[LucenePopupDialog]    No entity selected");
            }
        });
        searchPanel.add(testDialog);

        searchBtn = new JButton("Search");
        searchBtn.addActionListener(searchBtnListener);
        searchPanel.add(searchBtn);
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
        clearBtn.addActionListener(e -> clearQueryPanel());
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
        searchBtn.removeActionListener(searchBtnListener);
    }
}
