package org.protege.editor.search.nci;

import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.util.CollectionFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/06/2016
 */
public final class QueryType {

    private final String name;
    private final String displayName;
    private final boolean valueType;
    private final boolean nonValueType;
    private final boolean booleanType;

    private QueryType(String name, String displayName, boolean valueType, boolean nonValueType, boolean booleanType) {
        this.name = name;
        this.displayName = displayName;
        this.valueType = valueType;
        this.nonValueType = nonValueType;
        this.booleanType = booleanType;
    }

    private static QueryType getInstance(String name, String displayName, boolean valueType, boolean nonValueType,
            boolean booleanType) {
        return new QueryType(name, displayName, valueType, nonValueType, booleanType);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isValueType() {
        return valueType;
    }

    public boolean isNonValueType() {
        return nonValueType;
    }

    public boolean isBooleanType() {
        return booleanType;
    }

    public static List<QueryType> getTypesForOWLObject(OWLObject owlObject) {
        List<QueryType> types = new ArrayList<>();
        if (owlObject instanceof OWLDataProperty) {
            types.addAll(ValueQueryTypes);
            types.addAll(NonValueQueryTypes);
        }
        else if (owlObject instanceof OWLObjectProperty) {
            types.addAll(NonValueQueryTypes);
        }
        else if (owlObject instanceof OWLAnnotationProperty) {
            types.addAll(ValueQueryTypes);
            types.addAll(NonValueQueryTypes);
        }
        return types; 
    }

    //@formatter:off
    public static final QueryType CONTAINS = getInstance("ContainsQuery", "contains", true, false, false);
    public static final QueryType STARTS_WITH = getInstance("StartsWithQuery", "starts with", true, false, false);
    public static final QueryType ENDS_WITH = getInstance("EndsWithQuery", "ends with", true, false, false);
    public static final QueryType EXACT_MATCH = getInstance("ExactMatchQuery", "exact match", true, false, false);
    public static final QueryType PROPERTY_VALUE_ABSENT = getInstance("PropertyValueAbsentQuery", "property value absent", false, true, false);
    public static final QueryType PROPERTY_VALUE_PRESENT = getInstance("PropertyValuePresentQuery", "property value present", false, true, false);
    public static final QueryType PROPERTY_RESTRICTION_ABSENT = getInstance("PropertyRestrictionAbsentQuery", "property restriction absent", false, true, false);
    public static final QueryType PROPERTY_RESTRICTION_PRESENT = getInstance("PropertyRestrictionPresentQuery", "property restriction present", false, true, false);
    public static final QueryType IS = getInstance("IsQuery", "is", false, false, true);

    //@formatter:off
    public static final List<QueryType> QUERY_TYPES = CollectionFactory.list(
            CONTAINS,
            STARTS_WITH,
            ENDS_WITH,
            EXACT_MATCH,
            PROPERTY_VALUE_ABSENT,
            PROPERTY_VALUE_PRESENT,
            PROPERTY_RESTRICTION_ABSENT,
            PROPERTY_RESTRICTION_PRESENT,
            IS);

    public static final List<QueryType> ValueQueryTypes = CollectionFactory.list(
            CONTAINS,
            STARTS_WITH,
            ENDS_WITH,
            EXACT_MATCH);

    public static final List<QueryType> NonValueQueryTypes = CollectionFactory.list(
            PROPERTY_VALUE_ABSENT,
            PROPERTY_VALUE_PRESENT,
            PROPERTY_RESTRICTION_ABSENT,
            PROPERTY_RESTRICTION_PRESENT);

    public static final List<QueryType> BooleanQueryTypes = CollectionFactory.list(IS);
}
