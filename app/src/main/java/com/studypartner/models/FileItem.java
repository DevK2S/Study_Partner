package com.studypartner.models;

import android.os.Parcel;
import android.os.Parcelable;

public class FileItem implements Parcelable {
	
	public enum FileType {
		FILE_TYPE_FOLDER,
		FILE_TYPE_IMAGE,
		FILE_TYPE_VIDEO,
		FILE_TYPE_PDF,
		FILE_TYPE_OTHER
	}
	
	private String path;
	private String name;
	private FileType type;
	private String id;
	
	public FileItem() {
	}
	
	public FileItem(String path, String name, FileType type) {
		this.path = path;
		this.name = name;
		this.type = type;
	}
	
	protected FileItem(Parcel in) {
		path = in.readString();
		name = in.readString();
		type = FileType.valueOf(in.readString());
		id = in.readString();
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
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(path);
		parcel.writeString(name);
		parcel.writeString(type.name());
		parcel.writeString(id);
	}
	//  public String getId(){return id;}
 
	@Override
	public String toString() {
		return "FileItem{" +
				"path='" + path + '\'' +
				", Name='" + name + '\'' +
				", Type='" + type + '\'' +
				", id='" + id + '\'' +
				'}';
	}
}
