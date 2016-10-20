package kr.co.company.musiclight;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
   /******************************* 블루투스 ******************************/
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
   static OutputStream outStream = null;
   private ConnectThread mConnectThread = null;
   private String deviceAddress = null;
   // Well known SPP UUID (will *probably* map to RFCOMM channel 1 (default) if
   // not in use);
   private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

   /*************************************************************************/
   

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      
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
   
   /******************** 블루투스 ******************************************/
   public static void write(byte[] cc) {
      if (outStream != null) {
         try {

            outStream.write(cc);
         } catch (IOException e) {
         }
      }
   }
   

   public static void emptyOutStream() {
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

   public void FileListener(View target) {
      Intent intent = new Intent(getApplicationContext(), FileExplorer.class);
      startActivity(intent);
   }

}