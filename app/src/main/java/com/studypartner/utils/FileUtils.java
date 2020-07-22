package com.studypartner.utils;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.studypartner.models.FileItem;

import java.io.File;

public class FileUtils {
	
	public static FileType getFileType (File file) {
		MimeTypeMap.getFileExtensionFromUrl(file.getPath());
		return FileType.FILE_TYPE_FOLDER;
	}

	public static void showImage (FileItem image) {
	
	}
	
	public static void showVideo (FileItem video) {
	
	}
	
	public static void playAudio (FileItem audio) {
	
	}
	
	public static void openLink (Uri link) {
	
	}
	
	public static void openFile (FileItem file) {
	
	}
}