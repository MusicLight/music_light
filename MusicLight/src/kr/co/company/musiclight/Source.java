package kr.co.company.musiclight;

import java.io.IOException;

/**
 * Work in progress.
 * 
 * Class to describe a seekable data source. 
 *  
 */
public interface Source
{
	
	public static final long	LENGTH_UNKNOWN = -1;
	
	public int read(byte[] b, int offs, int len)
		throws IOException;
	
	
	public boolean	willReadBlock();
			
	public boolean	isSeekable();
		
	public long		length();
	
	public long		tell();
	
	public long		seek(long pos);
	
}
