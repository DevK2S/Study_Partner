package com.studypartner.models;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.*;
public class FileItem implements Parcelable {
    private String path;
    private String Name;
    private String Type;
    private String id;

    public FileItem(){}

    public FileItem(String path,String Name,String Type)
    {
        this.path=path;
        this.Name=Name;
        this.Type=Type;
        //this.id=id;
    }

    protected FileItem(Parcel in) {
        path = in.readString();
        Name = in.readString();
        Type = in.readString();
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

    public String getPath(){ return path;}
    public String getName(){return Name;}
    public String getType(){return Type;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(path);
        parcel.writeString(Name);
        parcel.writeString(Type);
        parcel.writeString(id);
    }
    //  public String getId(){return id;}


    @Override
    public String toString() {
        return "FileItem{" +
                "path='" + path + '\'' +
                ", Name='" + Name + '\'' +
                ", Type='" + Type + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
