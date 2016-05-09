package kr.co.company.musiclight;

import android.R.string;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Play extends Activity{
	MediaPlayer mp = null;
	TextView v;
	String s;
	Button start;
	Button pause;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		Intent intent = getIntent();
		String path = intent.getExtras().getString("path");
		String fileName = intent.getExtras().getString("fileName");
		start = (Button) findViewById(R.id.start);
		pause = (Button) findViewById(R.id.pause);
		
		s= path+fileName;
		
		((TextView) findViewById(R.id.mypath)).setText("���: "+path+fileName);	
		
		start.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v){
				mp = new MediaPlayer();
				try{
				mp.setDataSource(s);
				mp.prepare();
				}
				catch(Exception e){
					e.printStackTrace();
				}
				mp.start();
			}
		});
			
		
		pause.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v){
				if(mp != null){
					mp.stop();
					mp.release();
				}
				mp=null;
			}
		});
		}
				
	

	
	
	
	
	public void stopFileAudio(View v){
		if(mp != null){
			mp.stop();
			mp.release();
			
		}
		mp=null;
	}

}
