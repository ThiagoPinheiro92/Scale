package com.th.scala.core.services;

import com.th.scala.core.models.Machine;
import com.th.scala.core.models.Operator;
import java.util.*;

public class MachineDistributor {
	
	private static class OperatorLoad {
		boolean hasDifficult = false;
		int mediumCount = 0;
		int easyCount = 0;
	}
	
	public void distributeMachines(List<Machine> machines, List<Operator> operators) {
		clearCurrentDistribution(machines);
		Collections.shuffle(operators);
		
		Map<String, OperatorLoad> operatorLoads = new HashMap<>();
		
		// Fase 1: Atribui máquinas difíceis
		for (Machine machine : machines) {
			if (machine.getDifficulty() == 3) {
				assignMachine(machine, operators, operatorLoads, true);
			}
		}
		
		// Fase 2: Atribui máquinas médias
		for (Machine machine : machines) {
			if (machine.getDifficulty() == 2 && machine.getOperator().isEmpty()) {
				assignMachine(machine, operators, operatorLoads, false);
			}
		}
		
		// Fase 3: Atribui máquinas fáceis (com fallback)
		for (Machine machine : machines) {
			if (machine.getDifficulty() == 1 && machine.getOperator().isEmpty()) {
				if (!assignMachine(machine, operators, operatorLoads, false)) {
					// Fallback: Se não encontrou operador ideal, atribui a qualquer operador disponível
					assignToAnyOperator(machine, operators, operatorLoads);
				}
			}
		}
	}
	
	private boolean assignMachine(Machine machine,
	List<Operator> operators,
	Map<String, OperatorLoad> operatorLoads,
	boolean isDifficult) {
		for (Operator operator : operators) {
			OperatorLoad load = operatorLoads.getOrDefault(operator.getName(), new OperatorLoad());
			
			if (canAssign(machine, operator, load, isDifficult)) {
				machine.setOperator(operator.getName());
				updateLoad(machine, load);
				operatorLoads.put(operator.getName(), load);
				return true;
			}
		}
		return false;
	}
	
	private void assignToAnyOperator(Machine machine,
	List<Operator> operators,
	Map<String, OperatorLoad> operatorLoads) {
		for (Operator operator : operators) {
			OperatorLoad load = operatorLoads.getOrDefault(operator.getName(), new OperatorLoad());
			
			// Regras mais flexíveis para fallback
			if (!load.hasDifficult) {
				machine.setOperator(operator.getName());
				load.easyCount++;
				operatorLoads.put(operator.getName(), load);
				return;
			}
		}
	}
	
	private boolean canAssign(Machine machine,
	Operator operator,
	OperatorLoad load,
	boolean isDifficult) {
		if (isDifficult) {
			return operator.canHandleDifficultMachines() &&
			!load.hasDifficult &&
			load.mediumCount == 0 &&
			load.easyCount == 0;
			} else {
			switch (machine.getDifficulty()) {
				case 2: // Média
				return !load.hasDifficult &&
				load.mediumCount < 1 &&
				load.easyCount <= 1; // Permite 1 média + 1 fácil
				
				case 1: // Fácil
				return !load.hasDifficult &&
				(load.easyCount < 3 &&
				(load.mediumCount == 0 || load.easyCount < 1));
			}
		}
		return false;
	}
	
	private void updateLoad(Machine machine, OperatorLoad load) {
		switch (machine.getDifficulty()) {
			case 3: load.hasDifficult = true; break;
			case 2: load.mediumCount++; break;
			case 1: load.easyCount++; break;
		}
	}
	
	private void clearCurrentDistribution(List<Machine> machines) {
		for (Machine machine : machines) {
			machine.setOperator("");
		}
	}
}