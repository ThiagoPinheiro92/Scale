package com.th.scala.core.services;

import com.th.scala.core.models.Machine;
import com.th.scala.core.models.Operator;
import java.util.*;

public class MachineDistributor {
	
	private static class OperatorLoad {
		Set<Integer> difficultMachines = new HashSet<>();
		Set<Integer> mediumMachines = new HashSet<>();
		Set<Integer> easyMachines = new HashSet<>();
	}
	
	public void distributeMachines(List<Machine> machines, List<Operator> operators) {
		clearCurrentDistribution(machines);
		Collections.shuffle(operators);
		
		Map<String, OperatorLoad> operatorLoads = new HashMap<>();
		
		// Fase 1: Atribui máquinas difíceis (prioridade máxima)
		for (int i = 0; i < machines.size(); i++) {
			Machine machine = machines.get(i);
			if (machine.getDifficulty() == 3) {
				assignMachine(machine, i, operators, operatorLoads, true);
			}
		}
		
		// Fase 2: Atribui máquinas médias
		for (int i = 0; i < machines.size(); i++) {
			Machine machine = machines.get(i);
			if (machine.getDifficulty() == 2 && machine.getOperator().isEmpty()) {
				assignMachine(machine, i, operators, operatorLoads, false);
			}
		}
		
		// Fase 3: Atribui máquinas fáceis
		for (int i = 0; i < machines.size(); i++) {
			Machine machine = machines.get(i);
			if (machine.getDifficulty() == 1 && machine.getOperator().isEmpty()) {
				assignMachine(machine, i, operators, operatorLoads, false);
			}
		}
	}
	
	private void assignMachine(Machine machine,
	int machineIndex,
	List<Operator> operators,
	Map<String, OperatorLoad> operatorLoads,
	boolean isDifficult) {
		for (Operator operator : operators) {
			OperatorLoad load = operatorLoads.getOrDefault(operator.getName(), new OperatorLoad());
			
			if (canAssign(machine, operator, load, isDifficult)) {
				machine.setOperator(operator.getName());
				updateLoad(machine, load, machineIndex);
				operatorLoads.put(operator.getName(), load);
				break;
			}
		}
	}
	
	private boolean canAssign(Machine machine,
	Operator operator,
	OperatorLoad load,
	boolean isDifficult) {
		if (isDifficult) {
			return operator.canHandleDifficultMachines() &&
			load.difficultMachines.isEmpty() &&
			load.mediumMachines.isEmpty() &&
			load.easyMachines.isEmpty();
			} else {
			switch (machine.getDifficulty()) {
				case 2: // Média
				return load.difficultMachines.isEmpty() &&
				(load.mediumMachines.size() < 1 ||
				(load.mediumMachines.size() < 2 && load.easyMachines.isEmpty()));
				
				case 1: // Fácil
				return load.difficultMachines.isEmpty() &&
				(load.easyMachines.size() < 2 ||
				(load.easyMachines.size() < 3 && load.mediumMachines.isEmpty()));
			}
		}
		return false;
	}
	
	private void updateLoad(Machine machine, OperatorLoad load, int machineIndex) {
		switch (machine.getDifficulty()) {
			case 3:
			load.difficultMachines.add(machineIndex);
			break;
			case 2:
			load.mediumMachines.add(machineIndex);
			break;
			case 1:
			load.easyMachines.add(machineIndex);
			break;
		}
	}
	
	private void clearCurrentDistribution(List<Machine> machines) {
		for (Machine machine : machines) {
			machine.setOperator("");
		}
	}
}