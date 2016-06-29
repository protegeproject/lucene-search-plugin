package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLEntity;

import java.io.File;
import java.util.List;

public class CsvExporterBuilder {
    private OWLEditorKit editorKit;
    private File outputFile;
    private List<OWLEntity> results;
    private List<OWLEntity> properties;
    private String fileDelimiter;
    private String propertyValuesDelimiter;
    private boolean includeHeaders;
    private boolean includeEntityTypes;
    private boolean useCurrentRendering;
    private boolean includeSuperclasses;
    private boolean includeCustomText;
    private String customText;

    public CsvExporterBuilder setEditorKit(OWLEditorKit editorKit) {
        this.editorKit = editorKit;
        return this;
    }

    public CsvExporterBuilder setOutputFile(File outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    public CsvExporterBuilder setResults(List<OWLEntity> results) {
        this.results = results;
        return this;
    }

    public CsvExporterBuilder setProperties(List<OWLEntity> properties) {
        this.properties = properties;
        return this;
    }

    public CsvExporterBuilder setFileDelimiter(String fileDelimiter) {
        this.fileDelimiter = fileDelimiter;
        return this;
    }

    public CsvExporterBuilder setPropertyValuesDelimiter(String propertyValuesDelimiter) {
        this.propertyValuesDelimiter = propertyValuesDelimiter;
        return this;
    }

    public CsvExporterBuilder setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
        return this;
    }

    public CsvExporterBuilder setIncludeEntityTypes(boolean includeEntityTypes) {
        this.includeEntityTypes = includeEntityTypes;
        return this;
    }

    public CsvExporterBuilder setUseCurrentRendering(boolean useCurrentRendering) {
        this.useCurrentRendering = useCurrentRendering;
        return this;
    }

    public CsvExporterBuilder setIncludeSuperclasses(boolean includeSuperclasses) {
        this.includeSuperclasses = includeSuperclasses;
        return this;
    }

    public CsvExporterBuilder setIncludeCustomText(boolean includeCustomText) {
        this.includeCustomText = includeCustomText;
        return this;
    }

    public CsvExporterBuilder setCustomText(String customText) {
        this.customText = customText;
        return this;
    }

    public CsvExporter createCsvExporter() {
        return new CsvExporter(editorKit, outputFile, results, properties, fileDelimiter, propertyValuesDelimiter, includeHeaders, includeEntityTypes,
                useCurrentRendering, includeSuperclasses, includeCustomText, customText);
    }
}