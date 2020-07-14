package com.studypartner.items;

public class OnBoardingItem {
	
	String title, description;
	int image;
	
	public OnBoardingItem(String title, String description, int image) {
		this.title = title;
		this.description = description;
		this.image = image;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getImage() {
		return image;
	}
	
}
