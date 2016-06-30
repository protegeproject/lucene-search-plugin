package org.protege.editor.search.lucene;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 15/05/2016
 */
public class LuceneSearchPreferences {

    private static final Logger logger = LoggerFactory.getLogger(LuceneSearchPreferences.class);

    public static final String PREFERENCES_KEY = "SearchIndexPreferences";

    public static final String BASE_DIR = "BASE_DIR";

    public static final String PREFIX_INDEX_DIR = "ProtegeIndex";

    public static final String USE_TMP_DIR = "UseTmpDirectory";
    public static final String USE_HOME_DIR = "UseHomeDirectory";
    public static final String USE_CUSTOM_DIR = "UseCustomDirectory";

    public static final String PROTEGE_DIR = ".Protege";

    public static final String COLLECTOR_DIR = "lucene";

    private static String fsSeparator = System.getProperty("file.separator");

    private static Preferences getPreferences() {
        return PreferencesManager.getInstance().getApplicationPreferences(PREFERENCES_KEY);
    }

    public static String getTempDirectory() {
        return prepare(System.getProperty("java.io.tmpdir"));
    }

    public static String getUserHomeDirectory() {
        String homeDir = prepare(System.getProperty("user.home"));
        return homeDir + fsSeparator + PROTEGE_DIR + fsSeparator + COLLECTOR_DIR;
    }

    public static boolean useTempDirectoryAsBaseDirectory() {
        return getPreferences().getBoolean(USE_TMP_DIR, false);
    }

    /**
     * Use system's temp directory to store index file.
     */
    public static void setTempDirectoryAsBaseDirectory() {
        getPreferences().putBoolean(USE_TMP_DIR, true);
        getPreferences().putBoolean(USE_HOME_DIR, false);
        getPreferences().putBoolean(USE_CUSTOM_DIR, false);
    }

    public static boolean useUserHomeDirectoryAsBaseDirectory() {
        return getPreferences().getBoolean(USE_HOME_DIR, true);
    }

    /**
     * Use system's user home directory to store index file.
     */
    public static void setUserHomeDirectoryAsBaseDirectory() {
        getPreferences().putBoolean(USE_HOME_DIR, true);
        getPreferences().putBoolean(USE_TMP_DIR, false);
        getPreferences().putBoolean(USE_CUSTOM_DIR, false);
    }

    public static boolean useCustomDirectoryAsBaseDirectory() {
        return getPreferences().getBoolean(USE_CUSTOM_DIR, false);
    }

    /**
     * Use custom directory location to store index file.
     */
    public static void setCustomDirectoryAsBaseDirectory() {
        getPreferences().putBoolean(USE_CUSTOM_DIR, true);
        getPreferences().putBoolean(USE_TMP_DIR, false);
        getPreferences().putBoolean(USE_HOME_DIR, false);
    }

    /**
     * Get the <code>BASE_DIR</code> value.
     *
     * @return The base directory location.
     */
    public static String getBaseDirectory() {
        return getPreferences().getString(BASE_DIR, getDefaultBaseDirectory());
    }

    /**
     * Set the root directory for storing the index in the file system.
     *
     * @param baseDirectory
     *          A directory location set by the user.
     */
    public static void setBaseDirectory(String baseDirectory) {
        logger.info("Base index directory set to {}", baseDirectory);
        getPreferences().putString(BASE_DIR, prepare(baseDirectory));
    }

    /**
     * Construct a full index path location given the ontology object.
     *
     * @param ontology
     *          An OWL ontology object
     * @return A full path location of the index directory.
     */
    public static String createIndexLocation(OWLOntology ontology) {
        String directoryName = createUniqueName(ontology.getOntologyID());
        String directoryPath = getBaseDirectory() + fsSeparator + directoryName;
        logger.info("Created new index directory at " + directoryPath);
        getPreferences().putString(getLocationKey(ontology), directoryPath);
        return directoryPath;
    }

    public static String getIndexLocation(OWLOntology ontology) {
        Optional<String> location = getPreferenceValue(getLocationKey(ontology));
        if (location.isPresent()) {
            String cachedLocation = location.get();
            /*
             * Make sure the index location still exists.
             */
            if (new File(cachedLocation).exists()) {
                /*
                 * If does exist, check if the hash signature between the index and the
                 * current ontology is the same.
                 */
                Optional<String> hashInfo = getPreferenceValue(getHashKey(ontology));
                if (hashInfo.isPresent() && !hashInfo.get().equals(ontology.hashCode()+"")) {
                    logger.info("Auto-remove obsolete index directory at " + cachedLocation);
                    removeIndexLocation(ontology);
                    return createIndexLocation(ontology);
                }
                else {
                    logger.info("Loading index from " + cachedLocation);
                    return cachedLocation;
                }
            }
        }
        return createIndexLocation(ontology);
    }

    public static void removeIndexLocation(OWLOntology ontology) {
        try {
            Optional<String> location = getPreferenceValue(getLocationKey(ontology));
            if (location.isPresent()) {
                getPreferences().putString(getLocationKey(ontology), null); // remove preference by set null value
                getPreferences().putString(getHashKey(ontology), null);
                FileUtils.deleteDirectory(new File(location.get()));
            }
        }
        catch (IOException e) {
            logger.error("Error while removing index directory", e);
        }
    }

    public static void setIndexSnapshot(OWLOntology ontology) {
        getPreferences().putString(getHashKey(ontology), ontology.hashCode()+"");
    }

    /**
     * Clear all ontology versions and the associated index location from the preference.
     * Note that no physical files or folders are deleted from the file system.
     */
    public static void clear() {
        Preferences p = getPreferences();
        p.clear();
        p.putString(BASE_DIR, getBaseDirectory());
    }

    /*
     * Private utility methods
     */

    private static String getLocationKey(OWLOntology ontology) {
        return ontology.getOntologyID().getDefaultDocumentIRI().get().toString();
    }

    private static String getHashKey(OWLOntology ontology) {
        return "SIGN:" + ontology.getOntologyID().getDefaultDocumentIRI().get().toString();
    }

    private static String getDefaultBaseDirectory() {
        return getUserHomeDirectory();
    }

    private static Optional<String> getPreferenceValue(String key) {
        return Optional.ofNullable(getPreferences().getString(key, null));
    }

    private static String createUniqueName(OWLOntologyID ontologyId) {
        String ontologyIdHex = Integer.toHexString(ontologyId.toString().hashCode());
        String timestampHex = Integer.toHexString(new Date().hashCode());
        return String.format("%s-%s-%s", PREFIX_INDEX_DIR, ontologyIdHex, timestampHex);
    }
    /**
     * Make sure the path directory contains no file separator at the end of the string
     *
     * @param directory
     *          input path directory
     * @return clean path directory.
     */
    private static String prepare(String directory) {
        String systemFileSeparator = System.getProperty("file.separator");
        if (directory.endsWith(systemFileSeparator)) {
            directory = directory.substring(0, directory.length()-1);
        }
        return directory;
    }
}
