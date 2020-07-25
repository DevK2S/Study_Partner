package com.studypartner.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;
import com.studypartner.BuildConfig;
import com.studypartner.R;
import com.studypartner.models.FileItem;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import okhttp3.ResponseBody;

import static com.studypartner.BuildConfig.DEBUG;


public class FileUtils {
	public static final String DOCUMENTS_DIR = "documents";
	public static final String AUTHORITY =  "${applicationId}.provider";
	public static final String HIDDEN_PREFIX = ".";
	
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

	}
	
	public static void playAudio(Context context, FileItem audio) {

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
	
	public static String copyFile (String inputFilePath, String outputDirectoryPath) {
		
		String fileName = new File(inputFilePath).getName();
		
		String outputFilePath = new File(outputDirectoryPath, fileName).getPath();
		
		Log.d(TAG, "copyFile: from " + inputFilePath + " to " + outputFilePath);
		
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
	 * Gets the extension of a file name, like ".png" or ".jpg".
	 *
	 * @param uri
	 * @return Extension including the dot("."); "" if there is no extension;
	 * null if uri was null.
	 */
	public static String getExtension(String uri) {
		if (uri == null) {
			return null;
		}
		
		int dot = uri.lastIndexOf(".");
		if (dot >= 0) {
			return uri.substring(dot);
		} else {
			// No extension.
			return "";
		}
	}
	
	/**
	 * @return Whether the URI is a local one.
	 */
	public static boolean isLocal(String url) {
		return url != null && !url.startsWith("http://") && !url.startsWith("https://");
	}
	
	/**
	 * @return True if Uri is a MediaStore Uri.
	 * @author paulburke
	 */
	public static boolean isMediaUri(Uri uri) {
		return "media".equalsIgnoreCase(uri.getAuthority());
	}
	
	/**
	 * Convert File into Uri.
	 *
	 * @param file
	 * @return uri
	 */
	public static Uri getUri(File file) {
		return (file != null) ? Uri.fromFile(file) : null;
	}
	
	/**
	 * Returns the path only (without file name).
	 *
	 * @param file
	 * @return
	 */
	public static File getPathWithoutFilename(File file) {
		if (file != null) {
			if (file.isDirectory()) {
				// no file to be split off. Return everything
				return file;
			} else {
				String filename = file.getName();
				String filepath = file.getAbsolutePath();
				
				// Construct path without file name.
				String pathwithoutname = filepath.substring(0,
						filepath.length() - filename.length());
				if (pathwithoutname.endsWith("/")) {
					pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
				}
				return new File(pathwithoutname);
			}
		}
		return null;
	}
	
	/**
	 * @return The MIME type for the given file.
	 */
	public static String getMimeType(File file) {
		
		String extension = getExtension(file.getName());
		
		if (extension.length() > 0)
			return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));
		
		return "application/octet-stream";
	}
	
	/**
	 * @return The MIME type for the give Uri.
	 */
	public static String getMimeType(Context context, Uri uri) {
		File file = new File(getFilePath(context, uri));
		return getMimeType(file);
	}
	
	/**
	 * @return The MIME type for the give String Uri.
	 */
	public static String getMimeType(Context context, String url) {
		String type = context.getContentResolver().getType(Uri.parse(url));
		if (type == null) {
			type = "application/octet-stream";
		}
		return type;
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
	 * @see #getFile(Context, Uri)
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
					} catch (Exception e) {}
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
	
	
	/**
	 * Convert Uri into File, if possible.
	 *
	 * @return file A local file that the Uri was pointing to, or null if the
	 * Uri is unsupported or pointed to a remote resource.
	 * @author paulburke
	 * @see #getFilePath(Context, Uri)
	 */
	public static File getFile(Context context, Uri uri) {
		if (uri != null) {
			String path = getFilePath(context, uri);
			if (path != null && isLocal(path)) {
				return new File(path);
			}
		}
		return null;
	}
	
	/**
	 * Get the file size in a human-readable string.
	 *
	 * @param size
	 * @return
	 * @author paulburke
	 */
	public static String getReadableFileSize(int size) {
		final int BYTES_IN_KILOBYTES = 1024;
		final DecimalFormat dec = new DecimalFormat("###.#");
		final String KILOBYTES = " KB";
		final String MEGABYTES = " MB";
		final String GIGABYTES = " GB";
		float fileSize = 0;
		String suffix = KILOBYTES;
		
		if (size > BYTES_IN_KILOBYTES) {
			fileSize = size / BYTES_IN_KILOBYTES;
			if (fileSize > BYTES_IN_KILOBYTES) {
				fileSize = fileSize / BYTES_IN_KILOBYTES;
				if (fileSize > BYTES_IN_KILOBYTES) {
					fileSize = fileSize / BYTES_IN_KILOBYTES;
					suffix = GIGABYTES;
				} else {
					suffix = MEGABYTES;
				}
			}
		}
		return String.valueOf(dec.format(fileSize) + suffix);
	}
	
	/**
	 * Get the Intent for selecting content to be used in an Intent Chooser.
	 *
	 * @return The intent for opening a file with Intent.createChooser()
	 */
	public static Intent createGetContentIntent() {
		// Implicitly allow the user to select a particular kind of data
		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		// The MIME data type filter
		intent.setType("*/*");
		// Only return URIs that can be opened with ContentResolver
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		return intent;
	}
	
	
	/**
	 * Creates View intent for given file
	 *
	 * @param file
	 * @return The intent for viewing file
	 */
	public static Intent getViewIntent(Context context, File file) {
		//Uri uri = Uri.fromFile(file);
		Uri uri = FileProvider.getUriForFile(context, AUTHORITY, file);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		String url = file.toString();
		if (url.contains(".doc") || url.contains(".docx")) {
			// Word document
			intent.setDataAndType(uri, "application/msword");
		} else if (url.contains(".pdf")) {
			// PDF file
			intent.setDataAndType(uri, "application/pdf");
		} else if (url.contains(".ppt") || url.contains(".pptx")) {
			// Powerpoint file
			intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
		} else if (url.contains(".xls") || url.contains(".xlsx")) {
			// Excel file
			intent.setDataAndType(uri, "application/vnd.ms-excel");
		} else if (url.contains(".zip") || url.contains(".rar")) {
			// WAV audio file
			intent.setDataAndType(uri, "application/x-wav");
		} else if (url.contains(".rtf")) {
			// RTF file
			intent.setDataAndType(uri, "application/rtf");
		} else if (url.contains(".wav") || url.contains(".mp3")) {
			// WAV audio file
			intent.setDataAndType(uri, "audio/x-wav");
		} else if (url.contains(".gif")) {
			// GIF file
			intent.setDataAndType(uri, "image/gif");
		} else if (url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png")) {
			// JPG file
			intent.setDataAndType(uri, "image/jpeg");
		} else if (url.contains(".txt")) {
			// Text file
			intent.setDataAndType(uri, "text/plain");
		} else if (url.contains(".3gp") || url.contains(".mpg") || url.contains(".mpeg") ||
				url.contains(".mpe") || url.contains(".mp4") || url.contains(".avi")) {
			// Video files
			intent.setDataAndType(uri, "video/*");
		} else {
			intent.setDataAndType(uri, "*/*");
		}
		
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		return intent;
	}
	
	public static File getDownloadsDir() {
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
	}
	
	public static File getDocumentCacheDir(@NonNull Context context) {
		File dir = new File(context.getCacheDir(), DOCUMENTS_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		logDir(context.getCacheDir());
		logDir(dir);
		
		return dir;
	}
	
	private static void logDir(File dir) {
		if(!DEBUG) return;
		Log.d(TAG, "Dir=" + dir);
		File[] files = dir.listFiles();
		for (File file : files) {
			Log.d(TAG, "File=" + file.getPath());
		}
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
			Log.w(TAG, e);
			return null;
		}
		
		logDir(directory);
		
		return file;
	}
	
	/**
	 * Writes response body to disk
	 *
	 * @param body ResponseBody
	 * @param path file path
	 * @return File
	 */
	public static File writeResponseBodyToDisk(ResponseBody body, String path) {
		try {
			File target = new File(path);
			
			InputStream inputStream = null;
			OutputStream outputStream = null;
			
			try {
				byte[] fileReader = new byte[4096];
				
				inputStream = body.byteStream();
				outputStream = new FileOutputStream(target);
				
				while (true) {
					int read = inputStream.read(fileReader);
					
					if (read == -1) {
						break;
					}
					
					outputStream.write(fileReader, 0, read);
				}
				
				outputStream.flush();
				
				return target;
			} catch (IOException e) {
				return null;
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
				
				if (outputStream != null) {
					outputStream.close();
				}
			}
		} catch (IOException e) {
			return null;
		}
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
	
	public static byte[] readBytesFromFile(String filePath) {
		
		FileInputStream fileInputStream = null;
		byte[] bytesArray = null;
		
		try {
			
			File file = new File(filePath);
			bytesArray = new byte[(int) file.length()];
			
			//read file into bytes[]
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bytesArray);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		return bytesArray;
		
	}
	
	public static File createTempImageFile(Context context, String fileName) throws IOException {
		// Create an image file name
		File storageDir = new File(context.getCacheDir(), DOCUMENTS_DIR);
		return File.createTempFile(fileName, ".jpg", storageDir);
	}
	
	public static String getFileName(@NonNull Context context, Uri uri) {
		String mimeType = context.getContentResolver().getType(uri);
		String filename = null;
		
		if (mimeType == null && context != null) {
			String path = getFilePath(context, uri);
			if (path == null) {
				filename = getName(uri.toString());
			} else {
				File file = new File(path);
				filename = file.getName();
			}
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
	
	public static String getName(String filename) {
		if (filename == null) {
			return null;
		}
		int index = filename.lastIndexOf('/');
		return filename.substring(index + 1);
	}
}