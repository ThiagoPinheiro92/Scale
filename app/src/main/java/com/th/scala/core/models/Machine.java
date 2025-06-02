package com.th.scala.core.models;

public class Machine {
	private String name;
	private int number;
	private String operator;
	private String product;
	private int difficulty; // 1: Easy, 2: Medium, 3: Difficult
	private boolean heavy;
	private boolean isOn; // Estado da máquina (ligada/desligada)
	
	// Construtor
	public Machine(String name, int number, String operator, String product, int difficulty, boolean heavy) {
		this.name = name;
		this.number = number;
		this.operator = operator;
		this.product = product;
		this.difficulty = difficulty;
		this.heavy = heavy;
		this.isOn = true; // Por padrão, a máquina está ligada
	}
	
	// Getters
	public String getName() { return name; }
	public int getNumber() { return number; }
	public String getOperator() { return operator; }
	public String getProduct() { return product; }
	public int getDifficulty() { return difficulty; }
	public boolean isHeavy() { return heavy; }
	public boolean isOn() { return isOn; }
	public boolean isActive() {
		return isOn; // Corrigido
	}
	
	// Setters
	public void setOperator(String operator) {
		this.operator = (operator == null || operator.trim().isEmpty()) ? "" : operator;
	}
	public void setOn(boolean on) {
		this.isOn = on;
	}
	
	@Override
	public String toString() {
		return "Machine{" +
		"name='" + name + '\'' +
		", number=" + number +
		", operator='" + (operator.isEmpty() ? "<none>" : operator) + '\'' +
		", difficulty=" + difficulty +
		", isOn=" + isOn +
		'}';
	}
}