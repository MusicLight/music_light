package kr.co.company.musiclight;

import android.app.Activity;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Play extends Activity implements OnClickListener {
	MediaPlayer mp = null;
	TextView v;
	String s, s1;
	Button start, pause;
	int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    // 우리의 FFT 객체는 transformer고, 이 FFT 객체를 통해 AudioRecord 객체에서 한 번에 256가지 샘플을 다룬다. 사용하는 샘플의 수는 FFT 객체를 통해
    // 샘플들을 실행하고 가져올 주파수의 수와 일치한다. 다른 크기를 마음대로 지정해도 되지만, 메모리와 성능 측면을 반드시 고려해야 한다.
    // 적용될 수학적 계산이 프로세서의 성능과 밀접한 관계를 보이기 때문이다.
    private RealDoubleFFT transformer;
    int blockSize = 256;
    boolean started = false;
    
    // RecordAudio는 여기에서 정의되는 내부 클래스로서 AsyncTask를 확장한다.
    RecordAudio recordTask;
    
    // Bitmap 이미지를 표시하기 위해 ImageView를 사용한다. 이 이미지는 현재 오디오 스트림에서 주파수들의 레벨을 나타낸다.
    // 이 레벨들을 그리려면 Bitmap에서 구성한 Canvas 객체와 Paint객체가 필요하다.
    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		
		Intent intent = getIntent();
		String path = intent.getExtras().getString("path");
		String fileName = intent.getExtras().getString("fileName");
		String title = intent.getExtras().getString("title");
		
		start = (Button) findViewById(R.id.start);
		pause = (Button) findViewById(R.id.pause);
		
		s= path+fileName;
		
		
		((TextView) findViewById(R.id.mypath)).setText("경로: "+ path);	
		((TextView) findViewById(R.id.title)).setText("파일명: "+fileName);
		
		start.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v){
				mp = new MediaPlayer();
				try{
				mp.setDataSource(s);
				mp.prepare();
				}
				catch(Exception e){
					e.printStackTrace();
				}
				mp.start();
			}
		});
			
		
		pause.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v){
				if(mp != null){
					mp.stop();
					mp.release();
				}
				mp=null;
			}
		});
		
		
        start.setOnClickListener(this);
        
        
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
	public void onClick(View v) {
		if(started){
		    started = false;
		    start.setText("Start");
		    recordTask.cancel(true);
		    }else{
		    started = true;
		    start.setText("Stop");
		    recordTask = new RecordAudio();
		    recordTask.execute();
		// TODO Auto-generated method stub
		
	}
}
}