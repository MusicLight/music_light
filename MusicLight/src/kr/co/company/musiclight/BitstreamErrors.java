package kr.co.company.musiclight;

public interface BitstreamErrors extends JavaLayerErrors
{
	
	/**
	 * An undeterminable error occurred. 
	 */
	static public final int UNKNOWN_ERROR = BITSTREAM_ERROR + 0;
	
	/**
	 * The header describes an unknown sample rate.
	 */
	static public final int UNKNOWN_SAMPLE_RATE = BITSTREAM_ERROR + 1;

	/**
	 * A problem occurred reading from the stream.
	 */
	static public final int STREAM_ERROR = BITSTREAM_ERROR + 2;
	
	/**
	 * The end of the stream was reached prematurely. 
	 */
	static public final int UNEXPECTED_EOF = BITSTREAM_ERROR + 3;
	
	/**
	 * The end of the stream was reached. 
	 */
	static public final int STREAM_EOF = BITSTREAM_ERROR + 4;
	
	/**
	 * Frame data are missing. 
	 */
	static public final int INVALIDFRAME = BITSTREAM_ERROR + 5;

	/**
	 * 
	 */
	static public final int BITSTREAM_LAST = 0x1ff;
	
}