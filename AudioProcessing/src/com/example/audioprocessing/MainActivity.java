package com.example.audioprocessing;


import com.example.audioprocessing.RealDoubleFFT;

import android.app.Activity;
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

public class MainActivity extends Activity {

	// FFT(Fast Fourier Transform) DFT 알고리즘 : 데이터를 시간 기준(time base)에서 주파수 기준(frequency base)으로 바꾸는데 사용.
	public class AudioProcessing extends Activity implements OnClickListener{
	    // AudioRecord 객체에서 주파수는 8kHz, 오디오 채널은 하나, 샘플은 16비트를 사용
	int frequency = 8000;
	    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	    // 우리의 FFT 객체는 transformer고, 이 FFT 객체를 통해 AudioRecord 객체에서 한 번에 256가지 샘플을 다룬다. 사용하는 샘플의 수는 FFT 객체를 통해
	    // 샘플들을 실행하고 가져올 주파수의 수와 일치한다. 다른 크기를 마음대로 지정해도 되지만, 메모리와 성능 측면을 반드시 고려해야 한다.
	    // 적용될 수학적 계산이 프로세서의 성능과 밀접한 관계를 보이기 때문이다.
	    private RealDoubleFFT transformer;
	    int blockSize = 256;
	    Button startStopButton;
	    boolean started = false;
	    
	    // RecordAudio는 여기에서 정의되는 내부 클래스로서 AsyncTask를 확장한다.
	    RecordAudio recordTask;
	    
	    // Bitmap 이미지를 표시하기 위해 ImageView를 사용한다. 이 이미지는 현재 오디오 스트림에서 주파수들의 레벨을 나타낸다.
	    // 이 레벨들을 그리려면 Bitmap에서 구성한 Canvas 객체와 Paint객체가 필요하다.
	    ImageView imageView;
	    Bitmap bitmap;
	    Canvas canvas;
	    Paint paint;
	    
	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	        
	        startStopButton = (Button)findViewById(R.id.StartStopButton);
	        startStopButton.setOnClickListener(this);
	        
	        
	        // RealDoubleFFT 클래스 컨스트럭터는 한번에 처리할 샘플들의 수를 받는다. 그리고 출력될 주파수 범위들의 수를 나타낸다.
	        transformer = new RealDoubleFFT(blockSize);
	        
	        // ImageView 및 관련 객체 설정 부분
	        imageView = (ImageView)findViewById(R.id.ImageView01);
	        bitmap = Bitmap.createBitmap((int)256, (int)100, Bitmap.Config.ARGB_8888);
	        canvas = new Canvas(bitmap);
	        paint = new Paint();
	        paint.setColor(Color.GREEN);
	        imageView.setImageBitmap(bitmap);
	    }
	    
	    // 이 액티비티의 작업들은 대부분 RecordAudio라는 클래스에서 진행된다. 이 클래스는 AsyncTask를 확장한다.
	    // AsyncTask를 사용하면 사용자 인터페이스를 멍하니 있게 하는 메소드들을 별도의 스레드로 실행한다.
	    // doInBackground 메소드에 둘 수 있는 것이면 뭐든지 이런 식으로 실행할 수 있다.
	    private class RecordAudio extends AsyncTask<Void, double[], Void>{
	    @Override
	    protected Void doInBackground(Void... params) {
	    try{
	    	
	    // AudioRecord를 설정하고 사용한다.
	    int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
	   
	    AudioRecord audioRecord = new AudioRecord(
	    MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
	   
	    // short로 이뤄진 배열인 buffer는 원시 PCM 샘플을 AudioRecord 객체에서 받는다.
	    // double로 이뤄진 배열인 toTransform은 같은 데이터를 담지만 double 타입인데, FFT 클래스에서는 double타입이 필요해서이다.
	    short[] buffer = new short[blockSize];
	    double[] toTransform = new double[blockSize];
	   
	    audioRecord.startRecording();
	   
	    while(started){
	    int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
	   
	    // AudioRecord 객체에서 데이터를 읽은 다음에는 short 타입의 변수들을 double 타입으로 바꾸는 루프를 처리한다. 
	    // 직접 타입 변환(casting)으로 이 작업을 처리할 수 없다. 값들이 전체 범위가 아니라 -1.0에서 1.0 사이라서 그렇다
	    // short를 32,768.0(Short.MAX_VALUE) 으로 나누면 double로 타입이 바뀌는데, 이 값이 short의 최대값이기 때문이다. 
	    for(int i = 0; i < blockSize && i < bufferReadResult; i++){
	    toTransform[i] = (double)buffer[i] / Short.MAX_VALUE; // 부호 있는 16비트
	    }
	   
	    // 이제 double값들의 배열을 FFT 객체로 넘겨준다. FFT 객체는 이 배열을 재사용하여 출력 값을 담는다. 포함된 데이터는 시간 도메인이 아니라
	    // 주파수 도메인에 존재한다. 이 말은 배열의 첫 번째 요소가 시간상으로 첫 번째 샘플이 아니라는 얘기다. 배열의 첫 번째 요소는 첫 번째 주파수 집합의 레벨을 나타낸다.
	   
	    // 256가지 값(범위)을 사용하고 있고 샘플 비율이 8,000 이므로 배열의 각 요소가 대략 15.625Hz를 담당하게 된다. 15.625라는 숫자는 샘플 비율을 반으로 나누고(캡쳐할 수 있는
	    // 최대 주파수는 샘플 비율의 반이다. <- 누가 그랬는데...), 다시 256으로 나누어 나온 것이다. 따라서 배열의 첫 번째 요소로 나타난 데이터는 영(0)과 15.625Hz 사이에
	    // 해당하는 오디오 레벨을 의미한다.
	    transformer.ft(toTransform);
	    // publishProgress를 호출하면 onProgressUpdate가 호출된다.
	    publishProgress(toTransform);
	    }
	   
	    audioRecord.stop();
	    }catch(Throwable t){
	    Log.e("AudioRecord", "Recording Failed");
	    }
	   
	    return null;
	    }
	   
	    // onProgressUpdate는 우리 엑티비티의 메인 스레드로 실행된다. 따라서 아무런 문제를 일으키지 않고 사용자 인터페이스와 상호작용할 수 있다.
	    // 이번 구현에서는 onProgressUpdate가 FFT 객체를 통해 실행된 다음 데이터를 넘겨준다. 이 메소드는 최대 100픽셀의 높이로 일련의 세로선으로
	    // 화면에 데이터를 그린다. 각 세로선은 배열의 요소 하나씩을 나타내므로 범위는 15.625Hz다. 첫 번째 행은 범위가 0에서 15.625Hz인 주파수를 나타내고,
	    // 마지막 행은 3,984.375에서 4,000Hz인 주파수를 나타낸다.
	    @Override
	    protected void onProgressUpdate(double[]... toTransform) {
	    canvas.drawColor(Color.BLACK);
	   
	    for(int i = 0; i < toTransform[0].length; i++){
	    int x = i;
	    int downy = (int) (100 - (toTransform[0][i] * 10));
	    int upy = 100;
	   
	    canvas.drawLine(x, downy, x, upy, paint);
	    }
	    imageView.invalidate();
	    }
	    }
	    
	    @Override
	    public void onClick(View arg0) {
	    if(started){
	    started = false;
	    startStopButton.setText("Start");
	    recordTask.cancel(true);
	    }else{
	    started = true;
	    startStopButton.setText("Stop");
	    recordTask = new RecordAudio();
	    recordTask.execute();
	    }
	    }
	}
	
}
