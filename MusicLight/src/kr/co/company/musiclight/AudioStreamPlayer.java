package kr.co.company.musiclight;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class AudioStreamPlayer extends Activity
		implements OnAudioStreamInterface, OnSeekBarChangeListener, OnClickListener

{
	public static final String TAG = "AudioStreamPlayer";

	public MediaExtractor mExtractor = null;
	public MediaCodec mMediaCodec = null;
	public AudioTrack mAudioTrack = null;
	MainActivity ma = new MainActivity();

	public int mInputBufIndex = 0;

	public boolean isForceStop = false;
	public volatile boolean isPause = false;

	public OnAudioStreamInterface mListener = null;

	public ImageView mPlayButton = null;
	public ImageView mStopButton = null;
	public ImageView mPauseButton = null;

	public TextView mTextCurrentTime = null;
	public TextView mTextDuration = null;

	public SeekBar mSeekProgress = null;

	public ProgressDialog mProgressDialog = null;

	ImageView b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12;

	String path, fileName, s;
	int frequency = 3600;
	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private RealDoubleFFT transformer;
	static int blockSize = 300;
	ImageView imageView;
	Bitmap bitmap;
	Canvas canvas;
	Paint paint;
	final Handler han = new Handler();

	byte[] xxx = new byte[25];
	byte[] abc = new byte[blockSize];
	byte[] aa = new byte[13];

	double[] toTransform = new double[blockSize];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);

		mPlayButton = (ImageView) this.findViewById(R.id.button_play);
		mPlayButton.setOnClickListener(this);
		mStopButton = (ImageView) this.findViewById(R.id.button_stop);
		mStopButton.setOnClickListener(this);
		mPauseButton = (ImageView) this.findViewById(R.drawable.pause);

		mTextCurrentTime = (TextView) findViewById(R.id.text_pos);
		mTextDuration = (TextView) findViewById(R.id.text_duration);

		mSeekProgress = (SeekBar) findViewById(R.id.seek_progress);
		mSeekProgress.setOnSeekBarChangeListener(this);
		mSeekProgress.setMax(0);
		mSeekProgress.setProgress(0);

		updatePlayer(State.Stopped);

		Intent intent = getIntent();
		String path = intent.getExtras().getString("path");
		String fileName = intent.getExtras().getString("fileName");

		s = path + fileName;
		transformer = new RealDoubleFFT(blockSize);

		//((TextView) findViewById(R.id.songname)).setBackgroundColor(Color.parseColor("#ffdab9"));
		((TextView) findViewById(R.id.songname)).setText(fileName);
		// ImageView 및 관련 객체 설정 부분
		imageView = (ImageView) findViewById(R.id.ImageView01);
		bitmap = Bitmap.createBitmap((int) 300, (int) 100, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		paint = new Paint();
		paint.setColor(Color.GRAY);
		imageView.setImageBitmap(bitmap);

		b1 = (ImageView) findViewById(R.id.circle1);
		b2 = (ImageView) findViewById(R.id.circle2);
		b3 = (ImageView) findViewById(R.id.circle3);
		b4 = (ImageView) findViewById(R.id.circle4);
		b5 = (ImageView) findViewById(R.id.circle5);
		b6 = (ImageView) findViewById(R.id.circle6);
		b7 = (ImageView) findViewById(R.id.circle7);
		b8 = (ImageView) findViewById(R.id.circle8);
		b9 = (ImageView) findViewById(R.id.circle9);
		b10 = (ImageView) findViewById(R.id.circle10);
		b11 = (ImageView) findViewById(R.id.circle11);
		b12 = (ImageView) findViewById(R.id.circle12);
	}

	public void setOnAudioStreamInterface(OnAudioStreamInterface listener) {
		this.mListener = listener;
	}

	public enum State {
		Stopped, Prepare, Buffering, Playing, Pause
	};

	State mState = State.Stopped;

	public State getState() {
		return mState;
	}

	public AudioStreamPlayer() {
		mState = State.Stopped;
	}

	public void play() {
		stop();
		release();
		setOnAudioStreamInterface(this);

		mState = State.Prepare;
		isForceStop = false;

		mAudioPlayerHandler.onAudioPlayerBuffering(AudioStreamPlayer.this);

		new Thread(new Runnable() {
			@Override
			public void run() {
				decodeLoop();
				
			}
			

		}).start();
	}

	public DelegateHandler mAudioPlayerHandler = new DelegateHandler();

	class DelegateHandler extends Handler {
		public void handleMessage(Message msg) {
		}

		public void onAudioPlayerPlayerStart(AudioStreamPlayer player) {
			if (mListener != null) {
				mListener.onAudioPlayerStart(player);
			}
		}

		public void onAudioPlayerStop(AudioStreamPlayer player) {
			if (mListener != null) {
				mListener.onAudioPlayerStop(player);
			}
		}

		public void onAudioPlayerError(AudioStreamPlayer player) {
			if (mListener != null) {
				mListener.onAudioPlayerError(player);
			}
		}

		public void onAudioPlayerBuffering(AudioStreamPlayer player) {
			if (mListener != null) {
				mListener.onAudioPlayerBuffering(player);
			}
		}

		public void onAudioPlayerDuration(int totalSec) {
			if (mListener != null) {
				mListener.onAudioPlayerDuration(totalSec);
			}
		}

		public void onAudioPlayerCurrentTime(int sec) {
			if (mListener != null) {
				mListener.onAudioPlayerCurrentTime(sec);
			}
		}

		public void onAudioPlayerPause() {
			if (mListener != null) {
				mListener.onAudioPlayerPause(AudioStreamPlayer.this);
			}
		}
	};

	public void decodeLoop() {
		ByteBuffer[] codecInputBuffers;
		ByteBuffer[] codecOutputBuffers;

		mExtractor = new MediaExtractor();
		try {
			mExtractor.setDataSource(s);

		} catch (Exception e) {
			mAudioPlayerHandler.onAudioPlayerError(AudioStreamPlayer.this);
			return;
		}

		MediaFormat format = mExtractor.getTrackFormat(0);
		String mime = format.getString(MediaFormat.KEY_MIME);
		long duration = format.getLong(MediaFormat.KEY_DURATION);
		int totalSec = (int) (duration / 1000 / 1000);
		int min = totalSec / 60;
		int sec = totalSec % 60;

		mAudioPlayerHandler.onAudioPlayerDuration(totalSec);

		Log.d(TAG, "Time = " + min + " : " + sec);
		Log.d(TAG, "Duration = " + duration);

		try {
			mMediaCodec = MediaCodec.createDecoderByType(mime);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mMediaCodec.configure(format, null, null, 0);
		mMediaCodec.start();
		codecInputBuffers = mMediaCodec.getInputBuffers();
		codecOutputBuffers = mMediaCodec.getOutputBuffers();

		int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);

		Log.i(TAG, "mime " + mime);
		Log.i(TAG, "sampleRate " + sampleRate);

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO,
				AudioFormat.ENCODING_PCM_16BIT,
				AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT),
				AudioTrack.MODE_STREAM);

		mAudioTrack.play();
		mExtractor.selectTrack(0);

		final long kTimeOutUs = 10000;
		MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
		boolean sawInputEOS = false;
		int noOutputCounter = 0;
		int noOutputCounterLimit = 50;

		while (!sawInputEOS && noOutputCounter < noOutputCounterLimit && !isForceStop) {
			if (!sawInputEOS) {
				if (isPause) {
					if (mState != State.Pause) {
						mState = State.Pause;

						mAudioPlayerHandler.onAudioPlayerPause();
					}
					continue;
				}
				noOutputCounter++;
				if (isSeek) {
					mExtractor.seekTo(seekTime * 1000 * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
					isSeek = false;
				}

				mInputBufIndex = mMediaCodec.dequeueInputBuffer(kTimeOutUs);
				if (mInputBufIndex >= 0) {
					ByteBuffer dstBuf = codecInputBuffers[mInputBufIndex];

					int sampleSize = mExtractor.readSampleData(dstBuf, 0);

					long presentationTimeUs = 0;

					if (sampleSize < 0) {
						Log.d(TAG, "saw input EOS.");
						sawInputEOS = true;
						sampleSize = 0;
					} else {
						presentationTimeUs = mExtractor.getSampleTime();

						Log.d(TAG, "presentaionTime = " + (int) (presentationTimeUs / 1000 / 1000));

						mAudioPlayerHandler.onAudioPlayerCurrentTime((int) (presentationTimeUs / 1000 / 1000));
					}

					mMediaCodec.queueInputBuffer(mInputBufIndex, 0, sampleSize, presentationTimeUs,
							sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);

					if (!sawInputEOS) {
						mExtractor.advance();
					}
				} else {
					Log.e(TAG, "inputBufIndex " + mInputBufIndex);
				}
			}

			int res = mMediaCodec.dequeueOutputBuffer(info, kTimeOutUs);

			if (res >= 0) {
				if (info.size > 0) {
					noOutputCounter = 0;
				}

				int outputBufIndex = res;
				ByteBuffer buf = codecOutputBuffers[outputBufIndex];

				final byte[] chunk = new byte[info.size];
				buf.get(chunk);
				buf.clear();

				transformer.ft(toTransform);
				onProgressUpdate(toTransform);
				abc = toByteArray(toTransform);

				int a = 0;
				byte sum = 0, avg = 0;
				for (int i = 1; i < 24; i = i + 2) {
					for (int j = a; j < a + 25; j++) {
						sum = (byte) (sum + abc[i]);
					}
					avg = (byte) (sum / 25);
					a = a + 25;
					xxx[i] = (byte)((avg+6)*3);

				}
				

				byte temp;
				for (int i = 1; i < 24; i = i + 2) {
					if (xxx[i] == 0) {
						xxx[i - 1] = 0;
					} else {
						switch (i) {
						case 1:
							temp = 'A';
							xxx[i - 1] = temp;
							break;
						case 3:
							temp = 'B';
							xxx[i - 1] = temp;
							break;
						case 5:
							temp = 'C';
							xxx[i - 1] = temp;
							break;
						case 7:
							temp = 'D';
							xxx[i - 1] = temp;
							break;
						case 9:
							temp = 'E';
							xxx[i - 1] = temp;
							break;
						case 11:
							temp = 'F';
							xxx[i - 1] = temp;
							break;
						case 13:
							temp = 'G';
							xxx[i - 1] = temp;
							break;
						case 15:
							temp = 'H';
							xxx[i - 1] = temp;
							break;
						case 17:
							temp = 'I';
							xxx[i - 1] = temp;
							break;
						case 19:
							temp = 'J';
							xxx[i - 1] = temp;
							break;
						case 21:
							temp = 'K';
							xxx[i - 1] = temp;
							break;
						case 23:
							temp = 'L';
							xxx[i - 1] = temp;
							break;
						}

					}
				}

				xxx[24] = '/';
				
				for (int i = 0; i < 25; i++) {
					MainActivity.write(xxx[i]);
				}
				
				UIChange(xxx);

				

				/*
				 * if(xxx[1]<64) b37.setBackgroundColor(Color.rgb(255,255, 0));
				 * else if(xxx[1]>=64 && xxx[1]<128)
				 * b23.setBackgroundColor(Color.rgb(255,0, 0)); else
				 * if(xxx[1]>=128 && xxx[1]<192)
				 * b13.setBackgroundColor(Color.rgb(128,0, 0)); else
				 * b13.setBackgroundColor(Color.rgb(0,0,255));
				 */

				

				MainActivity.emptyOutStream();

				if (chunk.length > 0) {
					mAudioTrack.write(chunk, 0, chunk.length);

					for (int i = 0; i < 300; i++) {
						toTransform[i] = (double) chunk[i] / 32768.0;
					}

					if (this.mState != State.Playing) {
						mAudioPlayerHandler.onAudioPlayerPlayerStart(AudioStreamPlayer.this);
					}
					this.mState = State.Playing;
				}
				mMediaCodec.releaseOutputBuffer(outputBufIndex, false);
			} else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
				codecOutputBuffers = mMediaCodec.getOutputBuffers();

				Log.d(TAG, "output buffers have changed.");
			} else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				MediaFormat oformat = mMediaCodec.getOutputFormat();

				Log.d(TAG, "output format has changed to " + oformat);
			} else {
				Log.d(TAG, "dequeueOutputBuffer returned " + res);
			}
		}

		Log.d(TAG, "stopping...");

		releaseResources(true);

		this.mState = State.Stopped;
		isForceStop = true;

		if (noOutputCounter >= noOutputCounterLimit) {
			mAudioPlayerHandler.onAudioPlayerError(AudioStreamPlayer.this);
		} else {
			mAudioPlayerHandler.onAudioPlayerStop(AudioStreamPlayer.this);
		}
	}

	

	public void release() {
		stop();
		releaseResources(false);
	}

	public void releaseResources(Boolean release) {
		if (mExtractor != null) {
			mExtractor.release();
			mExtractor = null;
		}

		if (mMediaCodec != null) {
			if (release) {
				mMediaCodec.stop();
				mMediaCodec.release();
				mMediaCodec = null;
			}

		}
		if (mAudioTrack != null) {
			mAudioTrack.flush();
			mAudioTrack.release();
			mAudioTrack = null;
		}
	}

	public void pause() {

		isPause = true;

	}

	public void stop() {
		isForceStop = true;

	}

	boolean isSeek = false;
	int seekTime = 0;

	public void seekTo(int progress) {
		isSeek = true;
		seekTime = progress;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		stop();
	}

	public void updatePlayer(AudioStreamPlayer.State state) {
		switch (state) {
		case Stopped: {
			if (mProgressDialog != null) {
				mProgressDialog.cancel();
				mProgressDialog.dismiss();

				mProgressDialog = null;
			}
			mPlayButton.setSelected(false);
		//	mPlayButton.setText("Play");
			mPlayButton.setImageResource(R.drawable.play);

			mTextCurrentTime.setText("00:00");
			mTextDuration.setText("00:00");

			mSeekProgress.setMax(0);
			mSeekProgress.setProgress(0);

			break;
		}
		case Prepare:
		case Buffering: {
			if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(this);
			}
			mProgressDialog.show();

			mPlayButton.setSelected(false);
		//	mPlayButton.setText("Play");
			mPlayButton.setImageResource(R.drawable.play);

			mTextCurrentTime.setText("00:00");
			mTextDuration.setText("00:00");
			break;
		}
		case Pause: {

			break;
		}
		case Playing: {
			if (mProgressDialog != null) {
				mProgressDialog.cancel();
				mProgressDialog.dismiss();

				mProgressDialog = null;
			}
			mPlayButton.setSelected(true);
		//	mPlayButton.setText("Pause");
			mPlayButton.setImageResource(R.drawable.pause);

			break;
		}
		}
	}

	@Override
	public void onAudioPlayerStart(AudioStreamPlayer player) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				updatePlayer(State.Playing);
			}
		});
	}

	@Override
	public void onAudioPlayerStop(AudioStreamPlayer player) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				updatePlayer(State.Stopped);
			}
		});

	}

	@Override
	public void onAudioPlayerError(AudioStreamPlayer player) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				updatePlayer(State.Stopped);
			}
		});

	}

	@Override
	public void onAudioPlayerBuffering(AudioStreamPlayer player) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				updatePlayer(State.Buffering);
			}
		});

	}

	@Override
	public void onAudioPlayerDuration(final int totalSec) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (totalSec > 0) {
					int min = totalSec / 60;
					int sec = totalSec % 60;

					mTextDuration.setText(String.format("%02d:%02d", min, sec));

					mSeekProgress.setMax(totalSec);
				}
			}

		});
	}

	@Override
	public void onAudioPlayerCurrentTime(final int sec) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!isSeekBarTouch) {
					int m = sec / 60;
					int s = sec % 60;

					mTextCurrentTime.setText(String.format("%02d:%02d", m, s));

					mSeekProgress.setProgress(sec);
				}
			}
		});
	}

	@Override
	public void onAudioPlayerPause(AudioStreamPlayer player) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
		//		mPlayButton.setText("Play");
				mPlayButton.setImageResource(R.drawable.play);
			}
		});
	}

	public boolean isSeekBarTouch = false;

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		this.isSeekBarTouch = true;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		this.isSeekBarTouch = false;

		int progress = seekBar.getProgress();

		this.seekTo(progress);
	}
	public void UIChange(final byte[] xxx) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (xxx[1] < 11)
					b1.setImageResource(R.drawable.white);
				else if (xxx[1] >= 11 && xxx[1] < 22)
					b1.setImageResource(R.drawable.red);
				else if (xxx[1] >=22 && xxx[1] < 33)
					b1.setImageResource(R.drawable.green);
				else
					b1.setImageResource(R.drawable.blue);
				
				if (xxx[3] <11)
					b2.setImageResource(R.drawable.white);
				else if (xxx[3] >=11 && xxx[3] < 22)
					b2.setImageResource(R.drawable.red);
				else if (xxx[3] >=22 && xxx[3] < 33)
					b2.setImageResource(R.drawable.green);
				else
					b2.setImageResource(R.drawable.blue);
				
				if (xxx[5] < 11)
					b3.setImageResource(R.drawable.white);
				else if (xxx[5] >= 11 && xxx[5] < 22)
					b3.setImageResource(R.drawable.red);
				else if (xxx[5] >=22 && xxx[5] < 33)
					b3.setImageResource(R.drawable.green);
				else
					b3.setImageResource(R.drawable.blue);
				
				if (xxx[7] < 11)
					b4.setImageResource(R.drawable.white);
				else if (xxx[7] >= 11 && xxx[7] < 22)
					b4.setImageResource(R.drawable.red);
				else if (xxx[7] >=22 && xxx[7] < 33)
					b4.setImageResource(R.drawable.green);
				else
					b4.setImageResource(R.drawable.blue);
				
				if (xxx[9] <11)
					b5.setImageResource(R.drawable.white);
				else if (xxx[9] >= 11 && xxx[9] < 22)
					b5.setImageResource(R.drawable.red);
				else if (xxx[9] >=22 && xxx[9] <33)
					b5.setImageResource(R.drawable.green);
				else
					b5.setImageResource(R.drawable.blue);
				
				if (xxx[11] < 11)
					b6.setImageResource(R.drawable.white);
				else if (xxx[11] >= 11 && xxx[11] < 22)
					b6.setImageResource(R.drawable.red);
				else if (xxx[11] >=22 && xxx[11] < 33)
					b6.setImageResource(R.drawable.green);
				else
					b6.setImageResource(R.drawable.blue);
				
				if (xxx[13] < 11)
					b7.setImageResource(R.drawable.white);
				else if (xxx[13] >= 11 && xxx[13] < 22)
					b7.setImageResource(R.drawable.red);
				else if (xxx[13] >=22 && xxx[13] < 33)
					b7.setImageResource(R.drawable.green);
				else
					b7.setImageResource(R.drawable.blue);
				
				if (xxx[15] < 11)
					b8.setImageResource(R.drawable.white);
				else if (xxx[15] >= 11 && xxx[15] <22)
					b8.setImageResource(R.drawable.red);
				else if (xxx[15] >=22 && xxx[15] < 33)
					b8.setImageResource(R.drawable.green);
				else
					b8.setImageResource(R.drawable.blue);
				
				if (xxx[17] <11)
					b9.setImageResource(R.drawable.white);
				else if (xxx[17] >= 11 && xxx[17] < 22)
					b9.setImageResource(R.drawable.red);
				else if (xxx[17] >=22&& xxx[17] < 33)
					b9.setImageResource(R.drawable.green);
				else
					b9.setImageResource(R.drawable.blue);
				
				if (xxx[19] <11)
					b10.setImageResource(R.drawable.white);
				else if (xxx[19] >= 11&& xxx[19] <22)
					b10.setImageResource(R.drawable.red);
				else if (xxx[19] >=22 && xxx[19] < 33)
					b10.setImageResource(R.drawable.green);
				else
					b10.setImageResource(R.drawable.blue);
				
				if (xxx[21] <11)
					b11.setImageResource(R.drawable.white);
				else if (xxx[21] >= 11&& xxx[21] < 22)
					b11.setImageResource(R.drawable.red);
				else if (xxx[21] >=22 && xxx[21] < 33)
					b11.setImageResource(R.drawable.green);
				else
					b11.setImageResource(R.drawable.blue);
				
				if (xxx[23] < 11)
					b12.setImageResource(R.drawable.white);
				else if (xxx[23] >= 11 && xxx[23] < 22)
					b12.setImageResource(R.drawable.red);
				else if (xxx[23] >=22 && xxx[23] < 33)
					b12.setImageResource(R.drawable.green);
				else
					b12.setImageResource(R.drawable.blue);

				

			}
		});

		
	}

	public void onProgressUpdate(double[]... toTransform) {
		canvas.drawColor(Color.BLACK);

		for (int i = 0; i < toTransform[0].length; i++) {
			int x = i;
			int downy = (int) (100 - (toTransform[0][i] * 500));
			int upy = 100;

			canvas.drawLine(x, downy, x, upy, paint);
		}
		imageView.postInvalidate();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_play: {
			if (mPlayButton.isSelected()) {
				if (getState() == State.Pause) {
					isPause = false;

				} else {
					pause();

				}
			} else {
				play();

			}

			break;
		}
		case R.id.button_stop: {
			stop();
			break;
		}
		}
	}

	/*
	 * public static byte[] toByteArray(double[] s) { byte[] byteArray = new
	 * byte[s.length]; double a; byte b; for (int i = 0; i < byteArray.length;
	 * i++) { a = s[i]; b = (byte) a; byteArray[i] = b; }
	 * 
	 * return byteArray; }
	 */

	public static byte[] toByteArray(double[] s) {
		byte[] byteArray = new byte[blockSize];
		double a;
		byte b;
		for (int i = 0; i < blockSize; i++) {
			a = Math.round(s[i] * 100);
			b = (byte) a;
			byteArray[i] = b;
		}

		return byteArray;
	}

	public byte AVG(int a, int b, byte arr[]) {
		int sum = 0;
		byte avg = 0;
		for (int i = a; i < b; i++) {
			sum = sum + arr[i];
		}
		avg = (byte) (sum / 25);

		return avg;

	}

}