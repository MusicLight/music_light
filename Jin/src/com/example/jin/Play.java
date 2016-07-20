package com.example.jin;

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
	// �츮�� FFT ��ü�� transformer��, �� FFT ��ü�� ���� AudioRecord ��ü���� �� ���� 256���� ������
	// �ٷ��. ����ϴ� ������ ���� FFT ��ü�� ����
	// ���õ��� �����ϰ� ������ ���ļ��� ���� ��ġ�Ѵ�. �ٸ� ũ�⸦ ������� �����ص� ������, �޸𸮿� ���� ������ �ݵ�� ����ؾ�
	// �Ѵ�.
	// ����� ������ ����� ���μ����� ���ɰ� ������ ���踦 ���̱� �����̴�.
	private RealDoubleFFT transformer;
	int blockSize = 256;
	Button startStopButton;
	boolean started = false;
	byte[] arr = new byte[blockSize];
	double[] toTransform = new double[blockSize];
	byte[] abc = new byte[blockSize];
	byte a, b, c;
	// RecordAudio�� ���⿡�� ���ǵǴ� ���� Ŭ�����μ� AsyncTask�� Ȯ���Ѵ�.
	RecordAudio recordTask;
	double ad, bd, cd;
	// Bitmap �̹����� ǥ���ϱ� ���� ImageView�� ����Ѵ�. �� �̹����� ���� ����� ��Ʈ������ ���ļ����� ������ ��Ÿ����.
	// �� �������� �׸����� Bitmap���� ������ Canvas ��ü�� Paint��ü�� �ʿ��ϴ�.
	ImageView imageView;
	Bitmap bitmap;
	Canvas canvas;
	Paint paint;

	/****************************** 8** ������� ******************************/
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

		((TextView) findViewById(R.id.mypath)).setText("��� : " + path);
		((TextView) findViewById(R.id.title)).setText("���ϸ� : " + fileName);

		transformer = new RealDoubleFFT(blockSize);

		// ImageView �� ���� ��ü ���� �κ�
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
				if (started) {
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
			}
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
				// AudioRecord�� �����ϰ� ����Ѵ�.
				int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

				AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
						channelConfiguration, audioEncoding, bufferSize);

				// short�� �̷��� �迭�� buffer�� ���� PCM ������ AudioRecord ��ü���� �޴´�.
				// double�� �̷��� �迭�� toTransform�� ���� �����͸� ������ double Ÿ���ε�, FFT
				// Ŭ���������� doubleŸ���� �ʿ��ؼ��̴�.
				short[] buffer = new short[blockSize];
				double[] toTransform = new double[blockSize];
				int[] arr = new int[blockSize];
				int a;
				byte b, cc, dd, ee;
				byte[] abc = new byte[blockSize];

				audioRecord.startRecording();

				while (started) {
					int bufferReadResult = audioRecord.read(buffer, 0, blockSize);

					// AudioRecord ��ü���� �����͸� ���� �������� short Ÿ���� �������� double Ÿ������
					// �ٲٴ� ������ ó���Ѵ�.
					// ���� Ÿ�� ��ȯ(casting)���� �� �۾��� ó���� �� ����. ������ ��ü ������ �ƴ϶� -1.0����
					// 1.0 ���̶� �׷���
					// short�� 32,768.0(Short.MAX_VALUE) ���� ������ double�� Ÿ���� �ٲ�µ�,
					// �� ���� short�� �ִ밪�̱� �����̴�.
					for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
						toTransform[i] = (double) buffer[i] / Short.MAX_VALUE; // ��ȣ
						// �ִ�
						// 16��Ʈ
					}

					// ���� double������ �迭�� FFT ��ü�� �Ѱ��ش�. FFT ��ü�� �� �迭�� �����Ͽ� ��� ����
					// ��´�. ���Ե� �����ʹ� �ð� �������� �ƴ϶�
					// ���ļ� �����ο� �����Ѵ�. �� ���� �迭�� ù ��° ��Ұ� �ð������� ù ��° ������ �ƴ϶�� ����.
					// �迭�� ù ��° ��Ҵ� ù ��° ���ļ� ������ ������ ��Ÿ����.

					// 256���� ��(����)�� ����ϰ� �ְ� ���� ������ 8,000 �̹Ƿ� �迭�� �� ��Ұ� �뷫
					// 15.625Hz�� ����ϰ� �ȴ�. 15.625��� ���ڴ� ���� ������ ������ ������(ĸ���� �� �ִ�
					// �ִ� ���ļ��� ���� ������ ���̴�. <- ���� �׷��µ�...), �ٽ� 256���� ������ ���� ���̴�.
					// ���� �迭�� ù ��° ��ҷ� ��Ÿ�� �����ʹ� ��(0)�� 15.625Hz ���̿�
					// �ش��ϴ� ����� ������ �ǹ��Ѵ�.
					transformer.ft(toTransform);

					// publishProgress�� ȣ���ϸ� onProgressUpdate�� ȣ��ȴ�.
					publishProgress(toTransform);
					arr = toIntArray(toTransform);

					for (int i = 0; i < arr.length; i++) {
						a = arr[i];
						b = (byte) a;
						abc[i] = b;
					}
					cc = abc[10];
					dd = abc[20];
					ee = abc[30];

					write(cc);
					write(dd);
					write(ee);
				}

				audioRecord.stop();
			} catch (Throwable t) {
				Log.e("AudioRecord", "Recording Failed");
			}

			return null;
		}

		// onProgressUpdate�� �츮 ��Ƽ��Ƽ�� ���� ������� ����ȴ�. ���� �ƹ��� ������ ����Ű�� �ʰ� �����
		// �������̽��� ��ȣ�ۿ��� �� �ִ�.
		// �̹� ���������� onProgressUpdate�� FFT ��ü�� ���� ����� ���� �����͸� �Ѱ��ش�. �� �޼ҵ�� �ִ�
		// 100�ȼ��� ���̷� �Ϸ��� ���μ�����
		// ȭ�鿡 �����͸� �׸���. �� ���μ��� �迭�� ��� �ϳ����� ��Ÿ���Ƿ� ������ 15.625Hz��. ù ��° ���� ������ 0����
		// 15.625Hz�� ���ļ��� ��Ÿ����,
		// ������ ���� 3,984.375���� 4,000Hz�� ���ļ��� ��Ÿ����.
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