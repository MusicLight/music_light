package com.example.jin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {


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