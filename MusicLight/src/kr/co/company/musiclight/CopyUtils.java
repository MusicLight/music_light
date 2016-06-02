package kr.co.company.musiclight;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;

@Deprecated
public class CopyUtils {

    /**
     * The default size of the buffer.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public CopyUtils() { }

    // ----------------------------------------------------------------
    // byte[] -> OutputStream
    // ----------------------------------------------------------------

    /**
     * Copy bytes from a <code>byte[]</code> to an <code>OutputStream</code>.
     * @param input the byte array to read from
     * @param output the <code>OutputStream</code> to write to
     * @throws IOException In case of an I/O problem
     */
    public static void copy(final byte[] input, final OutputStream output)
            throws IOException {
        output.write(input);
    }

    // ----------------------------------------------------------------
    // byte[] -> Writer
    // ----------------------------------------------------------------

    /**
     * Copy and convert bytes from a <code>byte[]</code> to chars on a
     * <code>Writer</code>.
     * The platform's default encoding is used for the byte-to-char conversion.
     * @param input the byte array to read from
     * @param output the <code>Writer</code> to write to
     * @throws IOException In case of an I/O problem
     * @deprecated 2.5 use {@link #copy(byte[], Writer, String)} instead
     */
    @Deprecated
    public static void copy(final byte[] input, final Writer output)
            throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(input);
        copy(in, output);
    }


    /**
     * Copy and convert bytes from a <code>byte[]</code> to chars on a
     * <code>Writer</code>, using the specified encoding.
     * @param input the byte array to read from
     * @param output the <code>Writer</code> to write to
     * @param encoding The name of a supported character encoding. See the
     * <a href="http://www.iana.org/assignments/character-sets">IANA
     * Charset Registry</a> for a list of valid encoding types.
     * @throws IOException In case of an I/O problem
     */
    public static void copy(
            final byte[] input,
            final Writer output,
            final String encoding)
                throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(input);
        copy(in, output, encoding);
    }


    // ----------------------------------------------------------------
    // Core copy methods
    // ----------------------------------------------------------------

    /**
     * Copy bytes from an <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * @param input the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws IOException In case of an I/O problem
     */
    public static int copy(
            final InputStream input,
            final OutputStream output)
                throws IOException {
        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    // ----------------------------------------------------------------
    // Reader -> Writer
    // ----------------------------------------------------------------

    /**
     * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
     * @param input the <code>Reader</code> to read from
     * @param output the <code>Writer</code> to write to
     * @return the number of characters copied
     * @throws IOException In case of an I/O problem
     */
    public static int copy(
            final Reader input,
            final Writer output)
                throws IOException {
        final char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    // ----------------------------------------------------------------
    // InputStream -> Writer
    // ----------------------------------------------------------------

    /**
     * Copy and convert bytes from an <code>InputStream</code> to chars on a
     * <code>Writer</code>.
     * The platform's default encoding is used for the byte-to-char conversion.
     * @param input the <code>InputStream</code> to read from
     * @param output the <code>Writer</code> to write to
     * @throws IOException In case of an I/O problem
     * @deprecated 2.5 use {@link #copy(InputStream, Writer, String)} instead
     */
    @Deprecated
    public static void copy(
            final InputStream input,
            final Writer output)
                throws IOException {
        // make explicit the dependency on the default encoding
        final InputStreamReader in = new InputStreamReader(input, Charset.defaultCharset());
        copy(in, output);
    }

    /**
     * Copy and convert bytes from an <code>InputStream</code> to chars on a
     * <code>Writer</code>, using the specified encoding.
     * @param input the <code>InputStream</code> to read from
     * @param output the <code>Writer</code> to write to
     * @param encoding The name of a supported character encoding. See the
     * <a href="http://www.iana.org/assignments/character-sets">IANA
     * Charset Registry</a> for a list of valid encoding types.
     * @throws IOException In case of an I/O problem
     */
    public static void copy(
            final InputStream input,
            final Writer output,
            final String encoding)
                throws IOException {
        final InputStreamReader in = new InputStreamReader(input, encoding);
        copy(in, output);
    }


    // ----------------------------------------------------------------
    // Reader -> OutputStream
    // ----------------------------------------------------------------

    /**
     * Serialize chars from a <code>Reader</code> to bytes on an
     * <code>OutputStream</code>, and flush the <code>OutputStream</code>.
     * Uses the default platform encoding.
     * @param input the <code>Reader</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @throws IOException In case of an I/O problem
     * @deprecated 2.5 use {@link #copy(Reader, OutputStream, String)} instead
     */
    @Deprecated
    public static void copy(
            final Reader input,
            final OutputStream output)
                throws IOException {
        // make explicit the dependency on the default encoding
        final OutputStreamWriter out = new OutputStreamWriter(output, Charset.defaultCharset());
        copy(input, out);
        // XXX Unless anyone is planning on rewriting OutputStreamWriter, we
        // have to flush here.
        out.flush();
    }

    /**
     * Serialize chars from a <code>Reader</code> to bytes on an
     * <code>OutputStream</code>, and flush the <code>OutputStream</code>.
     * @param input the <code>Reader</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @param encoding The name of a supported character encoding. See the
     * <a href="http://www.iana.org/assignments/character-sets">IANA
     * Charset Registry</a> for a list of valid encoding types.
     * @throws IOException In case of an I/O problem
     * @since 2.5
     */
    public static void copy(
            final Reader input,
            final OutputStream output,
            final String encoding)
                throws IOException {
        final OutputStreamWriter out = new OutputStreamWriter(output, encoding);
        copy(input, out);
        // XXX Unless anyone is planning on rewriting OutputStreamWriter, we
        // have to flush here.
        out.flush();
    }

    // ----------------------------------------------------------------
    // String -> OutputStream
    // ----------------------------------------------------------------

    /**
     * Serialize chars from a <code>String</code> to bytes on an
     * <code>OutputStream</code>, and
     * flush the <code>OutputStream</code>.
     * Uses the platform default encoding.
     * @param input the <code>String</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @throws IOException In case of an I/O problem
     * @deprecated 2.5 use {@link #copy(String, OutputStream, String)} instead
     */
    @Deprecated
    public static void copy(
            final String input,
            final OutputStream output)
                throws IOException {
        final StringReader in = new StringReader(input);
        // make explicit the dependency on the default encoding
        final OutputStreamWriter out = new OutputStreamWriter(output, Charset.defaultCharset());
        copy(in, out);
        // XXX Unless anyone is planning on rewriting OutputStreamWriter, we
        // have to flush here.
        out.flush();
    }

    /**
     * Serialize chars from a <code>String</code> to bytes on an
     * <code>OutputStream</code>, and
     * flush the <code>OutputStream</code>.
     * @param input the <code>String</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @param encoding The name of a supported character encoding. See the
     * <a href="http://www.iana.org/assignments/character-sets">IANA
     * Charset Registry</a> for a list of valid encoding types.
     * @throws IOException In case of an I/O problem
     * @since 2.5
     */
    public static void copy(
            final String input,
            final OutputStream output,
            final String encoding)
                throws IOException {
        final StringReader in = new StringReader(input);
        final OutputStreamWriter out = new OutputStreamWriter(output, encoding);
        copy(in, out);
        // XXX Unless anyone is planning on rewriting OutputStreamWriter, we
        // have to flush here.
        out.flush();
    }

    // ----------------------------------------------------------------
    // String -> Writer
    // ----------------------------------------------------------------

    /**
     * Copy chars from a <code>String</code> to a <code>Writer</code>.
     * @param input the <code>String</code> to read from
     * @param output the <code>Writer</code> to write to
     * @throws IOException In case of an I/O problem
     */
    public static void copy(final String input, final Writer output)
                throws IOException {
        output.write(input);
    }

}