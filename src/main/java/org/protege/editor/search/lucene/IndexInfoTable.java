package org.protege.editor.search.lucene;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 30/06/2016
 */
@Deprecated
public class IndexInfoTable extends JTable {

    private static final long serialVersionUID = 8148865819337287518L;

    public IndexInfoTable() {
        setModel(new IndexLocationTableModel());
        setRowHeight(getRowHeight() + 10);
        setGridColor(Color.LIGHT_GRAY);
        setShowHorizontalLines(true);
        setShowVerticalLines(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resizeColumnWidth();
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    private void resizeColumnWidth() {
        final TableColumnModel columnModel = getColumnModel();
        for (int column = 0; column < getColumnCount(); column++) {
            int width = 50; // Min width
            String col = columnModel.getColumn(column).getHeaderValue().toString();
            int headerWidth = getFontMetrics(getFont()).stringWidth(col);
            for (int row = 0; row < getRowCount(); row++) {
                TableCellRenderer renderer = getCellRenderer(row, column);
                Component comp = prepareRenderer(renderer, row, column);
                width = Math.max(Math.max(comp.getPreferredSize().width+5, headerWidth+5), width);
            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    public void removeIndex(int selectedRow) {
        String locationKey = (String) getValueAt(selectedRow, 0); // 0 = first column
        LuceneSearchPreferences.removeIndexLocation(locationKey);
        ((AbstractTableModel) getModel()).fireTableDataChanged();
    }

    class IndexLocationTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        private List<String> locationKeys = new ArrayList<>();

        private Map<String, String> indexLocationMap = new HashMap<>();

        public IndexLocationTableModel() {
            initialize();
            addTableModelListener(e -> {
                locationKeys.clear();
                indexLocationMap = LuceneSearchPreferences.getIndexLocationMap();
                locationKeys.addAll(indexLocationMap.keySet());
            });
        }

        public int getRowCount() {
            return indexLocationMap.size();
        }

        public int getColumnCount() {
            return 3;
        }

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Ontology ID";
                case 1:
                    return "Index location path";
                case 2:
                    return "Last modified";
                default:
                    return "";
                }
        }

        public Object getValueAt(int row, int column) {
            String path = indexLocationMap.get(locationKeys.get(row));
            switch (column) {
                case 0: // "Ontology ID"
                    return locationKeys.get(row);
                case 1: // "Index location path"
                    if (path == null) {
                        path = "(path not found)";
                    }
                    return path;
                case 2: // "Last modified"
                    if (path == null) {
                        return "N/A";
                    }
                    File f = new File(path);
                    if (f.exists()) {
                        Date now = new Date();
                        return TimeUnit.MILLISECONDS.toDays(now.getTime() - f.lastModified()) + " days ago";
                    }
                    else {
                        return "(file not found)";
                    }
                default:
                    return "";
            }
        }

        private void initialize() {
            indexLocationMap = LuceneSearchPreferences.getIndexLocationMap();
            locationKeys.addAll(indexLocationMap.keySet());
        }
    }
}
