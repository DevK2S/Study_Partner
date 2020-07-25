package com.studypartner.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;
import com.studypartner.BuildConfig;
import com.studypartner.R;
import com.studypartner.models.FileItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.core.content.FileProvider;


public class FileUtils {
	
	private static final String TAG = "FileUtil";
	final static HashMap<String, FileType> types = new HashMap<>();
	
	static void createMap() {
		types.put("image", FileType.FILE_TYPE_IMAGE);
		types.put("applicaton", FileType.FILE_TYPE_APPLICATION);
		types.put("text", FileType.FILE_TYPE_TEXT);
		types.put("audio", FileType.FILE_TYPE_AUDIO);
		types.put("video", FileType.FILE_TYPE_VIDEO);
	}
	
	public static FileType getFileType(File file) {
		FileType ft = FileType.FILE_TYPE_OTHER;
		if (file.isDirectory())
			ft = FileType.FILE_TYPE_FOLDER;
		else {
			String extension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.getPath()));
			if (extension != null) {
				extension = extension.substring(0, extension.indexOf('/'));
				createMap();
				if (types.containsKey(extension))
					ft = types.get(extension);
			}
		}
		return ft;
	}
	
	public static void showImage(Context context, FileItem image) {
		File[] files = new File(image.getPath()).getParentFile().listFiles();
		ArrayList<FileItem> images = new ArrayList<>();
		
		int position = 0;
		
		if (files != null) {
			for (File file : files) {
				FileItem item = new FileItem(file.getPath());
				if (item.getType() == FileType.FILE_TYPE_IMAGE) {
					if (item.getPath().equals(image.getPath())) {
						position = images.size();
					}
					images.add(item);
				}
			}
			
			new StfalconImageViewer.Builder<>(context, images, new ImageLoader<FileItem>() {
				@Override
				public void loadImage(ImageView imageView, FileItem image) {
					Picasso.get()
							.load(new File(image.getPath()))
							.into(imageView);
				}
			})
					.withStartPosition(position)
					.withBackgroundColorResource(R.color.colorAccent)
					.withContainerPadding(R.dimen.smallPadding)
					.show();
		}
	}
	
	public static void showVideo(Activity activity, FileItem video) {
		Intent tostart = new Intent(Intent.ACTION_VIEW);
		tostart.setDataAndType(Uri.parse(video.getPath()), "video/*");
		activity.startActivity(tostart);
	}
	
	public static void playAudio(Context context, FileItem audio) {
		Intent audioplay = new Intent(Intent.ACTION_VIEW);
		audioplay.setDataAndType(Uri.parse(audio.getPath()), "audio/*");
		context.startActivity(audioplay);
	}
	
	public static void openLink(Uri link) {
	
	}
	
	public static void openFile(Context context, FileItem file) {
		
		Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(file.getPath()));
		
		Intent target = new Intent(Intent.ACTION_VIEW);
		if (uri.toString().contains(".doc") || uri.toString().contains(".docx")) {
			target.setDataAndType(uri,"application/msword");
		} else if(uri.toString().contains(".pdf")) {
			target.setDataAndType(uri,"application/pdf");
		} else if(uri.toString().contains(".ppt") || uri.toString().contains(".pptx")) {
			target.setDataAndType(uri,"application/vnd.ms-powerpoint");
		} else if(uri.toString().contains(".xls") || uri.toString().contains(".xlsx")) {
			target.setDataAndType(uri,"application/vnd.ms-excel");
		} else if(uri.toString().contains(".zip") || uri.toString().contains(".rar")) {
			target.setDataAndType(uri,"application/zip");
		} else if(uri.toString().contains(".rtf")) {
			target.setDataAndType(uri,"application/rtf");
		}  else if(uri.toString().contains(".txt")) {
			target.setDataAndType(uri,"text/plain");
		} else {
			target.setDataAndType(uri, "*/*");
		}
		
		target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		target.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		
		Intent intent = Intent.createChooser(target, "Open file");
		
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, "No application found to open this file", Toast.LENGTH_SHORT).show();
		}
	}
	
	@SuppressLint("NewApi")
	public static String getFilePath(Context context, Uri uri) {
		if(isWhatsAppFile(uri)){
			return getFilePathForWhatsApp(context,uri);
		}
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			String selection = null;
			String[] selectionArgs = null;
			
			if (DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
				if (isExternalStorageDocument(uri)) {
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				} else if (isDownloadsDocument(uri)) {
					final String id = DocumentsContract.getDocumentId(uri);
					uri = ContentUris.withAppendedId(
							Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				} else if (isMediaDocument(uri)) {
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					final String type = split[0];
					if ("image".equals(type)) {
						uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
					} else if ("video".equals(type)) {
						uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
					} else if ("audio".equals(type)) {
						uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
					}
					selection = "_id=?";
					selectionArgs = new String[]{
							split[1]
					};
				}
			}
			if ("content".equalsIgnoreCase(uri.getScheme())) {
				
				if (isGooglePhotosUri(uri)) {
					return uri.getLastPathSegment();
				}
				
				String[] projection = {
						MediaStore.Images.Media.DATA
				};
				Cursor cursor = null;
				try {
					cursor = context.getContentResolver()
							.query(uri, projection, selection, selectionArgs, null);
					int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					if (cursor.moveToFirst()) {
						return cursor.getString(column_index);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("file".equalsIgnoreCase(uri.getScheme())) {
				return uri.getPath();
			}
		} else {
			File file = new File(uri.getPath());
			final String[] split = file.getPath().split(":");
			return split[1];
		}
		return null;
	}
	
	private static String copyFileToInternalStorage(Context context, Uri uri, String newDirName) {
		Uri returnUri = uri;
		
		Cursor returnCursor = context.getContentResolver().query(returnUri, new String[]{
				OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE
		}, null, null, null);
		
		int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
		int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
		returnCursor.moveToFirst();
		String name = (returnCursor.getString(nameIndex));
		String size = (Long.toString(returnCursor.getLong(sizeIndex)));
		
		File output;
		if(!newDirName.equals("")) {
			File dir = new File(context.getFilesDir() + "/" + newDirName);
			if (!dir.exists()) {
				dir.mkdir();
			}
			output = new File(context.getFilesDir() + "/" + newDirName + "/" + name);
		}
		else{
			output = new File(context.getFilesDir() + "/" + name);
		}
		try {
			InputStream inputStream = context.getContentResolver().openInputStream(uri);
			FileOutputStream outputStream = new FileOutputStream(output);
			int read = 0;
			int bufferSize = 1024;
			final byte[] buffers = new byte[bufferSize];
			while ((read = inputStream.read(buffers)) != -1) {
				outputStream.write(buffers, 0, read);
			}
			
			inputStream.close();
			outputStream.close();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return output.getPath();
	}
	
	private static String getFilePathForWhatsApp(Context context, Uri uri){
		return copyFileToInternalStorage(context, uri,"whatsapp");
	}
	
	private static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}
	
	private static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}
	
	private static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	
	private static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}
	
	public static boolean isWhatsAppFile(Uri uri){
		return "com.whatsapp.provider.media".equals(uri.getAuthority());
	}
	
}