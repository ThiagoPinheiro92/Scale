package com.th.scala;

public class Machine {
	private String name;
	private int number;
	private String operator;
	private String product;
	private int difficulty;
	private boolean requiresSpecialTraining;
	private int position; // Posição física da máquina
	
	public Machine(String name, int number, String operator, String product,
	int difficulty, boolean requiresSpecialTraining, int position) {
		this.name = name;
		this.number = number;
		this.operator = operator;
		this.product = product;
		this.difficulty = difficulty;
		this.requiresSpecialTraining = requiresSpecialTraining;
		this.position = position;
	}
	
	// Getters e Setters
	public String getName() { return name; }
	public int getNumber() { return number; }
	public String getOperator() { return operator; }
	public String getProduct() { return product; }
	public int getDifficulty() { return difficulty; }
	public boolean requiresSpecialTraining() { return requiresSpecialTraining; }
	public int getPosition() { return position; }
	
	public void setOperator(String operator) { this.operator = operator; }
	public void setProduct(String product) { this.product = product; }
	public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
}