package com.th.scala.core.services;

import com.th.scala.core.models.Machine;
import com.th.scala.core.models.Operator;
import java.util.*;

public class MachineDistributor {
	
	private static class OperatorAssignment {
		boolean hasDifficult = false;
		List<Integer> assignedIndices = new ArrayList<>();
		int mediumCount = 0;
		int easyCount = 0;
	}
	
	public void distributeMachines(List<Machine> machines, List<Operator> operators) {
		clearCurrentDistribution(machines);
		Collections.shuffle(operators);
		
		Map<String, OperatorAssignment> assignments = new HashMap<>();
		
		// 1. Atribuição prioritária de máquinas difíceis
		assignDifficultMachines(machines, operators, assignments);
		
		// 2. Atribuição considerando vizinhança
		assignWithNeighborhoodCheck(machines, operators, assignments);
	}
	
	private void assignDifficultMachines(List<Machine> machines,
	List<Operator> operators,
	Map<String, OperatorAssignment> assignments) {
		for (int i = 0; i < machines.size(); i++) {
			Machine machine = machines.get(i);
			if (machine.getDifficulty() == 3) {
				assignToEligibleOperator(machines, i, operators, assignments, true);
			}
		}
	}
	
	private void assignWithNeighborhoodCheck(List<Machine> machines,
	List<Operator> operators,
	Map<String, OperatorAssignment> assignments) {
		// Primeiro atribui médias considerando vizinhança
		for (int i = 0; i < machines.size(); i++) {
			Machine machine = machines.get(i);
			if (machine.getDifficulty() == 2 && machine.getOperator().isEmpty()) {
				assignToEligibleOperator(machines, i, operators, assignments, false);
			}
		}
		
		// Depois atribui fáceis considerando vizinhança
		for (int i = 0; i < machines.size(); i++) {
			Machine machine = machines.get(i);
			if (machine.getDifficulty() == 1 && machine.getOperator().isEmpty()) {
				assignToEligibleOperator(machines, i, operators, assignments, false);
			}
		}
	}
	
	private void assignToEligibleOperator(List<Machine> machines,
	int currentIndex,
	List<Operator> operators,
	Map<String, OperatorAssignment> assignments,
	boolean isDifficult) {
		Machine machine = machines.get(currentIndex);
		
		for (Operator operator : operators) {
			OperatorAssignment assignment = assignments.getOrDefault(
			operator.getName(),
			new OperatorAssignment()
			);
			
			if (canAssign(machine, operator, assignment, isDifficult)) {
				// Verifica vizinhança para máquinas não-difíceis
				if (!isDifficult && !assignment.assignedIndices.isEmpty()) {
					boolean isNearby = false;
					for (int assignedIndex : assignment.assignedIndices) {
						if (Math.abs(assignedIndex - currentIndex) <= 2) { // 2 máquinas de distância
							isNearby = true;
							break;
						}
					}
					if (!isNearby) continue;
				}
				
				machine.setOperator(operator.getName());
				updateAssignment(machine, assignment, currentIndex);
				assignments.put(operator.getName(), assignment);
				break;
			}
		}
	}
	
	private boolean canAssign(Machine machine,
	Operator operator,
	OperatorAssignment assignment,
	boolean isDifficult) {
		if (isDifficult) {
			return operator.canHandleDifficultMachines() &&
			!assignment.hasDifficult &&
			assignment.mediumCount == 0 &&
			assignment.easyCount == 0;
			} else {
			switch (machine.getDifficulty()) {
				case 2: // Média
				return !assignment.hasDifficult &&
				assignment.mediumCount < 2 &&
				(assignment.easyCount == 0 || assignment.mediumCount == 0);
				
				case 1: // Fácil
				return !assignment.hasDifficult &&
				assignment.easyCount < 3 &&
				(assignment.mediumCount == 0 || assignment.easyCount < 1);
			}
		}
		return false;
	}
	
	private void updateAssignment(Machine machine,
	OperatorAssignment assignment,
	int index) {
		switch (machine.getDifficulty()) {
			case 3:
			assignment.hasDifficult = true;
			break;
			case 2:
			assignment.mediumCount++;
			break;
			case 1:
			assignment.easyCount++;
			break;
		}
		assignment.assignedIndices.add(index);
	}
	
	private void clearCurrentDistribution(List<Machine> machines) {
		for (Machine machine : machines) {
			machine.setOperator("");
		}
	}
}