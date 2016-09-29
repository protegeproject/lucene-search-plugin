package org.protege.editor.search.lucene;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;

import org.semanticweb.binaryowl.owlobject.OWLObjectBinaryType;
import org.semanticweb.binaryowl.stream.BinaryOWLOutputStream;
import org.semanticweb.binaryowl.stream.TreeSetTransformer;
import org.semanticweb.owlapi.model.OWLAxiom;

import com.google.common.base.MoreObjects;

/**
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 15/08/2014
 *
 * Represents the message digest of a knowledge base (set of axioms).
 */
public class KbDigest {

    private static final int INITIAL_BUFFER_SIZE = 20 * 1024 * 1204;

    private static final String DIGEST_TYPE = "SHA1";

    final private static char[] hexArray = "0123456789abcdef".toCharArray();

    private static final KbDigest EMPTY_DIGEST = getDigest(Collections.<OWLAxiom>emptyList());

    private static final int DIGEST_LENGTH = 20;

    private byte[] digest;

    private KbDigest(byte[] digest) {
        this.digest = digest;
    }

    /**
     * Gets the digest of the empty knowledge base.
     * @return A KbDigest representing the digest of the empty knowledge base.  Not {@code null}.
     */
    public static KbDigest emptyDigest() {
        return EMPTY_DIGEST;
    }

    /**
     * Gets the digest of the specified axioms.  Note that the digest is sensitive to the order of the axioms.
     * @param axioms The axioms. Not {@code null}.
     * @return The KbDigest of the axioms.  Not {@code null}.
     */
    public static KbDigest getDigest(Iterable<? extends OWLAxiom> axioms) {
        checkNotNull(axioms);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(INITIAL_BUFFER_SIZE);
            BinaryOWLOutputStream boos = new BinaryOWLOutputStream(new DataOutputStream(bos), new TreeSetTransformer());
            for (OWLAxiom ax : axioms) {
                OWLObjectBinaryType.write(ax, boos);
            }
            boos.flush();
            MessageDigest sha1 = MessageDigest.getInstance(DIGEST_TYPE);
            byte[] input = bos.toByteArray();
            sha1.update(input);
            byte[] digest = sha1.digest();
            return new KbDigest(digest);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error whilst computing axiom digest: " + e.getMessage());
        }
    }

    /**
     * Gets the digest corresponding to the specified array of bytes.  The digest representation must be a byte
     * array that is exactly 20 bytes in length.
     * @param bytes The bytes that represent a digest.  Not {@code null}.  Must be 20 bytes long.
     * @return The digest corresponding to the bytes.  Not {@code null}.
     * @throws NullPointerException if {@code bytes} is {@code null}.
     * @throws IllegalArgumentException if {@code bytes} is not exactly 20 in length.
     * @see {@link #toByteArray()}.
     */
    public static KbDigest fromByteArray(byte[] bytes) {
        checkNotNull(bytes);
        checkArgument(bytes.length == DIGEST_LENGTH);
        byte [] copy = new byte[bytes.length];
        System.arraycopy(bytes, 0, copy, 0, bytes.length);
        return new KbDigest(copy);
    }

    /**
     * Gets the digest as a byte array.
     * @return The digest.  Not {@code null}.  The length of the byte array will be 20.
     * @see {@link #fromByteArray(byte[])}.
     */
    public byte[] toByteArray() {
        return Arrays.copyOf(digest, digest.length);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("KbDigest")
                .addValue(toHex(digest))
                .toString();
    }

    @Override
    public int hashCode() {
        return "KbDigest".hashCode() + Arrays.hashCode(digest);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof KbDigest)) {
            return false;
        }
        KbDigest other = (KbDigest) o;
        return Arrays.equals(this.digest, other.digest);
    }


    // Pinched from http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java

    private static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
