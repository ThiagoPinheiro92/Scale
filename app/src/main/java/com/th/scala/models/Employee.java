package com.th.scala.models;

import java.util.List;

public class Employee {
	private int id;
	private String name;
	private boolean isPcd;
	private List<Integer> restrictedProductTypes; // Tipos de produto que não pode operar
	private int maxDifficulty; // Dificuldade máxima que pode lidar
	private int currentWorkload; // Número de máquinas atribuídas
	
	// Construtor, getters e setters
	public Employee(int id, String name, boolean isPcd) {
		this.id = id;
		this.name = name;
		this.isPcd = isPcd;
		this.currentWorkload = 0;
	}
	
	public boolean canOperate(Machine machine) {
		// Verifica se o funcionário pode operar a máquina
		return !(isPcd && restrictedProductTypes.contains(machine.getCurrentProduct().getType()))
		&& machine.getDifficulty() <= maxDifficulty;
	}
}