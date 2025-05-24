package com.th.scala.core.services;

import com.th.scala.core.models.Machine;
import com.th.scala.core.models.Operator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineDistributor {
	
	public void distributeMachines(List<Machine> machines, List<Operator> operators) {
		clearCurrentDistribution(machines);
		Collections.shuffle(operators);
		
		// Separa as máquinas por dificuldade
		List<Machine> difficultMachines = new ArrayList<>();
		List<Machine> mediumMachines = new ArrayList<>();
		List<Machine> easyMachines = new ArrayList<>();
		
		for (Machine machine : machines) {
			switch (machine.getDifficulty()) {
				case 3: difficultMachines.add(machine); break;
				case 2: mediumMachines.add(machine); break;
				case 1: easyMachines.add(machine); break;
				default: easyMachines.add(machine); // trata não definido como fácil
			}
		}
		
		// Mapa para controlar as atribuições
		Map<String, OperatorAssignment> operatorAssignments = new HashMap<>();
		
		// 1. Atribui máquinas difíceis primeiro (operadores exclusivos)
		assignDifficultMachines(difficultMachines, operators, operatorAssignments);
		
		// 2. Atribui máquinas médias
		assignMediumMachines(mediumMachines, operators, operatorAssignments);
		
		// 3. Atribui máquinas fáceis
		assignEasyMachines(easyMachines, operators, operatorAssignments);
	}
	
	private void clearCurrentDistribution(List<Machine> machines) {
		for (Machine machine : machines) {
			machine.setOperator("");
		}
	}
	
	private void assignDifficultMachines(List<Machine> difficultMachines,
	List<Operator> operators,
	Map<String, OperatorAssignment> operatorAssignments) {
		for (Machine machine : difficultMachines) {
			for (Operator operator : operators) {
				if (!operatorAssignments.containsKey(operator.getName()) &&
				operator.canHandleDifficultMachines()) {
					
					machine.setOperator(operator.getName());
					operatorAssignments.put(operator.getName(),
					new OperatorAssignment(1, 0, 0));
					break;
				}
			}
		}
	}
	
	private void assignMediumMachines(List<Machine> mediumMachines,
	List<Operator> operators,
	Map<String, OperatorAssignment> operatorAssignments) {
		for (Machine machine : mediumMachines) {
			if (!machine.getOperator().isEmpty()) continue;
			
			for (Operator operator : operators) {
				OperatorAssignment assignment = operatorAssignments.getOrDefault(
				operator.getName(),
				new OperatorAssignment(0, 0, 0)
				);
				
				// Pode atribuir se:
				// 1. Não tiver máquinas difíceis
				// 2. Ainda não atingiu limite de 2 médias
				if (assignment.difficultCount == 0 &&
				assignment.mediumCount < 2) {
					
					machine.setOperator(operator.getName());
					assignment.mediumCount++;
					operatorAssignments.put(operator.getName(), assignment);
					break;
				}
			}
		}
	}
	
	private void assignEasyMachines(List<Machine> easyMachines,
	List<Operator> operators,
	Map<String, OperatorAssignment> operatorAssignments) {
		for (Machine machine : easyMachines) {
			if (!machine.getOperator().isEmpty()) continue;
			
			for (Operator operator : operators) {
				OperatorAssignment assignment = operatorAssignments.getOrDefault(
				operator.getName(),
				new OperatorAssignment(0, 0, 0)
				);
				
				// Pode atribuir se:
				// 1. Não tiver máquinas difíceis ou médias
				// 2. Ainda não atingiu limite de 3 fáceis
				if (assignment.difficultCount == 0 &&
				assignment.mediumCount == 0 &&
				assignment.easyCount < 3) {
					
					machine.setOperator(operator.getName());
					assignment.easyCount++;
					operatorAssignments.put(operator.getName(), assignment);
					break;
				}
			}
		}
	}
	
	// Classe auxiliar para rastrear atribuições
	private static class OperatorAssignment {
		int difficultCount;
		int mediumCount;
		int easyCount;
		
		OperatorAssignment(int difficultCount, int mediumCount, int easyCount) {
			this.difficultCount = difficultCount;
			this.mediumCount = mediumCount;
			this.easyCount = easyCount;
		}
	}
}