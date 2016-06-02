package kr.co.company.musiclight;

public class BitstreamException extends JavaLayerException
implements BitstreamErrors
{	
private int errorcode = UNKNOWN_ERROR;

public BitstreamException(String msg, Throwable t)
{
	super(msg, t);	
}

public BitstreamException(int errorcode, Throwable t)
{
	this(getErrorString(errorcode), t);
	this.errorcode = errorcode;
}

public int getErrorCode()
{
	return errorcode;	
}


static public String getErrorString(int errorcode)
{
	// REVIEW: use resource bundle to map error codes
	// to locale-sensitive strings.
	
	return "Bitstream errorcode "+Integer.toHexString(errorcode);
}


}