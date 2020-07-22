package com.studypartner.models;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.studypartner.utils.FileType;
import com.studypartner.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class FileItem implements Parcelable {
	
	private String path;
	private String name;
	private FileType type;
	private String dateCreated, dateModified;
	private boolean isStarred;
	private long size;
	
	public FileItem() {
	}
	
	public FileItem(String path) {
		File file = new File(path);
		this.path = path;
		this.name = file.getName();
		this.type = FileUtils.getFileType(file);
		this.dateModified = String.valueOf(file.lastModified());
		this.isStarred = false;
		this.size = getFolderSize(file);
		try {
			setCreationTime(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static long getFolderSize(File file) {
		long size = 0;
		if (file.isDirectory()) {
			for (File f : Objects.requireNonNull(file.listFiles())) {
				size += getFolderSize(f);
			}
		} else {
			size = file.length();
		}
		return size;
	}
	
	private void setCreationTime(File file) throws IOException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			this.dateCreated = Files.readAttributes(file.toPath(), BasicFileAttributes.class).creationTime().toString();
		} else {
			this.dateCreated = String.valueOf(file.lastModified());
		}
	}
	
	protected FileItem(Parcel in) {
		path = in.readString();
		name = in.readString();
		type = FileType.valueOf(in.readString());
		size = in.readLong();
		dateModified = in.readString();
		dateCreated = in.readString();
		isStarred = Boolean.parseBoolean(in.readString());
	}
	
	public static final Creator<FileItem> CREATOR = new Creator<FileItem>() {
		@Override
		public FileItem createFromParcel(Parcel in) {
			return new FileItem(in);
		}
		
		@Override
		public FileItem[] newArray(int size) {
			return new FileItem[size];
		}
	};
	
	public String getPath() {
		return path;
	}
	
	public String getName() {
		return name;
	}
	
	public FileType getType() {
		return type;
	}
	
	public long getSize() {
		return size;
	}
	
	public String getDateCreated() {
		return dateCreated;
	}
	
	public String getDateModified() {
		return dateModified;
	}
	
	public boolean isStarred() {
		return isStarred;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
	}
	
	public void setStarred(boolean starred) {
		isStarred = starred;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(path);
		parcel.writeString(name);
		parcel.writeString(type.name());
		parcel.writeLong(size);
		parcel.writeString(dateModified);
		parcel.writeString(dateCreated);
		parcel.writeString(String.valueOf(isStarred));
	}
	
	@Override
	public String toString() {
		return "FileItem{" +
				"path='" + path + '\'' +
				", name='" + name + '\'' +
				", type=" + type +
				", size=" + size + '\'' +
				", dateCreated='" + dateCreated + '\'' +
				", dateModified='" + dateModified + '\'' +
				", isStarred=" + isStarred +
				'}';
	}
}
