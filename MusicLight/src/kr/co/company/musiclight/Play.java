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
    // �츮�� FFT ��ü�� transformer��, �� FFT ��ü�� ���� AudioRecord ��ü���� �� ���� 256���� ������ �ٷ��. ����ϴ� ������ ���� FFT ��ü�� ����
    // ���õ��� �����ϰ� ������ ���ļ��� ���� ��ġ�Ѵ�. �ٸ� ũ�⸦ ������� �����ص� ������, �޸𸮿� ���� ������ �ݵ�� ����ؾ� �Ѵ�.
    // ����� ������ ����� ���μ����� ���ɰ� ������ ���踦 ���̱� �����̴�.
    private RealDoubleFFT transformer;
    int blockSize = 256;
    boolean started = false;
    
    // RecordAudio�� ���⿡�� ���ǵǴ� ���� Ŭ�����μ� AsyncTask�� Ȯ���Ѵ�.
    RecordAudio recordTask;
    
    // Bitmap �̹����� ǥ���ϱ� ���� ImageView�� ����Ѵ�. �� �̹����� ���� ����� ��Ʈ������ ���ļ����� ������ ��Ÿ����.
    // �� �������� �׸����� Bitmap���� ������ Canvas ��ü�� Paint��ü�� �ʿ��ϴ�.
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
		
		
		((TextView) findViewById(R.id.mypath)).setText("���: "+ path);	
		((TextView) findViewById(R.id.title)).setText("���ϸ�: "+fileName);
		
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
        
        
        // RealDoubleFFT Ŭ���� ����Ʈ���ʹ� �ѹ��� ó���� ���õ��� ���� �޴´�. �׸��� ��µ� ���ļ� �������� ���� ��Ÿ����.
        transformer = new RealDoubleFFT(blockSize);
        
        // ImageView �� ���� ��ü ���� �κ�
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
	    // AudioRecord�� �����ϰ� ����Ѵ�.
	    int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
	   
	    AudioRecord audioRecord = new AudioRecord(
	    MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
	   
	    // short�� �̷��� �迭�� buffer�� ���� PCM ������ AudioRecord ��ü���� �޴´�.
	    // double�� �̷��� �迭�� toTransform�� ���� �����͸� ������ double Ÿ���ε�, FFT Ŭ���������� doubleŸ���� �ʿ��ؼ��̴�.
	    short[] buffer = new short[blockSize];
	    double[] toTransform = new double[blockSize];
	   
	    audioRecord.startRecording();
	   
	    while(started){
	    int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
	   
	    // AudioRecord ��ü���� �����͸� ���� �������� short Ÿ���� �������� double Ÿ������ �ٲٴ� ������ ó���Ѵ�. 
	    // ���� Ÿ�� ��ȯ(casting)���� �� �۾��� ó���� �� ����. ������ ��ü ������ �ƴ϶� -1.0���� 1.0 ���̶� �׷���
	    // short�� 32,768.0(Short.MAX_VALUE) ���� ������ double�� Ÿ���� �ٲ�µ�, �� ���� short�� �ִ밪�̱� �����̴�. 
	    for(int i = 0; i < blockSize && i < bufferReadResult; i++){
	    toTransform[i] = (double)buffer[i] / Short.MAX_VALUE; // ��ȣ �ִ� 16��Ʈ
	    }
	    transformer.ft(toTransform);
	    // publishProgress�� ȣ���ϸ� onProgressUpdate�� ȣ��ȴ�.
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