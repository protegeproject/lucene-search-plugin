package org.protege.editor.search.ui;

import java.util.Comparator;

import javax.swing.DefaultComboBoxModel;

/*
 * Custom model to make sure the items are stored in a sorted order. The
 * default is to sort in the natural order of the item, but a Comparator can
 * be used to customize the sort order.
 */
class SortedComboBoxModel<E> extends DefaultComboBoxModel<E> {

    private static final long serialVersionUID = -2676459635428216607L;

    private Comparator comparator;

    /*
     * Create an empty model that will use the natural sort order of the
     * item
     */
    public SortedComboBoxModel() {
        super();
    }

    /*
     * Create an empty model that will use the specified Comparator
     */
    public SortedComboBoxModel(Comparator comparator) {
        super();
        this.comparator = comparator;
    }

    @Override
    public void addElement(E element) {
        insertElementAt(element, 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void insertElementAt(E element, int index) {
        int size = getSize();
        // Determine where to insert element to keep model in sorted order
        for (index = 0; index < size; index++) {
            if (comparator != null) {
                E o = getElementAt(index);
                if (comparator.compare(o, element) > 0) {
                    break;
                }
            }
            else {
                Comparable c = (Comparable) getElementAt(index);
                if (c != null && c.compareTo(element) > 0) {
                    break;
                }
            }
        }
        super.insertElementAt(element, index);

        // Select an element when it is added to the beginning of the model
        if (index == 0 && element != null) {
            setSelectedItem(element);
        }
    }
}