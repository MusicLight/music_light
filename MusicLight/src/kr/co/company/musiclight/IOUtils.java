package kr.co.company.musiclight;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public final class IOUtils
{
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public IOUtils() {}

    /**
     * Unconditionally close an Reader.
     * Equivalent to {@link Reader#close()}, except any exceptions will be ignored.
     *
     * @param input A (possibly null) Reader
     */
    public static void closeQuietly( Reader input )
    {
        if( input == null )
        {
            return;
        }

        try
        {
            input.close();
        }
        catch( IOException ioe )
        {
        }
    }

    /**
     * Unconditionally close an Writer.
     * Equivalent to {@link Writer#close()}, except any exceptions will be ignored.
     *
     * @param output A (possibly null) Writer
     */
    public static void closeQuietly( Writer output )
    {
        if( output == null )
        {
            return;
        }

        try
        {
            output.close();
        }
        catch( IOException ioe )
        {
        }
    }

    /**
     * Unconditionally close an OutputStream.
     * Equivalent to {@link OutputStream#close()}, except any exceptions will be ignored.
     * @param output A (possibly null) OutputStream
     */
    public static void closeQuietly( OutputStream output )
    {
        if( output == null )
        {
            return;
        }

        try
        {
            output.close();
        }
        catch( IOException ioe )
        {
        }
    }

    /**
     * Unconditionally close an InputStream.
     * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored.
     * @param input A (possibly null) InputStream
     */
    public static void closeQuietly( InputStream input )
    {
        if( input == null )
        {
            return;
        }

        try
        {
            input.close();
        }
        catch( IOException ioe )
        {
        }
    }

    /**
     * Get the contents of an InputStream as a String.
     * The platform's default encoding is used for the byte-to-char conversion.
     * @param input the InputStream to read from
     * @return the requested String
     * @throws IOException In case of an I/O problem
     */
    public static String toString( InputStream input )
        throws IOException
    {
        StringWriter sw = new StringWriter();
        CopyUtils.copy( input, sw );
        return sw.toString();
    }

    /**
     * Get the contents of an InputStream as a String.
     * @param input the InputStream to read from
     * @param encoding The name of a supported character encoding. See the
     *   IANA
     *   Charset Registry for a list of valid encoding types.
     * @return the requested String
     * @throws IOException In case of an I/O problem
     */
    public static String toString( InputStream input,
                                   String encoding )
        throws IOException
    {
        StringWriter sw = new StringWriter();
        CopyUtils.copy( input, sw, encoding );
        return sw.toString();
    }

    ///////////////////////////////////////////////////////////////
    // InputStream -> byte[]

    /**
     * Get the contents of an InputStream as a byte[].
     * @param input the InputStream to read from
     * @return the requested byte array
     * @throws IOException In case of an I/O problem
     */
    public static byte[] toByteArray( InputStream input )
        throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CopyUtils.copy( input, output );
        return output.toByteArray();
    }


    ///////////////////////////////////////////////////////////////
    // Derived copy methods
    // Reader -> *
    ///////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////
    // Reader -> String
    /**
     * Get the contents of a Reader as a String.
     * @param input the Reader to read from
     * @return the requested String
     * @throws IOException In case of an I/O problem
     */
    public static String toString( Reader input )
        throws IOException
    {
        StringWriter sw = new StringWriter();
        CopyUtils.copy( input, sw );
        return sw.toString();
    }


    ///////////////////////////////////////////////////////////////
    // Reader -> byte[]
    /**
     * Get the contents of a Reader as a byte[].
     * @param input the Reader to read from
     * @return the requested byte array
     * @throws IOException In case of an I/O problem
     */
    public static byte[] toByteArray( Reader input )
        throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CopyUtils.copy( input, output );
        return output.toByteArray();
    }


    ///////////////////////////////////////////////////////////////
    // Derived copy methods
    // String -> *
    ///////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////
    // String -> byte[]
    /**
     * Get the contents of a String as a byte[].
     * @param input the String to convert
     * @return the requested byte array
     * @throws IOException In case of an I/O problem
     */
    public static byte[] toByteArray( String input )
        throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CopyUtils.copy( input, output );
        return output.toByteArray();
    }


    ///////////////////////////////////////////////////////////////
    // Derived copy methods
    // byte[] -> *
    ///////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////
    // byte[] -> String

    /**
     * Get the contents of a byte[] as a String.
     * The platform's default encoding is used for the byte-to-char conversion.
     * @param input the byte array to read from
     * @return the requested String
     * @throws IOException In case of an I/O problem
     */
    public static String toString( byte[] input )
        throws IOException
    {
        StringWriter sw = new StringWriter();
        CopyUtils.copy( input, sw );
        return sw.toString();
    }


    /**
     * Get the contents of a byte[] as a String.
     * @param input the byte array to read from
     * @param encoding The name of a supported character encoding. See the
     *   IANA
     *   Charset Registry for a list of valid encoding types.
     * @return the requested String
     * @throws IOException In case of an I/O problem
     */
    public static String toString( byte[] input,
                                   String encoding )
        throws IOException
    {
        StringWriter sw = new StringWriter();
        CopyUtils.copy( input, sw, encoding );
        return sw.toString();
    }


    /**
     * Compare the contents of two Streams to determine if they are equal or not.
     *
     * @param input1 the first stream
     * @param input2 the second stream
     * @return true if the content of the streams are equal or they both don't exist, false otherwise
     * @throws IOException In case of an I/O problem
     */
    public static boolean contentEquals( InputStream input1,
                                         InputStream input2 )
        throws IOException
    {
        InputStream bufferedInput1 = new BufferedInputStream( input1 );
        InputStream bufferedInput2 = new BufferedInputStream( input2 );

        int ch = bufferedInput1.read();
        while( -1 != ch )
        {
            int ch2 = bufferedInput2.read();
            if( ch != ch2 )
            {
                return false;
            }
            ch = bufferedInput1.read();
        }

        int ch2 = bufferedInput2.read();
        if( -1 != ch2 )
        {
            return false;
        }
        else
        {
            return true;
        }
    }

	public static void safeClose(InputStream inputStream) {
		// TODO Auto-generated method stub
		
	}
}