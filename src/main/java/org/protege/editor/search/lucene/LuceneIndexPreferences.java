package org.protege.editor.search.lucene;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 */
public class LuceneIndexPreferences {

    private static final Logger logger = LoggerFactory.getLogger(LuceneIndexPreferences.class);

    private static final int ONTOLOGY_IRI_ATTRIBUTE = 0;
    private static final int INDEX_DIRECTORY_ATTRIBUTE = 1;
    private static final int INDEX_CHECKSUM_ATTRIBUTE = 2;

    public static final String LUCENE_INDEX_PREFERENCES_KEY = "LuceneIndexPreferences";

    public static final String BASE_DIR = "BASE_DIR";
    public static final String INDEX_RECORD_KEYS = "INDEX_RECORD_KEYS";
    public static final String MAX_SIZE_FOR_IN_MEMORY_STORING = "MAX_SIZE_FOR_IN_MEMORY_STORING";

    public static final String PREFIX_INDEX_DIR = "ProtegeIndex";

    public static final String USE_TMP_DIR = "UseTmpDirectory";
    public static final String USE_HOME_DIR = "UseHomeDirectory";
    public static final String USE_CUSTOM_DIR = "UseCustomDirectory";
    public static final String USE_IN_MEMORY_STORING = "UseInMemoryStoring";

    public static final String PROTEGE_DIR = ".Protege";

    public static final String COLLECTOR_DIR = "lucene";

    private static String fileSystemSeparator = System.getProperty("file.separator");

    private static Preferences getPreferences() {
        return PreferencesManager.getInstance().getApplicationPreferences(LUCENE_INDEX_PREFERENCES_KEY);
    }

