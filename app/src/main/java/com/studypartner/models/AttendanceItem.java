package com.studypartner.models;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AttendanceItem {
	private String id;
	private int attendedClasses, totalClasses, missedClasses, classesNeededToAttend;
	private double attendedPercentage, requiredPercentage;
	private String subjectName;
	
	public AttendanceItem() {}
	
	public AttendanceItem(String subjectName, double requiredPercentage, int attendedClasses, int missedClasses) {
		this.id = String.valueOf(UUID.randomUUID());
		this.subjectName = subjectName;
		this.requiredPercentage = requiredPercentage;
		this.attendedClasses = attendedClasses;
		this.missedClasses = missedClasses;
		this.totalClasses = attendedClasses + missedClasses;
		this.classesNeededToAttend = classesNeededToAttend();
		this.attendedPercentage = attendedPercentage();
	}
	
	public String getId() {
		return id;
	}
	
	public int getAttendedClasses() {
		return attendedClasses;
	}
	
	public int getTotalClasses() {
		return totalClasses;
	}
	
	public int getMissedClasses() {
		return totalClasses - attendedClasses;
	}
	
	public int getClassesNeededToAttend() {
		return classesNeededToAttend;
	}
	
	public double getAttendedPercentage() {
		return attendedPercentage;
	}
	
	public double getRequiredPercentage() {
		return requiredPercentage;
	}
	
	public String getSubjectName() {
		return subjectName;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setRequiredPercentage(double requiredPercentage) {
		this.requiredPercentage = requiredPercentage;
		this.attendedPercentage = attendedPercentage();
		this.classesNeededToAttend = classesNeededToAttend();
	}
	
	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}
	
	public void increaseAttendedClasses() {
		this.attendedClasses++;
		this.totalClasses++;
		this.attendedPercentage = attendedPercentage();
		this.classesNeededToAttend = classesNeededToAttend();
	}
	
	public void increaseMissedClasses() {
		this.missedClasses++;
		this.totalClasses++;
		this.attendedPercentage = attendedPercentage();
		this.classesNeededToAttend = classesNeededToAttend();
	}
	
	public void decreaseAttendedClasses() {
		if (this.totalClasses > 0 && this.attendedClasses > 0) {
			this.totalClasses--;
			this.attendedClasses--;
			this.attendedPercentage = attendedPercentage();
			this.classesNeededToAttend = classesNeededToAttend();
		}
	}
	
	public void decreaseMissedClasses() {
		if (this.totalClasses > 0 && this.missedClasses > 0) {
			this.totalClasses--;
			this.missedClasses--;
			this.attendedPercentage = attendedPercentage();
			this.classesNeededToAttend = classesNeededToAttend();
		}
	}
	
	private double attendedPercentage() {
		return (totalClasses == 0) ? 0 : (double) (attendedClasses * 100) / totalClasses;
	}
	
	private int classesNeededToAttend() {
		int classesNeeded;
		
		if (totalClasses == 0) {
			classesNeeded = 0;
		} else if (attendedPercentage < requiredPercentage) {
			classesNeeded = (int) Math.ceil((((requiredPercentage / 100) * totalClasses) - attendedClasses) / (1 - (requiredPercentage / 100)));
		} else {
			classesNeeded = (int) ((((requiredPercentage / 100) * totalClasses) - attendedClasses) / (requiredPercentage / 100));
		}
		return classesNeeded;
	}
	
	@NotNull
	@Override
	public String toString() {
		return "AttendanceItem{" +
				"id='" + id + '\'' +
				", attendedClasses=" + attendedClasses +
				", totalClasses=" + totalClasses +
				", missedClasses=" + missedClasses +
				", classesNeededToAttend=" + classesNeededToAttend +
				", attendedPercentage=" + attendedPercentage +
				", requiredPercentage=" + requiredPercentage +
				", subjectName='" + subjectName + '\'' +
				'}';
	}
}