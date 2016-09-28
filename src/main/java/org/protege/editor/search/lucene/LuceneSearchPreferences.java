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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 15/05/2016
 */
@Deprecated
public class LuceneSearchPreferences {

    private static final Logger logger = LoggerFactory.getLogger(LuceneSearchPreferences.class);

    public static final String PREFERENCES_KEY = "SearchIndexPreferences";

    public static final String BASE_DIR = "BASE_DIR";
    public static final String INDEXED_ONTOLOGY = "INDEXED_ONTOLOGY";
    public static final String MAX_SIZE_FOR_IN_MEMORY_STORING = "MAX_SIZE_FOR_IN_MEMORY_STORING";

    public static final String PREFIX_INDEX_DIR = "ProtegeIndex";

    public static final String USE_TMP_DIR = "UseTmpDirectory";
    public static final String USE_HOME_DIR = "UseHomeDirectory";
    public static final String USE_CUSTOM_DIR = "UseCustomDirectory";
    public static final String USE_IN_MEMORY_STORING = "UseInMemoryStoring";

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
        getPreferences().putString(BASE_DIR, prepare(baseDirectory));
    }

    /**
     * Returns a map of ontology id and the associated index location in the file system
     */
    public static Map<String, String> getIndexLocationMap() {
        Map<String, String> toReturn = new HashMap<>();
        for (String locationKey : getPreferences().getStringList(INDEXED_ONTOLOGY, new ArrayList<String>())) {
            Optional<String> value = getPreferenceValue(locationKey);
            if (value.isPresent()) {
                toReturn.put(locationKey, value.get());
            }
        }
        return toReturn;
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
        registerLocation(getLocationKey(ontology), directoryPath);
        return directoryPath;
    }

    private static void registerLocation(String locationKey, String directoryPath) {
        getPreferences().putString(locationKey, directoryPath);
        List<String> currentList = getPreferences().getStringList(INDEXED_ONTOLOGY, new ArrayList<String>());
        currentList.add(locationKey);
        getPreferences().putStringList(INDEXED_ONTOLOGY, currentList);
    }

    /**
     * Get the index directory path given the input ontology object.
     *
     * @param ontology
     *          An OWL ontology object
     * @return A full path location of the index directory
     */
    public static Optional<String> getIndexLocation(OWLOntology ontology) {
        return getPreferenceValue(getLocationKey(ontology));
    }

    /**
     * Find a valid index directory path for the given input ontology object. The method will
     * first check if the ontology has an index directory already and reuse it. Otherwise,
     * the method will return a newly created directory path.
     *
     * @param ontology
     *          An OWL ontology object
     * @return A full path location of the index directory
     */
    public static String findIndexLocation(OWLOntology ontology) {
        Optional<String> location = getIndexLocation(ontology);
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
        removeIndexLocation(getLocationKey(ontology));
    }

    public static void removeIndexLocation(String locationKey) {
        try {
            Optional<String> location = getPreferenceValue(locationKey);
            if (location.isPresent()) {
                unregisterLocation(locationKey);
                unsetIndexSnapshot(getHashKey(locationKey));
                File f = new File(location.get());
                if (f.exists() && f.isDirectory()) {
                    FileUtils.deleteDirectory(f);
                }
            }
        }
        catch (IOException e) {
            logger.error("Error while removing index directory", e);
        }
    }

    private static void unregisterLocation(String locationKey) {
        getPreferences().putString(locationKey, null); // remove preference by set null value
        List<String> currentList = getPreferences().getStringList(INDEXED_ONTOLOGY, new ArrayList<String>());
        currentList.remove(locationKey);
        getPreferences().putStringList(INDEXED_ONTOLOGY, currentList);
    }

    public static void setIndexSnapshot(OWLOntology ontology) {
        getPreferences().putString(getHashKey(ontology), ontology.hashCode()+"");
    }

    private static void unsetIndexSnapshot(String hashKey) {
        getPreferences().putString(hashKey, null);
    }

    public static boolean useInMemoryIndexStoring() {
        return getPreferences().getBoolean(USE_IN_MEMORY_STORING, false);
    }

    public static void setInMemoryIndexStoring(boolean useInMemory) {
        getPreferences().putBoolean(USE_IN_MEMORY_STORING, useInMemory);
    }

    public static void setMaxSizeForInMemoryIndexStoring(int fileSize) {
        getPreferences().putLong(MAX_SIZE_FOR_IN_MEMORY_STORING, fileSize);
    }

    public static int getMaxSizeForInMemoryIndexStoring() {
        return getPreferences().getInt(MAX_SIZE_FOR_IN_MEMORY_STORING, 20); // in megabyte
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
        return getHashKey(getLocationKey(ontology));
    }

    private static String getHashKey(String locationKey) {
        return "SIGN:" + locationKey;
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
