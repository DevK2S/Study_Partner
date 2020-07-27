package com.studypartner.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ReminderItem implements Parcelable {
    private String time;
    private String date;
    private String Title;
    private String des;
    private int notifyId;
    private int position;

    public ReminderItem() {
    }

    public ReminderItem(String Title, String des, String time, String date) {
        this.Title = Title;
        this.time = time;
        this.date = date;
        this.des = des;
    }
    
    protected ReminderItem(Parcel in) {
        time = in.readString();
        date = in.readString();
        Title = in.readString();
        des = in.readString();
        notifyId = in.readInt();
        position = in.readInt();
    }
    
    public static final Creator<ReminderItem> CREATOR = new Creator<ReminderItem>() {
        @Override
        public ReminderItem createFromParcel(Parcel in) {
            return new ReminderItem(in);
        }
        
        @Override
        public ReminderItem[] newArray(int size) {
            return new ReminderItem[size];
        }
    };
    
    public void putPosition(int position) {
        this.position = position;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public void setDate(String setdate) {
        date = setdate;
    }

    public void setDes(String setDes) {
        des = setDes;
    }

    public void setTime(String setTime) {
        time = setTime;
    }

    public void Edit(String editTitle, String editDes, String editDate, String editTime) {
        Title = editTitle;
        des = editDes;
        date = editDate;
        time = editTime;
    }

    public String getTitle() {
        return Title;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getDes() {
        return des;
    }

    public int getPosition() {
        return position;
    }

    public int getnotifyId() {
        createNotifyId();
        return notifyId;
    }

    public void createNotifyId() {
        String dn = "0";
        String mdate = date.substring(0, 2) + date.substring(3, 5);
        if (time.substring(6).equals("PM"))
            dn = "1";
        String mtime = time.substring(0, 2) + time.substring(3, 5) + dn;
        notifyId = Integer.parseInt(mdate + mtime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(date);
        parcel.writeString(time);
        parcel.writeString(Title);
        parcel.writeString(des);
        parcel.writeInt(notifyId);
        parcel.writeInt(position);
    }

    @Override
    public String toString() {
        return "ReminderItem{" +
                "time='" + time + '\'' +
                ", date='" + date + '\'' +
                ", Title='" + Title + '\'' +
                ", des='" + des + '\'' +
                ", notifyId=" + notifyId +
                '}';
    }
}
