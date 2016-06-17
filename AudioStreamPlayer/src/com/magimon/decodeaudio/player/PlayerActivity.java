package com.magimon.decodeaudio.player;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import com.magimon.decodeaudio.player.AudioStreamPlayer.State;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerActivity extends Activity
		implements OnAudioStreamInterface, OnSeekBarChangeListener, OnClickListener {

	/****************** 블루투스 ******************************/
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// Program variables
	private byte AttinyOut;
	private boolean ledStat;
	private boolean connectStat = false;
	private Button connect_button;
	private Button button_send;
	protected static final int MOVE_TIME = 80;
	private long lastWrite = 0;
	private View aboutView;
	private View controlView;
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

	private StringBuffer mOutStringBuffer;

	/*************************************************************************/
	private Button mPlayButton = null;
	private Button mStopButton = null;

	private TextView mTextCurrentTime = null;
	private TextView mTextDuration = null;

	private SeekBar mSeekProgress = null;

	private ProgressDialog mProgressDialog = null;

	AudioStreamPlayer mAudioPlayer = new AudioStreamPlayer();
	int frequency = 8000;
	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	int blockSize = 256;
	private RealDoubleFFT transformer;
	ImageView imageView;
	Bitmap bitmap;
	Canvas canvas;
	Paint paint;
	boolean started = false;
	byte[] aaa;

	double[] toTransform = new double[blockSize];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio_stream_player);

		transformer = new RealDoubleFFT(blockSize);

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

		imageView = (ImageView) findViewById(R.id.ImageView01);
		bitmap = Bitmap.createBitmap((int) 256, (int) 100, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		paint = new Paint();
		paint.setColor(Color.GREEN);
		imageView.setImageBitmap(bitmap);

		transformer = new RealDoubleFFT(blockSize);

		/*********************************************************/
		// Finds buttons in .xml layout file
		connect_button = (Button) findViewById(R.id.connect_button);
		button_send = (Button) findViewById(R.id.button_send);

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

					// Reset the BluCar
					/*
					 * AttinyOut = 0; ledStat = false; write(AttinyOut);
					 */
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

		/**********************************************************************
		 * Buttons for controlling BluCar
		 */

		// Connect to Bluetooth Module
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

		button_send.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget

				//1~10 , 11~20, 21~30
				byte[] arr = { 5, 19, 26};
				write(arr);
			}
		});
		/**********************************************************************/
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();

		stop();
	}

	private void updatePlayer(AudioStreamPlayer.State state) {
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

	private void pause() {
		if (this.mAudioPlayer != null) {
			this.mAudioPlayer.pause();
		}
	}

	private void play() {
		releaseAudioPlayer();

		mAudioPlayer = new AudioStreamPlayer();
		mAudioPlayer.setOnAudioStreamInterface(this);

		mAudioPlayer.setUrlString("/storage/emulated/0/Music/aaa.mp3");

		try {

			mAudioPlayer.play();
			while (started) {

				aaa = mAudioPlayer.fftarr;
				for (int i = 0; i < aaa.length; i++) {
					toTransform[i] = (double) aaa[i] / Byte.MAX_VALUE;
				}
				transformer.ft(toTransform);

				onProgressUpdate(toTransform);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void releaseAudioPlayer() {
		if (mAudioPlayer != null) {
			mAudioPlayer.stop();
			mAudioPlayer.release();
			mAudioPlayer = null;

		}
	}

	private void stop() {
		if (this.mAudioPlayer != null) {
			this.mAudioPlayer.stop();
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

	private boolean isSeekBarTouch = false;

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

		this.mAudioPlayer.seekTo(progress);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_play: {
			if (mPlayButton.isSelected()) {
				if (mAudioPlayer != null && mAudioPlayer.getState() == State.Pause) {
					mAudioPlayer.pauseToPlay();
					started = true;

				} else {
					started = false;
					pause();

				}
			} else {
				started = true;
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

	public void onProgressUpdate(double[] toTransform) {
		canvas.drawColor(Color.BLACK);

		for (int i = 0; i < toTransform.length; i++) {
			int x = i;
			int downy = (int) (100 - (toTransform[i] * 10));
			int upy = 100;

			canvas.drawLine(x, downy, x, upy, paint);
		}
		imageView.invalidate();

	}
	
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
			// to call it, but it might hurt not to... discovery is aR
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
	
	

	public void write(byte[] arr) {
		if (outStream != null) {
			try {
				outStream.write(arr);
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

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { MenuInflater
	 * inflater = getMenuInflater(); inflater.inflate(R.menu.option_menu, menu);
	 * return true; }
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { switch
	 * (item.getItemId()) { case R.id.about: // Show info about the author
	 * (that's me!) aboutAlert.show(); return true; } return false; }
	 */

}
