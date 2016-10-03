package com.example.jin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	

	private Button connect_button;
	private boolean connectStat = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void FileListener(View target) {
		Intent intent = new Intent(getApplicationContext(), FileExplorer.class);
		startActivity(intent);
	}

}