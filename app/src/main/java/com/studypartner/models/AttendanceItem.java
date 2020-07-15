package com.studypartner.models;

public class AttendanceItem {
	private int attendedClasses, totalClasses, missedClasses, classesNeededToAttend;
	private double attendedPercentage, requiredPercentage;
	private String subjectName;
	
	public AttendanceItem(String subjectName, double requiredPercentage, int attendedClasses, int missedClasses) {
		this.subjectName = subjectName;
		this.requiredPercentage = requiredPercentage;
		this.attendedClasses = attendedClasses;
		this.missedClasses = missedClasses;
		this.totalClasses = attendedClasses + missedClasses;
		this.classesNeededToAttend = classesNeededToAttend();
		this.attendedPercentage = attendedPercentage();
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
	
	public void setAttendedClasses(int attendedClasses) {
		this.attendedClasses = attendedClasses;
	}
	
	public void setMissedClasses(int missedClasses) {
		this.missedClasses = missedClasses;
	}
	
	public void setRequiredPercentage(double requiredPercentage) {
		this.requiredPercentage = requiredPercentage;
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
		this.attendedClasses--;
		this.totalClasses--;
		this.attendedPercentage = attendedPercentage();
		this.classesNeededToAttend = classesNeededToAttend();
	}
	
	public void decreaseMissedClasses() {
		this.missedClasses--;
		this.totalClasses--;
		this.attendedPercentage = attendedPercentage();
		this.classesNeededToAttend = classesNeededToAttend();
	}
	
	private double attendedPercentage() {
		return (double) (attendedClasses * 100) / totalClasses;
	}
	
	private int classesNeededToAttend() {
		int classesNeeded;
		
		if (attendedPercentage < requiredPercentage) {
			classesNeeded = (int) Math.ceil((((requiredPercentage / 100) * totalClasses) - attendedClasses) / (1 - (requiredPercentage / 100)));
		} else {
			classesNeeded = (int) ((((requiredPercentage / 100) * totalClasses) - attendedClasses) / (requiredPercentage / 100));
		}
		return classesNeeded;
	}
}
