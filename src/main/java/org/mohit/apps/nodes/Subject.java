package org.mohit.apps.nodes;

public class Subject {
	private String name;
	private String credits;
	private String courseNumber;
	
	public Subject(String name, String credits, String courseNumber) {
		this.name = name;
		this.credits = credits;
		this.courseNumber = courseNumber;
	}

	public String getName() {
		return name;
	}

	public String getCredits() {
		return credits;
	}

	public String getCourseNumber() {
		return courseNumber;
	}
}
