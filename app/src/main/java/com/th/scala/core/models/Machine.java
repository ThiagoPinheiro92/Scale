package com.th.scala.core.models;

public class Machine {
	private final String name;
	private final int number;
	private String operator;
	private final String product;
	private int difficulty;
	private final boolean requiresSpecialTraining;
	
	public Machine(String name, int number, String operator, String product,
	int difficulty, boolean requiresSpecialTraining) {
		this.name = name;
		this.number = number;
		this.operator = operator;
		this.product = product;
		this.difficulty = difficulty;
		this.requiresSpecialTraining = requiresSpecialTraining;
	}
	
	// Getters e Setters
	public String getName() { return name; }
	public int getNumber() { return number; }
	public String getOperator() { return operator; }
	public String getProduct() { return product; }
	public int getDifficulty() { return difficulty; }
	public boolean requiresSpecialTraining() { return requiresSpecialTraining; }
	
	public void setOperator(String operator) { this.operator = operator; }
	public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
	
}