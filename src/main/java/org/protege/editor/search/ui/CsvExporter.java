package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class CsvExporter {
    private final boolean includeHeaders, includeEntityTypes, useCurrentRendering, includeSuperclasses, includeCustomText;
    private final String fileDelimiter, propertyValuesDelimiter, customText;
    private final List<OWLEntity> results, properties;
    private final File outputFile;
    private OWLEditorKit editorKit;
    private OWLModelManagerEntityRenderer renderer;
    private OWLOntology ont;

    private static final Logger logger = LoggerFactory.getLogger(CsvExporter.class.getName());

    /**
     * Package-private constructor. Use {@link CsvExporterBuilder}
     *
     * @param editorKit OWL Editor Kit
     * @param outputFile    Output file for CSV export
     * @param results   List of entities in the results set that should be exported
     * @param properties    List of properties selected to be exported
     * @param fileDelimiter Primary delimiter for entries
     * @param propertyValuesDelimiter   Delimiter for the (potentially multiple) values of the properties selected
     * @param includeHeaders  true if headers (e.g., property names) should be included in the first row of the file, false otherwise
     * @param includeEntityTypes    true if a column specifying the type of entity in each result row should be included, false otherwise
     * @param useCurrentRendering   true if the currently selected entity rendering should be used instead of IRIs, false otherwise
     * @param includeSuperclasses   true if the superclass(es) of each class in the result set should be included, false otherwise
     * @param includeCustomText true if a row should be added at the end of the file containing custom text, false otherwise
     * @param customText    Custom text to be included in the last row of the file
     */
    CsvExporter(OWLEditorKit editorKit, File outputFile, List<OWLEntity> results, List<OWLEntity> properties, String fileDelimiter, String propertyValuesDelimiter, boolean includeHeaders,
                boolean includeEntityTypes, boolean useCurrentRendering, boolean includeSuperclasses, boolean includeCustomText, String customText) {
        this.editorKit = checkNotNull(editorKit);
        this.outputFile = checkNotNull(outputFile);
        this.results = checkNotNull(results);
        this.properties = checkNotNull(properties);
        this.fileDelimiter = checkNotNull(fileDelimiter);
        this.propertyValuesDelimiter = checkNotNull(propertyValuesDelimiter);
        this.includeHeaders = checkNotNull(includeHeaders);
        this.includeEntityTypes = checkNotNull(includeEntityTypes);
        this.useCurrentRendering = checkNotNull(useCurrentRendering);
        this.includeSuperclasses = checkNotNull(includeSuperclasses);
        this.includeCustomText = checkNotNull(includeCustomText);
        this.customText = checkNotNull(customText);
        renderer = editorKit.getOWLModelManager().getOWLEntityRenderer();
        ont = editorKit.getOWLModelManager().getActiveOntology();
    }

    // TODO: Work in progress
    public void export() throws IOException {
        FileWriter fw = new FileWriter(outputFile);
        String header = getHeader();
        logger.info("exporting to: " + outputFile.getAbsolutePath());

        OWLReasoner reasoner = null;
        if(isIncludingSuperclasses()) {
            reasoner = LuceneUiHelper.getInstance(editorKit).getReasoner();
        }
        List<String> rows = new ArrayList<>();
        for(OWLEntity e : results) {
            String row = getEntityRendering(e) + fileDelimiter;
            if(includeEntityTypes) {
                row += e.getEntityType().getName() + fileDelimiter;
            }
            if(includeSuperclasses) {
                if(e.isOWLClass()) {
                    row += getSuperclasses(e, reasoner) + fileDelimiter;
                }
            }
            if(!properties.isEmpty()) {
                for (int i = 0; i < properties.size(); i++) {
                    row += getPropertyValues(e, properties.get(i));
                }
            }
            rows.add(row);
        }
        if(includeHeaders) {
            rows.add(0, header);
        }
        if(includeCustomText) {
            rows.add("\n\n" + customText);
        }
        for (String row : rows) { // write results to file
            fw.write(row + "\n");
        }
        fw.flush();
        fw.close();
    }

    private String getHeader() {
        String header = "Entity" + fileDelimiter;
        if(includeEntityTypes) {
            header += "Type" + fileDelimiter;
        }
        if(includeSuperclasses) {
            header += "Superclass(es)" + fileDelimiter;
        }
        if(!properties.isEmpty()) {
            for (int i = 0; i < properties.size(); i++) {
                header += getEntityRendering(properties.get(i)) + fileDelimiter;
            }
        }
        return header;
    }

    private String getPropertyValues(OWLEntity entity, OWLEntity prop) {
        logger.info("\tGetting values for property " + getEntityRendering(prop) + " on " + getEntityRendering(entity));
        List<String> values = new ArrayList<>();
        if(prop.isOWLAnnotationProperty()) {
            Set<OWLAnnotationAssertionAxiom> axioms = ont.getAnnotationAssertionAxioms(entity.getIRI());
            for(OWLAnnotationAssertionAxiom ax : axioms) {
                if(ax.getProperty().equals(prop)) {
                    OWLAnnotationValue annValue = ax.getValue();
                    if(annValue instanceof IRI) {
                        logger.info("\t   value is IRI");
                        values.add(annValue.toString());
                    } else if(annValue instanceof OWLLiteral) {
                        logger.info("\t   value is owl literal");
                        String literalStr =  ((OWLLiteral) annValue).getLiteral();
                        literalStr = literalStr.replaceAll("\"", "'");
                        values.add("\"" + literalStr + "\"");
                        logger.info("\t\tliteral: " + literalStr);
                    } else if(annValue instanceof OWLAnonymousIndividual) {
                        values.add("AnonymousIndividual-" + ((OWLAnonymousIndividual)annValue).getID().getID());
                    }
                }
            }
        } else if(prop.isOWLDataProperty() && entity.isOWLNamedIndividual()) {
            // TODO
        } else if(prop.isOWLObjectProperty()) {
            // TODO
        }

        String output = "";
        if(!values.isEmpty()) {
            Iterator<String> iter = values.iterator();
            while (iter.hasNext()) {
                output += iter.next();
                if (iter.hasNext()) {
                    output += propertyValuesDelimiter;
                } else {
                    output += fileDelimiter;
                }
            }
        } else {
            output += fileDelimiter;
        }
        return output;
    }

    private String getSuperclasses(OWLEntity e, OWLReasoner reasoner) {
        Set<OWLClass> superclasses = reasoner.getSuperClasses(e.asOWLClass(), true).getFlattened();
        String output = "";
        Iterator<OWLClass> iter = superclasses.iterator();
        while (iter.hasNext()) {
            OWLClass c = iter.next();
            output += getEntityRendering(c);
            if(iter.hasNext()) {
                output += propertyValuesDelimiter;
            }
        }
        logger.info("\t" + output);
        return output;
    }

    private String getEntityRendering(OWLEntity e) {
        String rendering;
        if(useCurrentRendering) {
            rendering = renderer.render(e);
        } else {
            rendering = e.getIRI().toString();
        }
        return rendering;
    }

    public String getFileDelimiter() {
        return fileDelimiter;
    }

    public String getPropertyValuesDelimiter() {
        return propertyValuesDelimiter;
    }

    public String getCustomText() {
        return customText;
    }

    public List<OWLEntity> getResults() {
        return results;
    }

    public List<OWLEntity> getProperties() {
        return properties;
    }

    public boolean isIncludingHeaders() {
        return includeHeaders;
    }

    public boolean isIncludingEntityTypes() {
        return includeEntityTypes;
    }

    public boolean isUsingCurrentRendering() {
        return useCurrentRendering;
    }

    public boolean isIncludingSuperclasses() {
        return includeSuperclasses;
    }

    public boolean isIncludingCustomText() {
        return includeCustomText;
    }

    public File getOutputFile() {
        return outputFile;
    }
}
