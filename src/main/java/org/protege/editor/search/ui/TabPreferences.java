package org.protege.editor.search.ui;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.search.nci.QueryType;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLProperty;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class TabPreferences {
    private static final String PREFERENCES_KEY = "LuceneTabPreferences";
    private static final String OWL_PROPERTY = "defaultProperty";
    private static final String RESULTS_PER_PAGE = "defaultResultsPerPage";
    private static final String QUERY_TYPE = "defaultQueryType";

    private static final int defaultResultsPerPage = 50;
    private static final QueryType defaultQueryType = QueryType.CONTAINS;
    private static final OWLProperty defaultProperty = OWLManager.getOWLDataFactory().getRDFSLabel();

    private static Preferences getPreferences() {
        return PreferencesManager.getInstance().getApplicationPreferences(PREFERENCES_KEY);
    }

    public static QueryType getDefaultQueryType() {
        String type = getPreferences().getString(QUERY_TYPE, defaultQueryType.getName());
        QueryType qt = QueryType.valueOf(type);
        return qt;
    }

    public static int getMaximumResultsPerPage() {
        return getPreferences().getInt(RESULTS_PER_PAGE, defaultResultsPerPage);
    }

    public static OWLProperty getDefaultProperty(OWLEditorKit editorKit) {
        String propIri = getPreferences().getString(OWL_PROPERTY, defaultProperty.getIRI().toString());
        Optional<OWLProperty> propOpt = LuceneUiUtils.getPropertyForIri(editorKit, IRI.create(propIri));
        if(propOpt.isPresent()) {
            return propOpt.get();
        }
        return defaultProperty;
    }

    public static void setMaximumResultsPerPage(int nrResultsPerPage) {
        getPreferences().putInt(RESULTS_PER_PAGE, checkNotNull(nrResultsPerPage));
    }

    public static void setDefaultProperty(IRI defaultPropertyIri) {
        checkNotNull(defaultPropertyIri);
        getPreferences().putString(OWL_PROPERTY, defaultPropertyIri.toString());
    }

    public static void setDefaultQueryType(QueryType queryType) {
        checkNotNull(queryType);
        getPreferences().putString(QUERY_TYPE, queryType.getName());
    }
}
