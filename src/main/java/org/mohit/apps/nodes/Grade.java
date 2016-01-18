package org.mohit.apps.nodes;

public class Grade {
	private double CPI;
	private String LetterGrade;
	private String creditEarned;
	
	public Grade(double cPI, String letterGrade, String creditEarned) {
		super();
		CPI = cPI;
		LetterGrade = letterGrade;
		this.creditEarned = creditEarned;
	}
	
	public double getCPI() {
		return CPI;
	}
	public String getLetterGrade() {
		return LetterGrade;
	}
	public String getCreditEarned() {
		return creditEarned;
	}	
}
