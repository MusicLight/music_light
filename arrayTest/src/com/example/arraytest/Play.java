package com.example.arraytest;

import java.io.IOException;
import java.io.OutputStream;
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
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Play extends Activity implements OnClickListener {
   
	MediaPlayer mp = null;
	TextView v;
	String s, s1;
	Button start, pause, light;
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
	byte[] arr = new byte[blockSize];
	double[] toTransform = new double[blockSize];
	byte[] abc = new byte[blockSize];
	byte a, b, c;
	// RecordAudio는 여기에서 정의되는 내부 클래스로서 AsyncTask를 확장한다.
	RecordAudio recordTask;
	double ad, bd, cd;
	// Bitmap 이미지를 표시하기 위해 ImageView를 사용한다. 이 이미지는 현재 오디오 스트림에서 주파수들의 레벨을 나타낸다.
	// 이 레벨들을 그리려면 Bitmap에서 구성한 Canvas 객체와 Paint객체가 필요하다.
	ImageView imageView;
	Bitmap bitmap;
	Canvas canvas;
	Paint paint;

	/****************************** 8** 블루투스 ******************************/
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// Program variables
	private boolean connectStat = false;
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

		Intent intent = getIntent();
		String path = intent.getExtras().getString("path");
		String fileName = intent.getExtras().getString("fileName");
		
		start = (Button) findViewById(R.id.start);
		pause = (Button) findViewById(R.id.pause);

		s = path + fileName;

		((TextView) findViewById(R.id.mypath)).setText("경로 : " + path);
		((TextView) findViewById(R.id.title)).setText("파일명 : " + fileName);

		transformer = new RealDoubleFFT(blockSize);

		// ImageView 및 관련 객체 설정 부분
		imageView = (ImageView) findViewById(R.id.ImageView01);
		bitmap = Bitmap.createBitmap((int) 256, (int) 100, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		paint = new Paint();
		paint.setColor(Color.GREEN);
		imageView.setImageBitmap(bitmap);

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

		start.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				byte arr[] = {5, 30, 100, 2, 4, 6, 10};
		            write(arr);
			}
			/*	if (started) {
					started = false;
					recordTask.cancel(true);
				} else {
					started = true;
					recordTask = new RecordAudio();
					recordTask.execute();
				}

				mp = new MediaPlayer();
				try {
					mp.setDataSource(s);
					mp.prepare();
				} catch (Exception e) {
					e.printStackTrace();
				}
				mp.start();
			}*/
		});

		pause.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (mp != null) {
					mp.stop();
					mp.release();
				}
				mp = null;
			}
		});

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

	private class RecordAudio extends AsyncTask<Void, double[], Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				// AudioRecord를 설정하고 사용한다.
				int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

				AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
						channelConfiguration, audioEncoding, bufferSize);

				// short로 이뤄진 배열인 buffer는 원시 PCM 샘플을 AudioRecord 객체에서 받는다.
				// double로 이뤄진 배열인 toTransform은 같은 데이터를 담지만 double 타입인데, FFT
				// 클래스에서는 double타입이 필요해서이다.
				short[] buffer = new short[blockSize];
				double[] toTransform = new double[blockSize];
				int[] arr = new int[blockSize];
				int a;
				byte b, cc, dd, ee;
				byte[] abc = new byte[blockSize];

				audioRecord.startRecording();

				while (started) {
					int bufferReadResult = audioRecord.read(buffer, 0, blockSize);

					// AudioRecord 객체에서 데이터를 읽은 다음에는 short 타입의 변수들을 double 타입으로
					// 바꾸는 루프를 처리한다.
					// 직접 타입 변환(casting)으로 이 작업을 처리할 수 없다. 값들이 전체 범위가 아니라 -1.0에서
					// 1.0 사이라서 그렇다
					// short를 32,768.0(Short.MAX_VALUE) 으로 나누면 double로 타입이 바뀌는데,
					// 이 값이 short의 최대값이기 때문이다.
					for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
						toTransform[i] = (double) buffer[i] / Short.MAX_VALUE; // 부호
						// 있는
						// 16비트
					}

					// 이제 double값들의 배열을 FFT 객체로 넘겨준다. FFT 객체는 이 배열을 재사용하여 출력 값을
					// 담는다. 포함된 데이터는 시간 도메인이 아니라
					// 주파수 도메인에 존재한다. 이 말은 배열의 첫 번째 요소가 시간상으로 첫 번째 샘플이 아니라는 얘기다.
					// 배열의 첫 번째 요소는 첫 번째 주파수 집합의 레벨을 나타낸다.

					// 256가지 값(범위)을 사용하고 있고 샘플 비율이 8,000 이므로 배열의 각 요소가 대략
					// 15.625Hz를 담당하게 된다. 15.625라는 숫자는 샘플 비율을 반으로 나누고(캡쳐할 수 있는
					// 최대 주파수는 샘플 비율의 반이다. <- 누가 그랬는데...), 다시 256으로 나누어 나온 것이다.
					// 따라서 배열의 첫 번째 요소로 나타난 데이터는 영(0)과 15.625Hz 사이에
					// 해당하는 오디오 레벨을 의미한다.
					transformer.ft(toTransform);

					// publishProgress를 호출하면 onProgressUpdate가 호출된다.
					publishProgress(toTransform);
					arr = toIntArray(toTransform);

				/*	for (int i = 0; i < arr.length; i++) {
						a = arr[i];
						b = (byte) a;
						abc[i] = b;
					}
					cc = abc[10];
					dd = abc[20];
					ee = abc[30];

					write(cc);
					write(dd);
					write(ee);*/
				}

				audioRecord.stop();
			} catch (Throwable t) {
				Log.e("AudioRecord", "Recording Failed");
			}

			return null;
		}

		// onProgressUpdate는 우리 엑티비티의 메인 스레드로 실행된다. 따라서 아무런 문제를 일으키지 않고 사용자
		// 인터페이스와 상호작용할 수 있다.
		// 이번 구현에서는 onProgressUpdate가 FFT 객체를 통해 실행된 다음 데이터를 넘겨준다. 이 메소드는 최대
		// 100픽셀의 높이로 일련의 세로선으로
		// 화면에 데이터를 그린다. 각 세로선은 배열의 요소 하나씩을 나타내므로 범위는 15.625Hz다. 첫 번째 행은 범위가 0에서
		// 15.625Hz인 주파수를 나타내고,
		// 마지막 행은 3,984.375에서 4,000Hz인 주파수를 나타낸다.
		@Override
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

	public static int[] toIntArray(double[] s) {
		int[] intArray = new int[s.length];
		double a;
		int b;
		for (int i = 0; i < intArray.length; i++) {
			a = s[i] * 100;
			b = (int) a;
			intArray[i] = b;
		}

		return intArray;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}