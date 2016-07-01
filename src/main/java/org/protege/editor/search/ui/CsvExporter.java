package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.protege.editor.owl.ui.renderer.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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
    private OWLModelManagerEntityRenderer entityRenderer;
    private OWLObjectRenderer objectRenderer;
    private OWLOntology ont;
    private OwlClassExpressionVisitor visitor = new OwlClassExpressionVisitor();

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

        OWLModelManager manager = editorKit.getOWLModelManager();
        entityRenderer = manager.getOWLEntityRenderer();
        objectRenderer = manager.getOWLObjectRenderer();
        ont = manager.getActiveOntology();
    }

    public void export() throws IOException {
        logger.info("Exporting Lucene search results to: " + outputFile.getAbsolutePath());
        FileWriter fw = new FileWriter(outputFile);
        String header = getHeader();
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
                for (OWLEntity property : properties) {
                    row += getPropertyValues(e, property);
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
        logger.info(" ... done exporting");
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
            for (OWLEntity property : properties) {
                header += getEntityRendering(property) + fileDelimiter;
            }
        }
        return header;
    }

    private String getPropertyValues(OWLEntity entity, OWLEntity property) {
        List<String> values = new ArrayList<>();
        if(property.isOWLAnnotationProperty()) {
            values = getAnnotationPropertyValues(entity, property);
        } else if(property.isOWLDataProperty()) {
            values = getPropertyValuesForEntity(entity, property);
        } else if(property.isOWLObjectProperty()) {
            values = getPropertyValuesForEntity(entity, property);
        }
        String output = "";
        if(!values.isEmpty()) {
            Iterator<String> iter = values.iterator();
            output += "\"";
            while (iter.hasNext()) {
                output += "'" + iter.next() + "'";
                if (iter.hasNext()) {
                    output += propertyValuesDelimiter;
                } else {
                    output += "\"" + fileDelimiter;
                }
            }
        } else {
            output += fileDelimiter;
        }
        return output;
    }

    private List<String> getAnnotationPropertyValues(OWLEntity entity, OWLEntity property) {
        List<String> values = new ArrayList<>();
        Set<OWLAnnotationAssertionAxiom> axioms = ont.getAnnotationAssertionAxioms(entity.getIRI());
        for(OWLAnnotationAssertionAxiom ax : axioms) {
            if(ax.getProperty().equals(property)) {
                OWLAnnotationValue annValue = ax.getValue();
                if(annValue instanceof IRI) {
                    values.add(annValue.toString());
                } else if(annValue instanceof OWLLiteral) {
                    String literalStr = ((OWLLiteral) annValue).getLiteral();
                    literalStr = literalStr.replaceAll("\"", "'");
                    values.add(literalStr);
                } else if(annValue instanceof OWLAnonymousIndividual) {
                    values.add("AnonymousIndividual-" + ((OWLAnonymousIndividual)annValue).getID().getID());
                }
            }
        }
        return values;
    }

    private List<String> getPropertyValuesForEntity(OWLEntity entity, OWLEntity property) {
        List<String> values = new ArrayList<>();
        if(entity.isOWLClass()) {
            for (OWLAxiom axiom : ont.getAxioms((OWLClass) entity, Imports.INCLUDED)) {
                if(axiom.getSignature().contains(property)) {
                    logger.info("Searching for axioms for entity: " + getEntityRendering(entity) + " and property: " + getEntityRendering(property));
                    logger.info("   Axiom : " + axiom);
                    if(axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF)) {
                        Optional<String> filler = getFillerForAxiom((OWLSubClassOfAxiom)axiom, entity, property);
                        if(filler.isPresent()) {
                            values.add(filler.get());
                            logger.info("\t Filler: " + filler.get());
                        }
                    } else if (axiom.getAxiomType().equals(AxiomType.EQUIVALENT_CLASSES)) {
                        OWLSubClassOfAxiom subClassOfAxiom = ((OWLEquivalentClassesAxiom) axiom).asOWLSubClassOfAxioms().iterator().next();
                        Optional<String> filler = getFillerForAxiom(subClassOfAxiom, entity, property);
                        if(filler.isPresent()) {
                            values.add(filler.get());
                            logger.info("\t Filler: " + filler.get());
                        }
                    } else {
                        // TODO
                    }
                }
            }
        }
        return values;
    }

    private Optional<String> getFillerForAxiom(OWLSubClassOfAxiom axiom, OWLEntity entity, OWLEntity property) {
        String filler = null;
        OWLClassExpression ce;
        if(axiom.getSubClass().equals(entity)) {
            ce = axiom.getSuperClass();
        } else {
            ce = axiom.getSubClass();
        }
        ce.accept(visitor);
        Optional<OWLEntity> optProp = visitor.getProperty();
        if(optProp.isPresent() && optProp.get().equals(property)) {
            Optional<OWLObject> optFiller = visitor.getFiller();
            if(optFiller.isPresent()) {
                filler = objectRenderer.render(optFiller.get());
            }
        }
        return Optional.ofNullable(filler);
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
        return output;
    }

    private String getEntityRendering(OWLEntity e) {
        String rendering;
        if(useCurrentRendering) {
            rendering = entityRenderer.render(e);
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
