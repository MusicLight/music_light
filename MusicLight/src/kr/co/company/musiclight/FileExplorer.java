package kr.co.company.musiclight;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SdCardPath")
public class FileExplorer extends Activity {
	static int kill=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_explorer);
		
		FileList _FileList = new FileList(this);   
		
        _FileList.setOnPathChangedListener(new OnPathChangedListener() {
			@Override
			public void onChanged(String path) {
				// TODO Auto-generated method stub
				((TextView) findViewById(R.id.FilePath)).setText("경로: "+path);
			}
		});
        
        _FileList.setOnFileSelected(new OnFileSelectedListener() {
			@Override
			public void onSelected(String path, String fileName) {
				// TODO Auto-generated method stub
				((TextView) findViewById(R.id.FilePath)).setText("경로: "+path+fileName);
				Intent intent = new Intent(getApplicationContext(), Play.class);
				intent.putExtra("path", path);
				intent.putExtra("fileName", fileName);
				startActivity(intent);				
				}
			
			
			
				
				
									
					
	});
        LinearLayout layout = (LinearLayout) findViewById(R.id.LinearLayout01);
        layout.addView(_FileList);
        
        _FileList.setPath("/storage/emulated/0/Music");
        _FileList.setFocusable(true);
        _FileList.setFocusableInTouchMode(true);
        
        
        
        
    }
	

	public void Listener(View target){
    	Intent intent = new Intent(getApplicationContext(), Play.class);
    	startActivity(intent);
    }
	public void _finish(){
    	moveTaskToBack(true);
    	finish();
    	android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  { // 뒤로가기 키를 눌렀을때
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
        	if(kill==0){
        		kill++;
        		Toast.makeText(this, "종료하시려면 한번더 누르세요", Toast.LENGTH_SHORT).show();
        	}else
        		_finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}