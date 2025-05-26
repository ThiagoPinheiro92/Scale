package com.th.scala.core.models;

public class Operator {
	private final String name;
	private final boolean canHandleDifficultMachines;
	private final int maxMachines;
	
	public Operator(String name, boolean canHandleDifficultMachines, int maxMachines) {
		this.name = name;
		this.canHandleDifficultMachines = canHandleDifficultMachines;
		this.maxMachines = maxMachines;
	}
	
	// Getters
	public String getName() { return name; }
	public boolean canHandleDifficultMachines() { return canHandleDifficultMachines; }
	public int getMaxMachines() { return maxMachines; }
}