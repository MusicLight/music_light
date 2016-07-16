package com.magimon.decodeaudio.player;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.magimon.decodeaudio.player.RealDoubleFFT;
import com.magimon.decoceaudio.decoder.AudioStreamPlayer;
import com.magimon.decoceaudio.decoder.AudioStreamPlayer.State;
import com.magimon.decoceaudio.decoder.OnAudioStreamInterface;

public class PlayerActivity extends Activity implements OnAudioStreamInterface, OnSeekBarChangeListener, OnClickListener
{

	private Button mPlayButton = null;
	private Button mStopButton = null;

	private TextView mTextCurrentTime = null;
	private TextView mTextDuration = null;

	private SeekBar mSeekProgress = null;

	private ProgressDialog mProgressDialog = null;

	AudioStreamPlayer mAudioPlayer = null;
	int frequency = 8000;
	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	// 우리의 FFT 객체는 transformer고, 이 FFT 객체를 통해 AudioRecord 객체에서 한 번에 256가지 샘플을
	// 다룬다. 사용하는 샘플의 수는 FFT 객체를 통해
	// 샘플들을 실행하고 가져올 주파수의 수와 일치한다. 다른 크기를 마음대로 지정해도 되지만, 메모리와 성능 측면을 반드시 고려해야
	// 한다.
	// 적용될 수학적 계산이 프로세서의 성능과 밀접한 관계를 보이기 때문이다.
	private RealDoubleFFT transformer;
	int blockSize = 256;
	Button startStopButton;
	boolean started = false;
	ImageView imageView;
	Bitmap bitmap;
	Canvas canvas;
	Paint paint;
	double[] toTransform = new double[blockSize];
	

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
		transformer = new RealDoubleFFT(blockSize);

		// ImageView 및 관련 객체 설정 부분
		imageView = (ImageView) findViewById(R.id.ImageView01);
		bitmap = Bitmap.createBitmap((int) 256, (int) 100, Bitmap.Config.ARGB_8888);
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

		mAudioPlayer.play();
		while(started){
		toTransform=mAudioPlayer.arr;
		FFTView(toTransform);}
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
	
	public void FFTView(double[] toTransform2){
		transformer.ft(toTransform);
		// publishProgress를 호출하면 onProgressUpdate가 호출된다.
		onProgressUpdate(toTransform);
	}
	
	protected void onProgressUpdate(double[]... toTransform) {
		canvas.drawColor(Color.BLACK);

		for (int i = 0; i < toTransform[0].length; i++) {
			int x = i;
			int downy = (int) (100 - (toTransform[0][i] * 10));
			int upy = 100;

			canvas.drawLine(x, downy, x, upy, paint);
		}
		imageView.invalidate();
	}

	@Override
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

}
