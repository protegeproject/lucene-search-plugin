package org.protege.editor.search.lucene;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.semanticweb.owlapi.model.IRI;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 */
public class IndexRecordTable extends JTable {

    private static final long serialVersionUID = 8265198081868345267L;

    public static final int ONTOLOGY_IRI_COLUMN = 0;
    public static final int INDEX_DIRECTORY_LOCATION_COLUMN = 1;
    public static final int INDEX_LAST_MODIFIED_COLUMN = 2;

    public IndexRecordTable() {
        setModel(new IndexRecordTableModel());
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
        String ontologyIriString = (String) getValueAt(selectedRow, ONTOLOGY_IRI_COLUMN);
        LuceneIndexPreferences.removeIndexRecord(IRI.create(ontologyIriString));
        ((AbstractTableModel) getModel()).fireTableDataChanged();
    }

    class IndexRecordTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        private List<List<String>> indexRecordTable = new ArrayList<>();

        public IndexRecordTableModel() {
            indexRecordTable = LuceneIndexPreferences.getIndexRecordTable();
            addTableModelListener(e -> {
                indexRecordTable = LuceneIndexPreferences.getIndexRecordTable();
            });
        }

        @Override
        public int getRowCount() {
            return indexRecordTable.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Ontology IRI";
                case 1:
                    return "Index directory location";
                case 2:
                    return "Last modified";
                default:
                    return "";
                }
        }

        @Override
        public Object getValueAt(int row, int column) {
            List<String> indexRecord = indexRecordTable.get(row);
            String ontologyIri = indexRecord.get(1);
            String directoryLocation = indexRecord.get(2);
            switch (column) {
                case 0: // "Ontology IRI"
                    return ontologyIri;
                case 1: // "Index directory location"
                    if (!new File(directoryLocation).exists()) {
                        return "(directory not found)";
                    }
                    return directoryLocation;
                case 2: // "Last modified"
                    if (!new File(directoryLocation).exists()) {
                        return "N/A";
                    }
                    File indexDirectory = new File(directoryLocation);
                    Date now = new Date();
                    return TimeUnit.MILLISECONDS.toDays(now.getTime() - indexDirectory.lastModified()) + " days ago";
                default:
                    return "";
            }
        }
    }
}
