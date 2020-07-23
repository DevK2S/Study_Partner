package com.studypartner.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;
import com.studypartner.R;
import com.studypartner.models.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


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
		FileType ft = FileType.FILE_TYPE_VIDEO;
		if (file.isDirectory())
			ft = FileType.FILE_TYPE_FOLDER;
		else {
			String extension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.getPath()));
			extension = extension.substring(0, extension.indexOf('/'));
			createMap();
			if (types.containsKey(extension))
				ft = types.get(extension);
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
		/*Intent tostart = new Intent(Intent.ACTION_VIEW);
		tostart.setDataAndType(Uri.parse(video.getPath()), "video/*");
		context.startActivity(tostart);*/
	}
	
	public static void playAudio(Context context, FileItem audio) {
		Intent audioplay = new Intent(Intent.ACTION_VIEW);
		audioplay.setDataAndType(Uri.parse(audio.getPath()), "audio/*");
		context.startActivity(audioplay);
	}
	
	public static void openLink(Uri link) {
	
	}
	
	public static void openFile(FileItem file) {
	
	}
}