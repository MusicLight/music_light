package com.example.jin;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class AudioStreamPlayer extends Activity
		implements OnAudioStreamInterface, OnSeekBarChangeListener, OnClickListener

{
	public static final String TAG = "AudioStreamPlayer";

	public MediaExtractor mExtractor = null;
	public MediaCodec mMediaCodec = null;
	public AudioTrack mAudioTrack = null;

	public int mInputBufIndex = 0;

	public boolean isForceStop = false;
	public volatile boolean isPause = false;

	public OnAudioStreamInterface mListener = null;

	public Button mPlayButton = null;
	public Button mStopButton = null;

	public TextView mTextCurrentTime = null;
	public TextView mTextDuration = null;

	public SeekBar mSeekProgress = null;

	public ProgressDialog mProgressDialog = null;

	String path, fileName, s;
	int frequency = 8000;
	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private RealDoubleFFT transformer;
	int blockSize = 256;
	ImageView imageView;
	Bitmap bitmap;
	Canvas canvas;
	Paint paint;

	byte[] xxx = new byte[7];
	byte[] abc = new byte[blockSize];

	double[] toTransform = new double[blockSize];

	/******************************* 블루투스 ******************************/
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// Program variables


	private Button connect_button;
	protected static final int MOVE_TIME = 80;
	OnClickListener myClickListener;
	ProgressDialog myProgressDialog;
	private Toast failToast;
	private Handler mHandler;

	// Bluetooth Stuff
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private ConnectThread mConnectThread = null;
	private String deviceAddress = null;
	// Well known SPP UUID (will *probably* map to RFCOMM channel 1 (default) if
	// not in use);
	private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	/*************************************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		
		/******************************* 블루투스 **************************/

		// Finds buttons in .xml layout file
		connect_button = (Button) findViewById(R.id.connect_button);

		myProgressDialog = new ProgressDialog(this);
		failToast = Toast.makeText(this, R.string.failedToConnect, Toast.LENGTH_SHORT);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (myProgressDialog.isShowing()) {
					myProgressDialog.dismiss();
				}

				// Check if bluetooth connection was made to selected device
				if (msg.what == 1) {
					// Set button to display current status
					connectStat = true;
					connect_button.setText(R.string.connected);

				} else {
					// Connection failed
					failToast.show();
				}
			}
		};

		// Check whether bluetooth adapter exists
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.no_bt_device, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// If BT is not on, request that it be enabled.
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}

		connect_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (connectStat) {
					// Attempt to disconnect from the device
					disconnect();
				} else {
					// Attempt to connect to the device
					connect();
				}
			}
		});

		/***********************************************************************/

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

		Intent intent = getIntent();
		String path = intent.getExtras().getString("path");
		String fileName = intent.getExtras().getString("fileName");

		s = path + fileName;
		transformer = new RealDoubleFFT(blockSize);
		
		((TextView) findViewById(R.id.songname)).setBackgroundColor(Color.parseColor("#ffdab9"));
		((TextView) findViewById(R.id.songname)).setText(fileName);
		// ImageView 및 관련 객체 설정 부분
		imageView = (ImageView) findViewById(R.id.ImageView01);
		bitmap = Bitmap.createBitmap((int) 256, (int) 100, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		paint = new Paint();
		paint.setColor(Color.GREEN);
		imageView.setImageBitmap(bitmap);
	}

	/***************** 블루투스 **************************************************/
	/** Thread used to connect to a specified Bluetooth Device */
	public class ConnectThread extends Thread {
		private String address;
		private boolean connectionStatus;

		ConnectThread(String MACaddress) {
			address = MACaddress;
			connectionStatus = true;
		}

		public void run() {
			// When this returns, it will 'know' about the server,
			// via it's MAC address.
			try {
				BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

				// We need two things before we can successfully connect
				// (authentication issues aside): a MAC address, which we
				// already have, and an RFCOMM channel.
				// Because RFCOMM channels (aka ports) are limited in
				// number, Android doesn't allow you to use them directly;
				// instead you request a RFCOMM mapping based on a service
				// ID. In our case, we will use the well-known SPP Service
				// ID. This ID is in UUID (GUID to you Microsofties)
				// format. Given the UUID, Android will handle the
				// mapping for you. Generally, this will return RFCOMM 1,
				// but not always; it depends what other BlueTooth services
				// are in use on your Android device.
				try {
					btSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
				} catch (IOException e) {
					connectionStatus = false;
				}
			} catch (IllegalArgumentException e) {
				connectionStatus = false;
			}

			// Discovery may be going on, e.g., if you're running a
			// 'scan for devices' search from your handset's Bluetooth
			// settings, so we call cancelDiscovery(). It doesn't hurt
			// to call it, but it might hurt not to... discovery is a
			// heavyweight process; you don't want it in progress when
			// a connection attempt is made.
			mBluetoothAdapter.cancelDiscovery();

			// Blocking connect, for a simple client nothing else can
			// happen until a successful connection is made, so we
			// don't care if it blocks.
			try {
				btSocket.connect();
			} catch (IOException e1) {
				try {
					btSocket.close();
				} catch (IOException e2) {
				}
			}

			// Create a data stream so we can talk to server.
			try {
				outStream = btSocket.getOutputStream();
			} catch (IOException e2) {
				connectionStatus = false;
			}

			// Send final result
			if (connectionStatus) {
				mHandler.sendEmptyMessage(1);
			} else {
				mHandler.sendEmptyMessage(0);
			}
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Show please wait dialog
				myProgressDialog = ProgressDialog.show(this, getResources().getString(R.string.pleaseWait),
						getResources().getString(R.string.makingConnectionString), true);

				// Get the device MAC address
				deviceAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Connect to device with specified MAC address
				mConnectThread = new ConnectThread(deviceAddress);
				mConnectThread.start();

			} else {
				// Failure retrieving MAC address
				Toast.makeText(this, R.string.macFailed, Toast.LENGTH_SHORT).show();
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	/****************************************************************/
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
		@Override
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

		mMediaCodec = MediaCodec.createDecoderByType(mime);
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

				xxx[1] = abc[50];
				xxx[3] = abc[110];
				xxx[5] = abc[170];

				if (xxx[1] == 0) {
					xxx[0] = 0;
				} else {
					xxx[0] = 'A';
				}

				if (xxx[3] == 0) {
					xxx[2] = 0;
				} else {
					xxx[2] = 'B';
				}

				if (xxx[5] == 0) {
					xxx[4] = 0;
				} else {
					xxx[4] = 'C';
				}

				xxx[6] = '/';

				for (int i = 0; i < 7; i++) {
					write(xxx[i]);
				}

				if (chunk.length > 0) {
					mAudioTrack.write(chunk, 0, chunk.length);

					for (int i = 0; i < 256; i++) {
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
			mPlayButton.setText("Play");

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
			mPlayButton.setText("Play");

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
			mPlayButton.setText("Pause");
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
				mPlayButton.setText("Play");
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

	/******************** 블루투스 ******************************************/
	public void write(byte cc) {
		if (outStream != null) {
			try {

				outStream.write(cc);
			} catch (IOException e) {
			}
		}
	}

	public void emptyOutStream() {
		if (outStream != null) {
			try {
				outStream.flush();
			} catch (IOException e) {
			}
		}
	}

	public void connect() {
		// Launch the DeviceListActivity to see devices and do scan
		Intent serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}

	public void disconnect() {
		if (outStream != null) {
			try {
				outStream.close();
				connectStat = false;
				connect_button.setText(R.string.disconnected);
			} catch (IOException e) {
			}
		}
	}

	public static byte[] toByteArray(double[] s) {
		byte[] byteArray = new byte[s.length];
		double a;
		byte b;
		for (int i = 0; i < byteArray.length; i++) {
			a = s[i] * 100;
			b = (byte) a;
			byteArray[i] = b;
		}

		return byteArray;
	}

}