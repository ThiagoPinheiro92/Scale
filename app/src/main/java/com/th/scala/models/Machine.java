package com.th.scala.models;

public class Machine {
	private int id;
	private String name;
	private int difficulty; // 1-5 escala de dificuldade
	private Product currentProduct;
	private boolean requiresSpecialTraining;
	
	// Construtor, getters e setters
	public Machine(int id, String name, int difficulty) {
		this.id = id;
		this.name = name;
		this.difficulty = difficulty;
	}
	
	// Métodos adicionais...
}