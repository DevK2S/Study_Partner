package com.studypartner.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.Patterns;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.studypartner.BuildConfig;
import com.studypartner.models.FileItem;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import okhttp3.HttpUrl;


public class FileUtils {
	public static final String DOCUMENTS_DIR = "documents";
	public static final String AUTHORITY =  "${applicationId}.provider";
	
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
	
	public static boolean isValidUrl (String link) {
	
		if (link.trim().isEmpty()) 	return false;
		else if (!URLUtil.isNetworkUrl(link)) return false;
		else if (!Patterns.WEB_URL.matcher(link).matches()) return false;
		else return HttpUrl.parse(link) != null;
		
	}
	
	public static void openLink(final Context context, final FileItem link) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Open Link");
		builder.setMessage("Do you want to open the link?");
		builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (isValidUrl(link.getName())) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link.getName()));
					context.startActivity(Intent.createChooser(browserIntent, "Select the app to open the link"));
				} else {
					Toast.makeText(context, "Link is invalid", Toast.LENGTH_SHORT).show();
				}
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			
			}
		});
		builder.show();
		
	}
	
	public static void openLink(final Context context, final String link) {
		
		if (isValidUrl(link)) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			context.startActivity(Intent.createChooser(browserIntent, "Select the app to open the link"));
		} else {
			Toast.makeText(context, "Link is invalid", Toast.LENGTH_SHORT).show();
		}
		
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
	
	public static String copyFile (String inputFilePath, String outputDirectoryPath) {
		
		String fileName = new File(inputFilePath).getName();
		
		String outputFilePath = new File(outputDirectoryPath, fileName).getPath();
		
		
		try (InputStream in = new FileInputStream(inputFilePath)) {
			
			OutputStream out = new FileOutputStream(outputFilePath);
			
			byte[] buffer = new byte[1024];
			
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			out.flush();
			out.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return outputFilePath;
	}
	
	public static String getFileType(ArrayList<FileItem> fileItems) {
		String fileType = "*/*";
		boolean text = false, app = false, image = false, video = false, audio = false, other = false;
		
		for (FileItem item : fileItems) {
			if (item.getType() == FileType.FILE_TYPE_FOLDER) {
				return null;
			} else if (item.getType() == FileType.FILE_TYPE_IMAGE) {
				image = true;
			} else if (item.getType() == FileType.FILE_TYPE_APPLICATION) {
				app = true;
			} else if (item.getType() == FileType.FILE_TYPE_TEXT) {
				text = true;
			} else if (item.getType() == FileType.FILE_TYPE_AUDIO) {
				audio = true;
			} else if (item.getType() == FileType.FILE_TYPE_VIDEO) {
				video = true;
			} else {
				other = true;
			}
		}
		
		if (text && !(app && image && video && audio && other)) {
			fileType = "text/*";
		} else if (app && !(text && image && video && audio && other)) {
			fileType = "application/*";
		} else if (image && !(text && app && video && audio && other)) {
			fileType = "image/*";
		} else if (video && !(text && app && image && audio && other)) {
			fileType = "video/*";
		} else if (audio && !(text && app && image && video && other)) {
			fileType = "audio/*";
		}
		
		return fileType;
	}
	
	/**
	 * @return Whether the URI is a local one.
	 */
	public static boolean isLocal(String url) {
		return url != null && !url.startsWith("http://") && !url.startsWith("https://");
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is local.
	 */
	public static boolean isLocalStorageDocument(Uri uri) {
		return AUTHORITY.equals(uri.getAuthority());
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}
	
	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context       The context.
	 * @param uri           The Uri to query.
	 * @param selection     (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
	                                   String[] selectionArgs) {
		
		Cursor cursor = null;
		final String column = MediaStore.Files.FileColumns.DATA;
		final String[] projection = {
				column
		};
		
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		}  catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}
	
	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.<br>
	 * <br>
	 * Callers should check whether the path is local before assuming it
	 * represents a local file.
	 *
	 * @param context The context.
	 * @param uri     The Uri to query.
	 * @see #isLocal(String)
	 */
	public static String getFilePath(final Context context, final Uri uri) {
		String absolutePath = getLocalPath(context, uri);
		return absolutePath != null ? absolutePath : uri.toString();
	}
	
	private static String getLocalPath(final Context context, final Uri uri) {
		
		// DocumentProvider
		if (DocumentsContract.isDocumentUri(context, uri)) {
			// LocalStorageProvider
			if (isLocalStorageDocument(uri)) {
				// The path is the id
				return DocumentsContract.getDocumentId(uri);
			}
			// ExternalStorageProvider
			else if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				} else if ("home".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/documents/" + split[1];
				} else {
					String sdFile = "/storage/" + type + "/" + split[1];
					return sdFile;
				}
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {
				
				final String id = DocumentsContract.getDocumentId(uri);
				
				if (id != null && id.startsWith("raw:")) {
					return id.substring(4);
				}
				
				String[] contentUriPrefixesToTry = new String[]{
						"content://downloads/public_downloads",
						"content://downloads/my_downloads"
				};
				
				for (String contentUriPrefix : contentUriPrefixesToTry) {
					Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
					try {
						String path = getDataColumn(context, contentUri, null, null);
						if (path != null) {
							return path;
						}
					} catch (Exception ignored) {}
				}
				
				// path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
				String fileName = getFileName(context, uri);
				File cacheDir = getDocumentCacheDir(context);
				File file = generateFileName(fileName, cacheDir);
				String destinationPath = null;
				if (file != null) {
					destinationPath = file.getAbsolutePath();
					saveFileFromUri(context, uri, destinationPath);
				}
				
				return destinationPath;
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				
				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{
						split[1]
				};
				
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			
			// Return the remote address
			if (isGooglePhotosUri(uri)) {
				return uri.getLastPathSegment();
			}
			
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		
		return null;
	}
	
	public static File getDocumentCacheDir(@NonNull Context context) {
		File dir = new File(context.getCacheDir(), DOCUMENTS_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	@Nullable
	public static File generateFileName(@Nullable String name, File directory) {
		if (name == null) {
			return null;
		}
		
		File file = new File(directory, name);
		
		if (file.exists()) {
			String fileName = name;
			String extension = "";
			int dotIndex = name.lastIndexOf('.');
			if (dotIndex > 0) {
				fileName = name.substring(0, dotIndex);
				extension = name.substring(dotIndex);
			}
			
			int index = 0;
			
			while (file.exists()) {
				index++;
				name = fileName + '(' + index + ')' + extension;
				file = new File(directory, name);
			}
		}
		
		try {
			if (!file.createNewFile()) {
				return null;
			}
		} catch (IOException e) {
			return null;
		}
		
		return file;
	}
	
	private static void saveFileFromUri(Context context, Uri uri, String destinationPath) {
		InputStream is = null;
		BufferedOutputStream bos = null;
		try {
			is = context.getContentResolver().openInputStream(uri);
			bos = new BufferedOutputStream(new FileOutputStream(destinationPath, false));
			byte[] buf = new byte[1024];
			is.read(buf);
			do {
				bos.write(buf);
			} while (is.read(buf) != -1);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) is.close();
				if (bos != null) bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getFileName(@NonNull Context context, Uri uri) {
		String mimeType = context.getContentResolver().getType(uri);
		String filename = null;
		
		if (mimeType == null) {
			String path = getFilePath(context, uri);
			File file = new File(path);
			filename = file.getName();
		} else {
			Cursor returnCursor = context.getContentResolver().query(uri, null,
					null, null, null);
			if (returnCursor != null) {
				int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
				returnCursor.moveToFirst();
				filename = returnCursor.getString(nameIndex);
				returnCursor.close();
			}
		}
		
		return filename;
	}
}