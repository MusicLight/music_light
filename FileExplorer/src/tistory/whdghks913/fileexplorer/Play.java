package tistory.whdghks913.fileexplorer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;

public class Play extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
	}
	MediaPlayer mp = null;
	
	/*
	public void startFileAudio(View v, String path, String fileName){
		String file;
		
		file =path+fileName;
		mp= new MediaPlayer();
		try{
			mp.setDataSource(file);
			mp.prepare();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		mp.start();
	}
	
	public void stopFileAudio(View v){
		if(mp != null){
			mp.stop();
			mp.release();
			
		}
		mp=null;
	}
*/
}
