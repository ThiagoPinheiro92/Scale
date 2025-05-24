package com.th.scala.core.services;

import com.th.scala.core.models.Machine;
import com.th.scala.core.models.Operator;
import java.util.*;

public class MachineDistributor {
	
	public void distributeMachines(List<Machine> machines, List<Operator> operators) {
		clearCurrentDistribution(machines);
		Collections.shuffle(operators);
		
		// Contadores por operador
		Map<String, Integer> difficultCount = new HashMap<>();
		Map<String, Integer> mediumCount = new HashMap<>();
		Map<String, Integer> easyCount = new HashMap<>();
		
		// Fase 1: Atribui máquinas difíceis
		for (Machine machine : machines) {
			if (machine.getDifficulty() == 3) {
				assignDifficultMachine(machine, operators, difficultCount);
			}
		}
		
		// Fase 2: Atribui máquinas médias
		for (Machine machine : machines) {
			if (machine.getDifficulty() == 2 && machine.getOperator().isEmpty()) {
				assignMediumMachine(machine, operators, difficultCount, mediumCount, easyCount);
			}
		}
		
		// Fase 3: Atribui máquinas fáceis
		for (Machine machine : machines) {
			if (machine.getDifficulty() == 1 && machine.getOperator().isEmpty()) {
				assignEasyMachine(machine, operators, difficultCount, easyCount);
			}
		}
	}
	
	private void assignDifficultMachine(Machine machine,
	List<Operator> operators,
	Map<String, Integer> difficultCount) {
		for (Operator operator : operators) {
			if (operator.canHandleDifficultMachines() &&
			!difficultCount.containsKey(operator.getName())) {
				
				machine.setOperator(operator.getName());
				difficultCount.put(operator.getName(), 1);
				return;
			}
		}
	}
	
	private void assignMediumMachine(Machine machine,
	List<Operator> operators,
	Map<String, Integer> difficultCount,
	Map<String, Integer> mediumCount,
	Map<String, Integer> easyCount) {
		for (Operator operator : operators) {
			int currentMedium = mediumCount.getOrDefault(operator.getName(), 0);
			int currentEasy = easyCount.getOrDefault(operator.getName(), 0);
			
			if (!difficultCount.containsKey(operator.getName()) &&
			currentMedium < 2 &&
			currentEasy == 0) {
				
				machine.setOperator(operator.getName());
				mediumCount.put(operator.getName(), currentMedium + 1);
				return;
			}
		}
	}
	
	private void assignEasyMachine(Machine machine,
	List<Operator> operators,
	Map<String, Integer> difficultCount,
	Map<String, Integer> easyCount) {
		for (Operator operator : operators) {
			int currentEasy = easyCount.getOrDefault(operator.getName(), 0);
			
			if (!difficultCount.containsKey(operator.getName()) &&
			currentEasy < 3) {
				
				machine.setOperator(operator.getName());
				easyCount.put(operator.getName(), currentEasy + 1);
				return;
			}
		}
	}
	
	private void clearCurrentDistribution(List<Machine> machines) {
		for (Machine machine : machines) {
			machine.setOperator("");
		}
	}
}