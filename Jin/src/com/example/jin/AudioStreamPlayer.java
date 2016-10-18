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
   MainActivity ma = new MainActivity();

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
   int frequency = 3600;
   int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
   int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
   private RealDoubleFFT transformer;
   static int blockSize = 300;
   ImageView imageView;
   Bitmap bitmap;
   Canvas canvas;
   Paint paint;

   byte[] xxx = new byte[25];
   byte[] abc = new byte[blockSize];

   double[] toTransform = new double[blockSize];

   

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.player);
      
      

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
      bitmap = Bitmap.createBitmap((int) 300, (int) 100, Bitmap.Config.ARGB_8888);
      canvas = new Canvas(bitmap);
      paint = new Paint();
      paint.setColor(Color.GREEN);
      imageView.setImageBitmap(bitmap);
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

            int a = 0;
            byte sum=0, avg=0;
            for(int i=1;i<24;i=i+2)
            {
            	for(int j=a;j<a+25;j++)
            	{
            		sum=(byte) (sum+abc[i]);
            	}
            	avg=(byte) (sum/25);
            	a=a+25;
            	xxx[i]=avg;
            	
            }
            
           
            byte temp;
            for(int i=1;i<24;i=i+2)
            {
            	if(xxx[i] == 0)
            	{
            		xxx[i-1]=0;
            	}
            	else
            	{
            			switch(i)
            			{
            			case 1:
            				temp = 'A';
            				xxx[i-1]=temp;
            				break;
            			case 3:
            				temp = 'B';
            				xxx[i-1]=temp;
            				break;
            			case 5:
            				temp = 'C';
            				xxx[i-1]=temp;
            				break;
            			case 7:
            				temp = 'D';
            				xxx[i-1]=temp;
            				break;
            			case 9:
            				temp = 'E';
            				xxx[i-1]=temp;
            				break;
            			case 11:
            				temp = 'F';
            				xxx[i-1]=temp;
            				break;
            			case 13:
            				temp = 'G';
            				xxx[i-1]=temp;
            				break;
            			case 15:
            				temp = 'H';
            				xxx[i-1]=temp;
            				break;
            			case 17:
            				temp = 'I';
            				xxx[i-1]=temp;
            				break;
            			case 19:
            				temp = 'J';
            				xxx[i-1]=temp;
            				break;
            			case 21:
            				temp = 'K';
            				xxx[i-1]=temp;
            				break;
            			case 23:
            				temp = 'L';
            				xxx[i-1]=temp;
            				break;
            			}
            			
            	}
            }


            xxx[24] = '/';

            
            MainActivity.write(xxx[4]);
            MainActivity.write(xxx[5]);
            MainActivity.write(xxx[12]);
            MainActivity.write(xxx[13]);
            MainActivity.write(xxx[18]);
            MainActivity.write(xxx[19]);
            MainActivity.write(xxx[24]);
            
            /*MainActivity.write(10);
            MainActivity.write(0);
            MainActivity.write(-10);
            MainActivity.write(-20);
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

   

   /*public static byte[] toByteArray(double[] s) {
      byte[] byteArray = new byte[s.length];
      double a;
      byte b;
      for (int i = 0; i < byteArray.length; i++) {
         a = s[i];
         b = (byte) a;
         byteArray[i] = b;
      }

      return byteArray;
   }*/
   
   public static byte[] toByteArray(double[] s) {
   byte[] byteArray = new byte[blockSize];
   double a;
   byte b;
   for (int i = 0; i < blockSize; i++) {
      a = Math.round(s[i]*100);
      b = (byte) a;
      byteArray[i] = b;
   }

   return byteArray;
}
   public byte AVG(int a, int b,byte arr[])
   {
	   int sum=0;
	   byte avg=0;
	   for(int i=a;i<b;i++)
	   {
		   sum=sum+arr[i];
	   }
	   avg=(byte)(sum/25);
	  
	   return avg;
	   
   }

   

}