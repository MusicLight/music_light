package com.example.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ListActivity {
 
 private List<String> item = null;
 private List<String> path = null;
 private String root;
 private TextView myPath;
 
 private String currentPath;
 Comparator<? super File> comparator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myPath = (TextView)findViewById(R.id.path);
        
        comparator = filecomparatorByAlphabetically;
        root = Environment.getExternalStorageDirectory().getPath();
        getDir(root);
        
        Button btnAlphabetically = (Button)findViewById(R.id.button_alphabetically);
        btnAlphabetically.setOnClickListener(new OnClickListener(){

   @Override
   public void onClick(View arg0) {
    comparator = filecomparatorByAlphabetically;
    getDir(currentPath);
    
   }});
        
        Button btnLastDateModified = (Button)findViewById(R.id.button_lastDateModified);
        btnLastDateModified.setOnClickListener(new OnClickListener(){

   @Override
   public void onClick(View arg0) {
    comparator = filecomparatorByLastModified;
          getDir(currentPath);
    
   }});
    }
    
    private void getDir(String dirPath)
    {
     currentPath = dirPath;
     
     myPath.setText("Location: " + dirPath);
     item = new ArrayList<String>();
     path = new ArrayList<String>();
     File f = new File(dirPath);
     File[] files = f.listFiles();
     
     if(!dirPath.equals(root))
     {
      item.add(root);
      path.add(root);
      item.add("../");
      path.add(f.getParent()); 
     }
     
     Arrays.sort(files, comparator);
     
     for(int i=0; i < files.length; i++)
     {
      File file = files[i];
      
      if(!file.isHidden() && file.canRead()){
       path.add(file.getPath());
          if(file.isDirectory()){
           item.add(file.getName() + "/");
          }else{
           item.add(file.getName());
          }
      } 
     }

     ArrayAdapter<String> fileList =
       new ArrayAdapter<String>(this, R.layout.row, item);
     setListAdapter(fileList); 
    }
    
    Comparator<? super File> filecomparatorByLastModified = new Comparator<File>(){
  
  public int compare(File file1, File file2) {

   if(file1.isDirectory()){
    if (file2.isDirectory()){
     return Long.valueOf(file1.lastModified()).compareTo(file2.lastModified());
    }else{
     return -1;
    }
   }else {
    if (file2.isDirectory()){
     return 1;
    }else{
     return Long.valueOf(file1.lastModified()).compareTo(file2.lastModified());
    }
   }
    
  }  
 };
    
    Comparator<? super File> filecomparatorByAlphabetically = new Comparator<File>(){
  
  public int compare(File file1, File file2) {

   if(file1.isDirectory()){
    if (file2.isDirectory()){
     return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
    }else{
     return -1;
    }
   }else {
    if (file2.isDirectory()){
     return 1;
    }else{
     return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
    }
   }
    
  }  
 };
 
 @Override
 protected void onListItemClick(ListView l, View v, int position, long id) {
  // TODO Auto-generated method stub
  File file = new File(path.get(position));
  
  if (file.isDirectory())
  {
   if(file.canRead()){
    getDir(path.get(position));
   }else{
    new AlertDialog.Builder(this)
     .setIcon(R.drawable.ic_launcher)
     .setTitle("[" + file.getName() + "] folder can't be read!")
     .setPositiveButton("OK", null).show(); 
   } 
  }else {
	  Intent intent = new Intent(getApplicationContext(), Play.class);
	  startActivity(intent);
	  
	  
	  /*
   new AlertDialog.Builder(this)
     .setIcon(R.drawable.ic_launcher)
     .setTitle("[" + file.getName() + "]")
     .setPositiveButton("OK", null).show();
     */

    }
  
  
 }
 
 
 
 

}