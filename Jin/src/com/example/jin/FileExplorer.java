package com.example.jin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("SdCardPath")
public class FileExplorer extends Activity {
	static int kill = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_explorer);

		FileList _FileList = new FileList(this);

		_FileList.setOnPathChangedListener(new OnPathChangedListener() {
			@Override
			public void onChanged(String path) {
				
				((TextView) findViewById(R.id.FilePath)).setText("경로 : " + path);
			}
		});

		_FileList.setOnFileSelected(new OnFileSelectedListener() {
			@Override
			public void onSelected(String path, String fileName) {
				
				((TextView) findViewById(R.id.FilePath)).setText("경로 : " + path);
				Intent intent = new Intent(getApplicationContext(), AudioStreamPlayer.class);
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

	public void Listener(View target) {
		Intent intent = new Intent(getApplicationContext(), AudioStreamPlayer.class);
		startActivity(intent);
	}
}