    /**
     * Gets the system temporary directory path.
     *
     * @return Returns the system temporary directory path.
     */
    @Nonnull
    public static String getTempDirectory() {
        return normalizePathName(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Gets the user home directory path.
     *
     * @return Returns the user home directory path.
     */
    @Nonnull
    public static String getUserHomeDirectory() {
        String homeDir = normalizePathName(System.getProperty("user.home"));
        return homeDir + fileSystemSeparator + PROTEGE_DIR + fileSystemSeparator + COLLECTOR_DIR;
    }

    public static boolean useTempDirectoryAsBaseDirectory() {
        return getPreferences().getBoolean(USE_TMP_DIR, false);
    }

    /**
     * Sets to use the system temporary directory to store the index files.
     */
    public static void setTempDirectoryAsBaseDirectory() {
        getPreferences().putBoolean(USE_TMP_DIR, true);
        getPreferences().putBoolean(USE_HOME_DIR, false);
        getPreferences().putBoolean(USE_CUSTOM_DIR, false);
    }

    /**
     * Checks if the index files are stored in the user home directory.
     *
     * @return Returns <code>true</code> if using the user home directory,
     * or <code>false</code> otherwise.
     */
    public static boolean useUserHomeDirectoryAsBaseDirectory() {
        return getPreferences().getBoolean(USE_HOME_DIR, true);
    }

    /**
     * Sets to use the user home directory to store the index files.
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
     * Checks if the index files are stored in the memory, instead of in the
     * file system.
     *
     * @return Returns <code>true</code> id the index files are stored in the
     * memory, or <code>false</code> otherwise.
     */
    public static boolean useInMemoryIndexStoring() {
        return getPreferences().getBoolean(USE_IN_MEMORY_STORING, false);
    }

    /**
     * Sets to use the memory to store the index files.
     *
     * @param useInMemory
     */
    public static void setInMemoryIndexStoring(boolean useInMemory) {
        getPreferences().putBoolean(USE_IN_MEMORY_STORING, useInMemory);
    }

    /**
     * Sets the maximum size of the ontology file for storing the index files
     * in the memory.
     *
     * @param fileSize
     *          The file size in megabytes
     */
    public static void setMaxSizeForInMemoryIndexStoring(int fileSize) {
        getPreferences().putLong(MAX_SIZE_FOR_IN_MEMORY_STORING, fileSize);
    }

    /**
     * Gets the maximum size of the ontology file for storing the index files
     * in the memory.
     *
     * @return The file size in megabytes.
     */
    public static int getMaxSizeForInMemoryIndexStoring() {
        return getPreferences().getInt(MAX_SIZE_FOR_IN_MEMORY_STORING, 20);
    }

    /**
     * Sets to use a custom directory location to store the index files.
     */
    public static void setCustomDirectoryAsBaseDirectory() {
        getPreferences().putBoolean(USE_CUSTOM_DIR, true);
        getPreferences().putBoolean(USE_TMP_DIR, false);
        getPreferences().putBoolean(USE_HOME_DIR, false);
    }

    /**
     * Gets the base directory to store the index files. By default, the base
     * directory is the user home directory.
     *
     * @return Returns the base directory location.
     */
    @Nonnull
    public static String getBaseDirectory() {
        return getPreferences().getString(BASE_DIR, getUserHomeDirectory());
    }

    /**
     * Sets the base directory to store the index files.
     *
     * @param baseDirectory
     *          A directory location set by the user.
     */
    public static void setBaseDirectory(@Nonnull String baseDirectory) {
        checkNotNull(baseDirectory);
        getPreferences().putString(BASE_DIR, normalizePathName(baseDirectory));
    }

    /**
     * Adds an index record to the index preference given the input ontology.
     * The record will contain the information about the ontology IRI, the index
     * directory location and the index checksum. This method will do nothing if
     * the input is an anonymous ontology.
     *
     * @param ontology
     *            The input ontology to create the index record
     */
    public static void addIndexRecord(@Nonnull OWLOntology ontology) {
        checkNotNull(ontology);
        if (!ontology.isAnonymous()) {
            IRI ontologyIri = ontology.getOntologyID().getOntologyIRI().get();
            final String indexRecordKey = createIndexRecordKey(ontologyIri);
            getPreferences().putStringList(indexRecordKey, createIndexAttributes(ontology));
            collectIndexRecordKey(indexRecordKey);
        }
    }

    /**
     * Removes the index record given the ontology IRI. This method will also
     * remove the index directory from the file system.
     *
     * @param ontologyIri
     *          The input ontology IRI that identifies the index record.
     */
    public static void removeIndexRecord(@Nonnull IRI ontologyIri) {
        checkNotNull(ontologyIri);
        final String indexRecordKey = createIndexRecordKey(ontologyIri);
        deleteIndexDirectory(indexRecordKey);
        getPreferences().putString(indexRecordKey, null);
        discardIndexRecordKey(indexRecordKey);
    }

    /**
     * Checks if the index preference contains the index record for the given
     * input ontology. When the method finds an index record associated with
     * the input ontology, it will check if the index directory location is
     * still valid. The method will check as well if the index checksum agrees
     * with the input ontology such that the index record is still valid.
     * 
     * This method will always return <code>false</code> if the input is an
     * anonymous ontology.
     *
     * @param ontology
     *          The input ontology
     * @return Returns <code>true</code> if the index record is still relevant
     * with the given input ontology, or <code>false</code> otherwise.
     */
    public static boolean containsIndexRecord(@Nonnull OWLOntology ontology) {
        checkNotNull(ontology);
        if (ontology.isAnonymous()) {
            return false;
        }
        boolean doesContain = true;
        IRI ontologyIri = ontology.getOntologyID().getOntologyIRI().get();
        String indexRecordKey = createIndexRecordKey(ontologyIri);
        Optional<List<String>> indexRecord = getIndexRecord(indexRecordKey);
        if (indexRecord.isPresent()) {
            String directoryLocation = getIndexDirectoryLocation(ontologyIri);
            if (doesDirectoryExist(directoryLocation)) {
                String recordedIndexChecksum = getIndexChecksum(ontologyIri);
                String currentIndexChecksum = createIndexChecksum(ontology);
                if (!recordedIndexChecksum.equals(currentIndexChecksum)) {
                    doesContain = false;
                }
            } else {
                doesContain = false;
            }
        } else {
            doesContain = false;
        }
        return doesContain;
    }

    private static boolean doesDirectoryExist(String directoryLocation) {
        if (directoryLocation == null) {
            return false;
        }
        if (directoryLocation.isEmpty()) {
            return false;
        }
        return new File(directoryLocation).exists();
    }

    /**
     * Returns the index record table. The table consists of the record key, the ontology IRI,
     * the index directory location and the index checksum.
     *
     * @return A index record table.
     */
    @Nonnull
    public static List<List<String>> getIndexRecordTable() {
        List<List<String>> indexRecordTable = new ArrayList<>();
        List<String> indexMapKeys = getPreferences().getStringList(INDEX_RECORD_KEYS, new ArrayList<>());
        for (String indexMapKey : indexMapKeys) {
            List<String> indexRecord = new ArrayList<>();
            indexRecord.add(indexMapKey);
            indexRecord.addAll(getCheckedIndexRecord(indexMapKey));
            indexRecordTable.add(indexRecord);
        }
        return indexRecordTable;
    }

    /**
     * Gets the index directory path given the input ontology IRI.
     *
     * @param ontologyIri
     *          The input ontology IRI that identifies the index directory path.
     * @return A full path location of the index directory
     */
    @Nonnull
    public static String getIndexDirectoryLocation(@Nonnull IRI ontologyIri) {
        checkNotNull(ontologyIri);
        String indexMapKey = createIndexRecordKey(ontologyIri);
        List<String> indexAttributes = getCheckedIndexRecord(indexMapKey);
        return indexAttributes.get(INDEX_DIRECTORY_ATTRIBUTE);
    }

    /**
     * Gets the index checksum given the input ontology IRI
     *
     * @param ontologyIri
     *          The input ontology IRI that identifies the index checksum.
     * @return The checksum string of the index
     */
    @Nonnull
    public static String getIndexChecksum(@Nonnull IRI ontologyIri) {
        checkNotNull(ontologyIri);
        String indexRecordKey = createIndexRecordKey(ontologyIri);
        List<String> indexAttributes = getCheckedIndexRecord(indexRecordKey);
        return indexAttributes.get(INDEX_CHECKSUM_ATTRIBUTE);
    }

    /**
     * Updates the index checksum in the index record. The method will recompute
     * the index checksum based on the given ontology and update the index
     * record value.
     *
     * @param ontology
     *            The input ontology to compute the checksum.
     */
    public static void updateIndexChecksum(@Nonnull OWLOntology ontology) {
        checkNotNull(ontology);
        if (!ontology.isAnonymous()) {
            IRI ontologyIri = ontology.getOntologyID().getOntologyIRI().get();
            final String indexRecordKey = createIndexRecordKey(ontologyIri);
            List<String> indexAttributes = getCheckedIndexRecord(indexRecordKey);
            indexAttributes.set(INDEX_CHECKSUM_ATTRIBUTE, createIndexChecksum(ontology));
            getPreferences().putStringList(indexRecordKey, indexAttributes);
        }
    }

    /**
     * Clears all ontology versions and the associated index location from the preference.
     * Note that no physical files or folders are deleted from the file system.
     */
    public static void clear() {
        Preferences preferences = getPreferences();
        preferences.clear();
        preferences.putString(BASE_DIR, getBaseDirectory());
    }

    /*
     * Private utility methods
     */

    private static Optional<List<String>> getIndexRecord(String indexRecordKey) {
        return Optional.ofNullable(getPreferences().getStringList(indexRecordKey, null));
    }

    private static List<String> getCheckedIndexRecord(String indexRecordKey) {
        Optional<List<String>> uncheckedIndexRecord = getIndexRecord(indexRecordKey);
        List<String> indexRecord = new ArrayList<>();
        if (!uncheckedIndexRecord.isPresent()) {
            indexRecord.add(ONTOLOGY_IRI_ATTRIBUTE, "");
            indexRecord.add(INDEX_DIRECTORY_ATTRIBUTE, "");
            indexRecord.add(INDEX_CHECKSUM_ATTRIBUTE, "");
        } else {
            indexRecord.addAll(uncheckedIndexRecord.get());
        }
        return indexRecord;
    }

    private static String createIndexRecordKey(IRI ontologyIri) {
        return "KEY:" + ontologyIri.hashCode();
    }

    private static List<String> createIndexAttributes(OWLOntology ontology) {
        List<String> indexAttributes = new ArrayList<>();
        indexAttributes.add(ONTOLOGY_IRI_ATTRIBUTE, fetchOntologyIri(ontology));
        indexAttributes.add(INDEX_DIRECTORY_ATTRIBUTE, createIndexDirectoryLocation(ontology));
        indexAttributes.add(INDEX_CHECKSUM_ATTRIBUTE, createIndexChecksum(ontology));
        return indexAttributes;
    }

    private static String fetchOntologyIri(OWLOntology ontology) {
        return ontology.getOntologyID().getOntologyIRI().get().toString();
    }

    private static String createIndexDirectoryLocation(OWLOntology ontology) {
        StringBuffer directoryLocation = new StringBuffer();
        directoryLocation.append(getBaseDirectory());
        directoryLocation.append(fileSystemSeparator);
        directoryLocation.append(createDirectoryName(ontology.getOntologyID()));
        return directoryLocation.toString();
    }

    private static String createIndexChecksum(OWLOntology ontology) {
        List<OWLAxiom> sortedAxioms = new ArrayList<>(ontology.getAxioms());
        Collections.sort(sortedAxioms);
        KbDigest digest = KbDigest.getDigest(sortedAxioms);
        return Hex.encodeHexString(digest.toByteArray());
    }

    private static void collectIndexRecordKey(String indexMapKey) {
        List<String> indexRecordKeys = getPreferences().getStringList(
                INDEX_RECORD_KEYS,
                new ArrayList<>());
        if (!indexRecordKeys.contains(indexMapKey)) { // to prevent duplication in the list
            indexRecordKeys.add(indexMapKey);
        }
        getPreferences().putStringList(INDEX_RECORD_KEYS, indexRecordKeys);
    }

    private static void discardIndexRecordKey(String indexMapKey) {
        List<String> indexMapKeys = getPreferences().getStringList(
                INDEX_RECORD_KEYS,
                new ArrayList<>());
        indexMapKeys.remove(indexMapKey);
        getPreferences().putStringList(INDEX_RECORD_KEYS, indexMapKeys);
    }

    private static String createDirectoryName(OWLOntologyID ontologyId) {
        String ontologyIdHex = Integer.toHexString(ontologyId.hashCode());
        String timestampHex = Integer.toHexString(new Date().hashCode());
        return String.format("%s-%s-%s", PREFIX_INDEX_DIR, ontologyIdHex, timestampHex);
    }

    private static void deleteIndexDirectory(String indexMapKey) {
        List<String> indexAttributes = getCheckedIndexRecord(indexMapKey);
        String directoryLocation = indexAttributes.get(INDEX_DIRECTORY_ATTRIBUTE);
        try {
            File indexDirectory = new File(directoryLocation);
            if (indexDirectory.exists()) {
                FileUtils.deleteDirectory(indexDirectory);
            }
        } catch (IOException e) {
            logger.error("Error while removing index directory: " + directoryLocation, e);
        }
    }

    /**
     * Make sure the path directory contains no file separator at the end of the string
     *
     * @param directory
     *          input path directory
     * @return clean path directory.
     */
    private static String normalizePathName(String directory) {
        if (directory.endsWith(fileSystemSeparator)) {
            directory = directory.substring(0, directory.length()-1);
        }
        return directory;
    }
}
