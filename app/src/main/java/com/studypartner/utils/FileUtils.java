package com.studypartner.utils;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.stfalcon.imageviewer.StfalconImageViewer;
import com.studypartner.models.FileItem;

import java.io.File;
import java.util.HashMap;


public class FileUtils {

	private static final String TAG = "FileUtil" ;
	final static HashMap<String,FileType> types=new HashMap<>();
	static void createMap()
	{
		types.put("images",FileType.FILE_TYPE_IMAGE);
		types.put("applicaton",FileType.FILE_TYPE_APPLICATION);
		types.put("text",FileType.FILE_TYPE_TEXT);
		types.put("audio",FileType.FILE_TYPE_AUDIO);
		types.put("video",FileType.FILE_TYPE_VIDEO);
	}

	public static FileType getFileType (File file) {
		FileType ft=FileType.FILE_TYPE_VIDEO;
		if(file.isDirectory())
			ft=FileType.FILE_TYPE_FOLDER;
		else {
		String extension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.getPath()));
		extension=extension.substring(0,extension.indexOf('/'));
		createMap();
		if(types.containsKey(extension))
		ft=types.get(extension);
		}
		return ft;
	}


	public static void showImage (FileItem image) {

	}
	
	public static void showVideo (Context context,FileItem video) {
		Intent tostart = new Intent(Intent.ACTION_VIEW);
		tostart.setDataAndType(Uri.parse(video.getPath()), "video/*");
		context.startActivity(tostart);
	}


	public static void playAudio (Context context,FileItem audio) {
		Intent audioplay = new Intent(Intent.ACTION_VIEW);
		audioplay.setDataAndType(Uri.parse(audio.getPath()), "audio/*");
		context.startActivity(audioplay);
	}
	
	public static void openLink (Uri link) {
	
	}
	
	public static void openFile (FileItem file) {
	
	}
}