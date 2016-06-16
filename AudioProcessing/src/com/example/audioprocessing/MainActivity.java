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

	// FFT(Fast Fourier Transform) DFT �˰��� : �����͸� �ð� ����(time base)���� ���ļ� ����(frequency base)���� �ٲٴµ� ���.
	public class AudioProcessing extends Activity implements OnClickListener{
	    // AudioRecord ��ü���� ���ļ��� 8kHz, ����� ä���� �ϳ�, ������ 16��Ʈ�� ���
	int frequency = 8000;
	    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	    // �츮�� FFT ��ü�� transformer��, �� FFT ��ü�� ���� AudioRecord ��ü���� �� ���� 256���� ������ �ٷ��. ����ϴ� ������ ���� FFT ��ü�� ����
	    // ���õ��� �����ϰ� ������ ���ļ��� ���� ��ġ�Ѵ�. �ٸ� ũ�⸦ ������� �����ص� ������, �޸𸮿� ���� ������ �ݵ�� ����ؾ� �Ѵ�.
	    // ����� ������ ����� ���μ����� ���ɰ� ������ ���踦 ���̱� �����̴�.
	    private RealDoubleFFT transformer;
	    int blockSize = 256;
	    Button startStopButton;
	    boolean started = false;
	    
	    // RecordAudio�� ���⿡�� ���ǵǴ� ���� Ŭ�����μ� AsyncTask�� Ȯ���Ѵ�.
	    RecordAudio recordTask;
	    
	    // Bitmap �̹����� ǥ���ϱ� ���� ImageView�� ����Ѵ�. �� �̹����� ���� ����� ��Ʈ������ ���ļ����� ������ ��Ÿ����.
	    // �� �������� �׸����� Bitmap���� ������ Canvas ��ü�� Paint��ü�� �ʿ��ϴ�.
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
	    
	    // �� ��Ƽ��Ƽ�� �۾����� ��κ� RecordAudio��� Ŭ�������� ����ȴ�. �� Ŭ������ AsyncTask�� Ȯ���Ѵ�.
	    // AsyncTask�� ����ϸ� ����� �������̽��� ���ϴ� �ְ� �ϴ� �޼ҵ���� ������ ������� �����Ѵ�.
	    // doInBackground �޼ҵ忡 �� �� �ִ� ���̸� ������ �̷� ������ ������ �� �ִ�.
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
	   
	    // ���� double������ �迭�� FFT ��ü�� �Ѱ��ش�. FFT ��ü�� �� �迭�� �����Ͽ� ��� ���� ��´�. ���Ե� �����ʹ� �ð� �������� �ƴ϶�
	    // ���ļ� �����ο� �����Ѵ�. �� ���� �迭�� ù ��° ��Ұ� �ð������� ù ��° ������ �ƴ϶�� ����. �迭�� ù ��° ��Ҵ� ù ��° ���ļ� ������ ������ ��Ÿ����.
	   
	    // 256���� ��(����)�� ����ϰ� �ְ� ���� ������ 8,000 �̹Ƿ� �迭�� �� ��Ұ� �뷫 15.625Hz�� ����ϰ� �ȴ�. 15.625��� ���ڴ� ���� ������ ������ ������(ĸ���� �� �ִ�
	    // �ִ� ���ļ��� ���� ������ ���̴�. <- ���� �׷��µ�...), �ٽ� 256���� ������ ���� ���̴�. ���� �迭�� ù ��° ��ҷ� ��Ÿ�� �����ʹ� ��(0)�� 15.625Hz ���̿�
	    // �ش��ϴ� ����� ������ �ǹ��Ѵ�.
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
	   
	    // onProgressUpdate�� �츮 ��Ƽ��Ƽ�� ���� ������� ����ȴ�. ���� �ƹ��� ������ ����Ű�� �ʰ� ����� �������̽��� ��ȣ�ۿ��� �� �ִ�.
	    // �̹� ���������� onProgressUpdate�� FFT ��ü�� ���� ����� ���� �����͸� �Ѱ��ش�. �� �޼ҵ�� �ִ� 100�ȼ��� ���̷� �Ϸ��� ���μ�����
	    // ȭ�鿡 �����͸� �׸���. �� ���μ��� �迭�� ��� �ϳ����� ��Ÿ���Ƿ� ������ 15.625Hz��. ù ��° ���� ������ 0���� 15.625Hz�� ���ļ��� ��Ÿ����,
	    // ������ ���� 3,984.375���� 4,000Hz�� ���ļ��� ��Ÿ����.
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
