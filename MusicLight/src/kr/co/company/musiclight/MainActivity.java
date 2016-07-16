package kr.co.company.musiclight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

<<<<<<< HEAD
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      /*
       * @Override public boolean onCreateOptionsMenu(Menu menu) {
       * MenuInflater inflater = getMenuInflater();
       * inflater.inflate(R.menu.option_menu, menu); return true; }
       * 
       * @Override public boolean onOptionsItemSelected(MenuItem item) {
       * switch (item.getItemId()) { case R.id.about: // Show info about the
       * author (that's me!) aboutAlert.show(); return true; } return false; }
       */
   }

   public void FileListener(View target) {
      Intent intent = new Intent(getApplicationContext(), FileExplorer.class);
      startActivity(intent);
   }
=======
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void FileListener(View target) {
		Intent intent = new Intent(getApplicationContext(), FileExplorer.class);
		startActivity(intent);
	}
>>>>>>> 950a28ea47caa5c1b5cdc9ac84f34916fdc8ac35
}