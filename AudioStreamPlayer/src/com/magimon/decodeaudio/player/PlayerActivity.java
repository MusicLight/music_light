package com.magimon.decodeaudio.player;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.magimon.decodeaudio.player.AudioStreamPlayer.State;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayerActivity extends Activity implements OnAudioStreamInterface, OnSeekBarChangeListener, OnClickListener
{

	private Button mPlayButton = null;
	private Button mStopButton = null;

	private TextView mTextCurrentTime = null;
	private TextView mTextDuration = null;

	private SeekBar mSeekProgress = null;

	private ProgressDialog mProgressDialog = null;

	AudioStreamPlayer mAudioPlayer = new AudioStreamPlayer();
	 int frequency = 4000;
	 int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	 int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	 private RealDoubleFFT transformer;
	 int blockSize = 256;
	 boolean started = false;
	 ImageView imageView;
	 Bitmap bitmap;
	 Canvas canvas;
	 Paint paint;
	 
	 byte buf[]= mAudioPlayer.FFTFile();
	  
	
	
	 
	 TextView t;
	 FFTAudio audiotask;
	 
	 

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio_stream_player);

		mPlayButton = (Button) this.findViewById(R.id.button_play);
		mPlayButton.setOnClickListener(this);
		mStopButton = (Button) this.findViewById(R.id.button_stop);
		mStopButton.setOnClickListener(this);

		mTextCurrentTime = (TextView) findViewById(R.id.text_pos);
		mTextDuration = (TextView) findViewById(R.id.text_duration);

		mSeekProgress = (SeekBar) findViewById(R.id.seek_progress);
		mSeekProgress.setOnSeekBarChangeListener(this);
		mSeekProgress.setMax(0);
		mSeekProgress.setProgress(0);

		updatePlayer(State.Stopped);
		
		imageView = (ImageView)findViewById(R.id.ImageView01);
        bitmap = Bitmap.createBitmap((int)256, (int)100, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        imageView.setImageBitmap(bitmap);
      
        
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		stop();
	}

	private void updatePlayer(AudioStreamPlayer.State state)
	{
		switch (state)
		{
		case Stopped:
		{
			if (mProgressDialog != null)
			{
				mProgressDialog.cancel();
				mProgressDialog.dismiss();

				mProgressDialog = null;
			}
			mPlayButton.setSelected(false);
			mPlayButton.setText("Play");

			mTextCurrentTime.setText("00:00");
			mTextDuration.setText("00:00");

			mSeekProgress.setMax(0);
			mSeekProgress.setProgress(0);

			break;
		}
		case Prepare:
		case Buffering:
		{
			if (mProgressDialog == null)
			{
				mProgressDialog = new ProgressDialog(this);
			}
			mProgressDialog.show();

			mPlayButton.setSelected(false);
			mPlayButton.setText("Play");

			mTextCurrentTime.setText("00:00");
			mTextDuration.setText("00:00");
			break;
		}
		case Pause:
		{
			break;
		}
		case Playing:
		{
			if (mProgressDialog != null)
			{
				mProgressDialog.cancel();
				mProgressDialog.dismiss();

				mProgressDialog = null;
			}
			mPlayButton.setSelected(true);
			mPlayButton.setText("Pause");
			break;
		}
		}
	}

	private void pause()
	{
		if (this.mAudioPlayer != null)
		{
			this.mAudioPlayer.pause();
		}
	}

	private void play()
	{
		releaseAudioPlayer();

		mAudioPlayer = new AudioStreamPlayer();
		mAudioPlayer.setOnAudioStreamInterface(this);

		mAudioPlayer.setUrlString("/storage/emulated/0/Music/aaa.mp3");

		try
		{
			mAudioPlayer.play();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void releaseAudioPlayer()
	{
		if (mAudioPlayer != null)
		{
			mAudioPlayer.stop();
			mAudioPlayer.release();
			mAudioPlayer = null;

		}
	}

	private void stop()
	{
		if (this.mAudioPlayer != null)
		{
			this.mAudioPlayer.stop();
		}
	}

	@Override
	public void onAudioPlayerStart(AudioStreamPlayer player)
	{
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				updatePlayer(State.Playing);
			}
		});
	}

	@Override
	public void onAudioPlayerStop(AudioStreamPlayer player)
	{
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				updatePlayer(State.Stopped);
			}
		});

	}

	@Override
	public void onAudioPlayerError(AudioStreamPlayer player)
	{
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				updatePlayer(State.Stopped);
			}
		});

	}

	@Override
	public void onAudioPlayerBuffering(AudioStreamPlayer player)
	{
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				updatePlayer(State.Buffering);
			}
		});

	}

	@Override
	public void onAudioPlayerDuration(final int totalSec)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (totalSec > 0)
				{
					int min = totalSec / 60;
					int sec = totalSec % 60;

					mTextDuration.setText(String.format("%02d:%02d", min, sec));

					mSeekProgress.setMax(totalSec);
				}
			}

		});
	}

	@Override
	public void onAudioPlayerCurrentTime(final int sec)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (!isSeekBarTouch)
				{
					int m = sec / 60;
					int s = sec % 60;

					mTextCurrentTime.setText(String.format("%02d:%02d", m, s));

					mSeekProgress.setProgress(sec);
				}
			}
		});
	}

	@Override
	public void onAudioPlayerPause(AudioStreamPlayer player)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mPlayButton.setText("Play");
			}
		});
	}

	private boolean isSeekBarTouch = false;

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		this.isSeekBarTouch = true;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		this.isSeekBarTouch = false;

		int progress = seekBar.getProgress();

		this.mAudioPlayer.seekTo(progress);
	}
	
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.button_play:
		{
			if (mPlayButton.isSelected())
			{
				if (mAudioPlayer != null && mAudioPlayer.getState() == State.Pause)
				{
					mAudioPlayer.pauseToPlay();
				}
				else
				{
					pause();
				}
			}
			else
			{
				play();
			}
			break;
		}
		case R.id.button_stop:
		{
			stop();
			break;
		}
		}
	}

	
	private	class FFTAudio extends AsyncTask<Void, double[], Void>{
	    @Override
	    protected Void doInBackground(Void... params) {
	    	double[] toTransform = new double[blockSize];
	    	for(int i = 0; i < blockSize ; i++){
	    	    toTransform[i] = (double)buf[i] / Short.MAX_VALUE; // 부호 있는 16비트
	    	    }
	    
		    	
	    
        
        transformer.ft(toTransform);
        // publishProgress를 호출하면 onProgressUpdate가 호출된다.
        publishProgress(toTransform);
	   	
        
	    
	    
	    return null;
	    }
	
	
	@Override
	public void onProgressUpdate(double[]... toTransform) {
	    canvas.drawColor(Color.BLACK);
	   
	    for(int i = 0; i < toTransform[0].length; i++){
	    int x = i;
	    int downy = (int) (100 - (toTransform[0][i] * 10));
	    int upy = 100;
	   
	    canvas.drawLine(x, downy, x, upy, paint);
	    }
	    imageView.invalidate();
	    
	    
	    }
	
	public void onClick(View arg0) {
		if(started){
		    started = false;
		    audiotask.cancel(true);
		    }else{
		    started = true;
		    audiotask = new FFTAudio();
		    audiotask.execute();
	}
	
	
	
	
	

	
}


	
	
}


}
	
	
