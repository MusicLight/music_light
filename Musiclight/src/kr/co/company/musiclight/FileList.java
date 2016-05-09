package kr.co.company.musiclight;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FileList extends ListView {

	public FileList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context);
	}

	public FileList(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	public FileList(Context context) {
		super(context);

		init(context);
	}
	
	private void init(Context context) {
		_Context = context;
        setOnItemClickListener(_OnItemClick);
	}
	
	private Context _Context = null;
	private ArrayList<String> _List = new ArrayList<String>();
    private ArrayList<String> _FolderList = new ArrayList<String>();
    private ArrayList<String> _FileList = new ArrayList<String>();
	private ArrayAdapter<String> _Adapter = null; 
	
	// Property 
	private String _Path = "";
	
	// Event
	private OnPathChangedListener _OnPathChangedListener = null;
	private OnFileSelectedListener _OnFileSelectedListener = null;
	
	private boolean openPath(String path) {
		_FolderList.clear();
		_FileList.clear();
		
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) return false;
        
        for (int i=0; i<files.length; i++) {
        	if (files[i].isDirectory()) {
        		_FolderList.add("<" + files[i].getName() + ">");
        	} else {
        		_FileList.add(files[i].getName());
        	}
        }
        
        Collections.sort(_FolderList);
        Collections.sort(_FileList);
        
		if(! path.equals("/"))
			_FolderList.add(0, "<뒤로가기>");
        
        return true;
	}
	
	private void updateAdapter() {
		_List.clear();
        _List.addAll(_FolderList);
        _List.addAll(_FileList);
        
		_Adapter = new ArrayAdapter<String>(_Context, android.R.layout.simple_list_item_1, _List);
        setAdapter(_Adapter);
	}

	public void setPath(String value) {
		
		if (value.length() == 0) {
			value = "/";
		} else {
			String lastChar = value.substring(value.length()-1, value.length());
			if (lastChar.matches("/") == false) value = value + "/"; 
		}
		
		if (openPath(value)) {
			_Path = value;
			updateAdapter();	        
			if (_OnPathChangedListener != null) _OnPathChangedListener.onChanged(value);
		}
	}

	public String getPath() {
		return _Path;
	}
	
	public void setOnPathChangedListener(OnPathChangedListener value) {
		_OnPathChangedListener = value;
	}

	public OnPathChangedListener getOnPathChangedListener() {
		return _OnPathChangedListener;
	}

	public void setOnFileSelected(OnFileSelectedListener value) {
		_OnFileSelectedListener = value;
	}

	public OnFileSelectedListener getOnFileSelected() {
		return _OnFileSelectedListener;
	}
	
	public String DelteRight(String value, String border) {
		String list[] = value.split(border);

		String result = "";
		
		for (int i=0; i<list.length; i++) {
			result = result + list[i] + border; 
		}
		
		return result;
	}
	
	private String delteLastFolder(String value) {
		String list[] = value.split("/");

		String result = "";
		
		for (int i=0; i<list.length-1; i++) {
			result = result + list[i] + "/"; 
		}
		
		return result;
	}
	
	/**
	 * int position을 따로 추가하였습니다
	 * 그 이유는 폴더 이름이 뒤로가기일경우 뒤로가기 처리가 되기 때문에 맨위에 있는 뒤로가기(position은 0)일때만
	 * 상단 폴더로 넘어가도록 처리하였습니다
	 */
	private String getRealPathName(String newPath, int position) {
		String path = newPath.substring(1, newPath.length()-1);
		if (path.matches("뒤로가기") && position == 0) {
			return delteLastFolder(_Path);
		} else {
			return _Path + path + "/";
		}
	}

	private AdapterView.OnItemClickListener _OnItemClick = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long id) {
			String fileName = getItemAtPosition(position).toString();
			if (fileName.matches("<.*>")) {
				setPath(getRealPathName(fileName, position));
			} else {
				if (_OnFileSelectedListener != null) _OnFileSelectedListener.onSelected(_Path, fileName);
			}
			/**
			 * 파일/폴더를 선택할경우 두번눌러 어플 종료 카운터를 초기화 합니다
			 */
			FileExplorer.kill = 0;
		}
	};

}
