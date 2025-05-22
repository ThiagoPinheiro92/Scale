package com.th.scala;

public class Machine {
	private String name;
	private int number;
	private String operator;
	private String product;
	private int difficulty;
	
	public Machine(String name, int number, String operator, String product, int difficulty) {
		this.name = name;
		this.number = number;
		this.operator = operator;
		this.product = product;
		this.difficulty = difficulty;
	}
	
	// Getters e Setters para todos os campos
	public String getName() { return name; }
	public int getNumber() { return number; }
	public String getOperator() { return operator; }
	public String getProduct() { return product; }
	public int getDifficulty() { return difficulty; }
	
	public void setOperator(String operator) { this.operator = operator; }
	public void setProduct(String product) { this.product = product; }
	public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
}