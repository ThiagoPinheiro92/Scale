package com.th.scala.core.services;

import com.th.scala.core.models.Machine;
import com.th.scala.core.models.Operator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Distributor adapted to only consider active (isOn=true) machines
public class MachineDistributor {
	
	private static final String NOT_ASSIGNED = "(não atribuído)";
	
	// Main distribution method
	public void distributeMachines(List<Machine> allMachines, List<Operator> rotatedOperators) {
		// 1. Clear previous assignments from ALL machines (even inactive ones)
		clearCurrentDistribution(allMachines);
		
		// 2. Filter to get only machines that are currently ON
		List<Machine> activeMachines = allMachines.stream()
		.filter(Machine::isOn)
		.collect(Collectors.toList());
		
		System.out.println("--- Iniciando Distribuição --- ");
		System.out.println("Total de máquinas: " + allMachines.size());
		System.out.println("Máquinas LIGADAS consideradas: " + activeMachines.size());
		
		// If no machines are active, there's nothing to distribute
		if (activeMachines.isEmpty()) {
			System.out.println("Nenhuma máquina ativa para distribuir.");
			// Ensure remaining inactive machines are marked as unassigned if needed (already done by clear)
			// assignRemainingToNotAssigned(allMachines); // Might be redundant if clear works
			return;
		}
		
		// 3. Calculate counts based ONLY on active machines
		long easyCount = activeMachines.stream().filter(m -> m.getDifficulty() == 1).count();
		long mediumCount = activeMachines.stream().filter(m -> m.getDifficulty() == 2).count();
		long difficultCount = activeMachines.stream().filter(m -> m.getDifficulty() == 3).count();
		int totalActiveMachines = activeMachines.size();
		
		System.out.println("Ativas - Fáceis: " + easyCount + ", Médias: " + mediumCount + ", Difíceis: " + difficultCount);
		
		// 4. Determine scenario based on ACTIVE machines and call the appropriate method
		// Note: The original rule for easy (totalMachines == 14) might need review
		// if you want it to apply when *all active* machines are easy, regardless of count.
		// For now, keeping the original condition but applying it to active counts.
		if (mediumCount == 0 && difficultCount == 0 && easyCount == totalActiveMachines /* && totalActiveMachines == 14 */) {
			// Condition `totalActiveMachines == 14` kept for now, review if needed
			System.out.println("--- Executando Cenário: Todas Ativas Fáceis (Rotacionado) ---");
			distributeAllEasyScenarioRotated(activeMachines, rotatedOperators);
			} else if (mediumCount >= 1 && difficultCount == 0) {
			System.out.println("--- Executando Cenário: Pelo Menos Uma Média Ativa (Rotacionado) ---");
			distributeOneMediumScenarioRotated(activeMachines, rotatedOperators);
			} else if (difficultCount >= 1) {
			System.out.println("--- Executando Cenário: Pelo Menos Uma Difícil Ativa (Rotacionado e Corrigido) ---");
			distributeOneDifficultScenarioRotatedCorrected(activeMachines, rotatedOperators);
			} else {
			// Fallback: If active machines don't fit a specific scenario, distribute them equitably
			System.out.println("--- Cenário específico não reconhecido para máquinas ativas. Distribuindo equitativamente. ---");
			assignRemainingEquitably(activeMachines, rotatedOperators, 0);
		}
		
		// 5. Mark any remaining machines (active or inactive) that weren't assigned as NOT_ASSIGNED
		// This catches machines that might have been active but didn't get assigned by the scenario logic
		// and ensures inactive machines remain unassigned.
		assignRemainingToNotAssigned(allMachines);
		System.out.println("--- Distribuição Concluída --- ");
	}
	
	// --- Scenario Methods (Now operate on activeMachines) ---
	
	private void distributeAllEasyScenarioRotated(List<Machine> activeMachines, List<Operator> rotatedOperators) {
		int assignedCount = 0;
		int opIndex = 0;
		int numOperators = rotatedOperators.size();
		
		// Rule requires 5 operators for 14 machines (3,3,3,3,2)
		// Adapt if the rule changes for different counts of easy machines
		if (numOperators < 5) {
			System.out.println("Aviso: Menos de 5 operadores disponíveis para o cenário fácil.");
			assignRemainingEquitably(activeMachines, rotatedOperators, 0);
			return;
		}
		if (activeMachines.size() != 14) {
			System.out.println("Aviso: Cenário fácil chamado com " + activeMachines.size() + " máquinas ativas (esperado 14). Distribuindo equitativamente.");
			assignRemainingEquitably(activeMachines, rotatedOperators, 0);
			return;
		}
		
		// Apply the 3,3,3,3,2 distribution to the active machines
		assignedCount += assignMachinesToOperator(activeMachines, getOperatorAtIndex(rotatedOperators, opIndex++), 3, assignedCount);
		assignedCount += assignMachinesToOperator(activeMachines, getOperatorAtIndex(rotatedOperators, opIndex++), 3, assignedCount);
		assignedCount += assignMachinesToOperator(activeMachines, getOperatorAtIndex(rotatedOperators, opIndex++), 3, assignedCount);
		assignedCount += assignMachinesToOperator(activeMachines, getOperatorAtIndex(rotatedOperators, opIndex++), 3, assignedCount);
		assignedCount += assignMachinesToOperator(activeMachines, getOperatorAtIndex(rotatedOperators, opIndex++), 2, assignedCount);
		
		// assignRemainingToNotAssigned(activeMachines); // Handled globally now
	}
	
	private void distributeOneMediumScenarioRotated(List<Machine> activeMachines, List<Operator> rotatedOperators) {
		List<Machine> assignedInThisScenario = new ArrayList<>();
		Set<Operator> operatorsUsedInThisScenario = new HashSet<>();
		int numOperators = rotatedOperators.size();
		
		// Rule requires 6 operators
		if (numOperators < 6) {
			System.out.println("Aviso: Menos de 6 operadores disponíveis para o cenário médio.");
			assignRemainingEquitably(activeMachines, rotatedOperators, 0);
			return;
		}
		
		Operator opRotated0 = getOperatorAtIndex(rotatedOperators, 0);
		Operator opRotated1 = getOperatorAtIndex(rotatedOperators, 1);
		Operator opRotated2 = getOperatorAtIndex(rotatedOperators, 2);
		Operator opRotated3 = getOperatorAtIndex(rotatedOperators, 3);
		Operator opRotated4 = getOperatorAtIndex(rotatedOperators, 4);
		Operator opRotated5 = getOperatorAtIndex(rotatedOperators, 5);
		
		// Apply rules using only activeMachines
		assignSpecificMachines(activeMachines, opRotated0, 1, 3, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		int mediumMachineIndex3 = findFirstMachineByDifficultyAndUnassigned(activeMachines, 2, 0, assignedInThisScenario);
		if (mediumMachineIndex3 != -1) {
			assignOperatorToMachine(activeMachines.get(mediumMachineIndex3), opRotated2, assignedInThisScenario, operatorsUsedInThisScenario);
			int adjacentEasyIndex3 = findAdjacentMachineByDifficultyAndUnassigned(activeMachines, mediumMachineIndex3, 1, assignedInThisScenario);
			if (adjacentEasyIndex3 != -1) {
				assignOperatorToMachine(activeMachines.get(adjacentEasyIndex3), opRotated2, assignedInThisScenario, operatorsUsedInThisScenario);
			}
		}
		int mediumMachineIndex4 = findFirstMachineByDifficultyAndUnassigned(activeMachines, 2, 0, assignedInThisScenario);
		if (mediumMachineIndex4 != -1) {
			assignOperatorToMachine(activeMachines.get(mediumMachineIndex4), opRotated3, assignedInThisScenario, operatorsUsedInThisScenario);
			int adjacentEasyIndex4 = findAdjacentMachineByDifficultyAndUnassigned(activeMachines, mediumMachineIndex4, 1, assignedInThisScenario);
			if (adjacentEasyIndex4 != -1) {
				assignOperatorToMachine(activeMachines.get(adjacentEasyIndex4), opRotated3, assignedInThisScenario, operatorsUsedInThisScenario);
			}
		}
		assignSpecificMachines(activeMachines, opRotated1, 1, 3, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		assignSpecificMachines(activeMachines, opRotated4, 1, 3, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		if (activeMachines.size() > 0) {
			// Find the last *active* machine
			Machine lastActiveMachine = activeMachines.get(activeMachines.size() - 1);
			if (lastActiveMachine.getOperator().isEmpty()) {
				assignOperatorToMachine(lastActiveMachine, opRotated5, assignedInThisScenario, operatorsUsedInThisScenario);
			}
		}
		// assignRemainingToNotAssigned(activeMachines); // Handled globally now
	}
	
	private void distributeOneDifficultScenarioRotatedCorrected(List<Machine> activeMachines, List<Operator> rotatedOperators) {
		List<Machine> assignedInThisScenario = new ArrayList<>();
		Set<Operator> operatorsUsedInThisScenario = new HashSet<>();
		int numOperators = rotatedOperators.size();
		
		// Rule requires 6 operators to have one free for the difficult machine
		if (numOperators < 6) {
			System.out.println("Aviso: Menos de 6 operadores disponíveis para o cenário difícil.");
			assignRemainingEquitably(activeMachines, rotatedOperators, 0);
			return;
		}
		
		Operator opRotated0 = getOperatorAtIndex(rotatedOperators, 0);
		Operator opRotated1 = getOperatorAtIndex(rotatedOperators, 1);
		Operator opRotated2 = getOperatorAtIndex(rotatedOperators, 2);
		Operator opRotated3 = getOperatorAtIndex(rotatedOperators, 3);
		Operator opRotated4 = getOperatorAtIndex(rotatedOperators, 4);
		
		// Apply rules using only activeMachines
		assignSpecificMachines(activeMachines, opRotated0, 1, 3, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		assignSpecificMachines(activeMachines, opRotated1, 1, 1, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		int mediumMachineIndex2 = findFirstMachineByDifficultyAndUnassigned(activeMachines, 2, 0, assignedInThisScenario);
		if (mediumMachineIndex2 != -1) {
			assignOperatorToMachine(activeMachines.get(mediumMachineIndex2), opRotated1, assignedInThisScenario, operatorsUsedInThisScenario);
		}
		int mediumMachineIndex5 = findFirstMachineByDifficultyAndUnassigned(activeMachines, 2, 0, assignedInThisScenario);
		if (mediumMachineIndex5 != -1) {
			assignOperatorToMachine(activeMachines.get(mediumMachineIndex5), opRotated4, assignedInThisScenario, operatorsUsedInThisScenario);
		}
		assignSpecificMachines(activeMachines, opRotated4, 1, 1, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		assignSpecificMachines(activeMachines, opRotated2, 1, 3, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		assignSpecificMachines(activeMachines, opRotated3, 1, 1, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		assignSpecificMachines(activeMachines, opRotated3, 1, 1, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		
		int difficultMachineIndex = findFirstMachineByDifficultyAndUnassigned(activeMachines, 3, 0, assignedInThisScenario);
		if (difficultMachineIndex != -1) {
			Operator operatorForDifficult = null;
			for (Operator op : rotatedOperators) {
				if (!operatorsUsedInThisScenario.contains(op)) {
					operatorForDifficult = op;
					break;
				}
			}
			if (operatorForDifficult != null) {
				System.out.println("Atribuindo máquina difícil (" + activeMachines.get(difficultMachineIndex).getName() + ") para operador livre: " + operatorForDifficult.getName());
				assignOperatorToMachine(activeMachines.get(difficultMachineIndex), operatorForDifficult, assignedInThisScenario, operatorsUsedInThisScenario);
				} else {
				System.out.println("Aviso: NENHUM operador livre encontrado para a máquina difícil. Atribuindo a 'não atribuído'.");
				assignOperatorToMachine(activeMachines.get(difficultMachineIndex), null, assignedInThisScenario, operatorsUsedInThisScenario);
			}
			int adjacentEasyIndex = findAdjacentMachineByDifficultyAndUnassigned(activeMachines, difficultMachineIndex, 1, assignedInThisScenario);
			if (adjacentEasyIndex != -1) {
				assignOperatorToMachine(activeMachines.get(adjacentEasyIndex), opRotated3, assignedInThisScenario, operatorsUsedInThisScenario);
			}
		}
		// assignRemainingToNotAssigned(activeMachines); // Handled globally now
	}
	
	// --- Helper Methods (Now operate on the list passed to them, usually activeMachines) ---
	
	private Operator getOperatorAtIndex(List<Operator> operators, int index) {
		if (operators == null || operators.isEmpty() || index < 0) {
			return null;
		}
		return operators.get(index % operators.size());
	}
	
	// Clears assignments from a list of machines
	private void clearCurrentDistribution(List<Machine> machineList) {
		for (Machine machine : machineList) {
			machine.setOperator("");
		}
	}
	
	// Assigns count machines from the list to the operator, starting search from startIndex
	private int assignMachinesToOperator(List<Machine> machineList, Operator op, int count, int startIndex) {
		int assigned = 0;
		String operatorName = (op != null) ? op.getName() : NOT_ASSIGNED;
		for (int i = startIndex; i < machineList.size() && assigned < count; i++) {
			Machine machine = machineList.get(i);
			// Assign only if the machine is unassigned (and implicitly active, as it's from activeMachines list)
			if (machine.getOperator().isEmpty()) {
				machine.setOperator(operatorName);
				assigned++;
			}
		}
		return assigned;
	}
	
	// Distributes machines equitably among operators
	private void assignRemainingEquitably(List<Machine> machineList, List<Operator> operators, int startIndex) {
		if (operators == null || operators.isEmpty()) {
			// assignRemainingToNotAssigned(machineList); // Handled globally
			return;
		}
		int opIndex = 0;
		for (int i = startIndex; i < machineList.size(); i++) {
			Machine machine = machineList.get(i);
			if (machine.getOperator().isEmpty()) {
				Operator currentOp = operators.get(opIndex % operators.size());
				machine.setOperator(currentOp.getName());
				opIndex++;
			}
		}
	}
	
	// Assigns specific count of machines with given difficulty to an operator
	private void assignSpecificMachines(List<Machine> machineList, Operator op, int difficulty, int count, int startIndex, List<Machine> alreadyAssigned, Set<Operator> operatorsUsed) {
		int assigned = 0;
		String operatorName = (op != null) ? op.getName() : NOT_ASSIGNED;
		boolean operatorAddedToUsedSet = false;
		
		// Search from startIndex onwards
		for (int i = startIndex; i < machineList.size() && assigned < count; i++) {
			Machine machine = machineList.get(i);
			if (machine.getDifficulty() == difficulty && machine.getOperator().isEmpty() && !alreadyAssigned.contains(machine)) {
				machine.setOperator(operatorName);
				alreadyAssigned.add(machine);
				assigned++;
				if (op != null && !operatorAddedToUsedSet) {
					operatorsUsed.add(op);
					operatorAddedToUsedSet = true;
				}
			}
		}
		// Search from beginning up to startIndex if needed
		if (assigned < count && startIndex > 0) {
			for (int i = 0; i < startIndex && assigned < count; i++) {
				Machine machine = machineList.get(i);
				if (machine.getDifficulty() == difficulty && machine.getOperator().isEmpty() && !alreadyAssigned.contains(machine)) {
					machine.setOperator(operatorName);
					alreadyAssigned.add(machine);
					assigned++;
					if (op != null && !operatorAddedToUsedSet) {
						operatorsUsed.add(op);
						operatorAddedToUsedSet = true;
					}
				}
			}
		}
		// Search entire list again if still needed (should be rare)
		if (assigned < count) {
			for (int i = 0; i < machineList.size() && assigned < count; i++) {
				Machine machine = machineList.get(i);
				if (machine.getDifficulty() == difficulty && machine.getOperator().isEmpty() && !alreadyAssigned.contains(machine)) {
					machine.setOperator(operatorName);
					alreadyAssigned.add(machine);
					assigned++;
					if (op != null && !operatorAddedToUsedSet) {
						operatorsUsed.add(op);
						operatorAddedToUsedSet = true;
					}
				}
			}
		}
	}
	
	// Assigns a single machine to an operator
	private void assignOperatorToMachine(Machine machine, Operator op, List<Machine> alreadyAssigned, Set<Operator> operatorsUsed) {
		if (machine != null && machine.getOperator().isEmpty() && !alreadyAssigned.contains(machine)) {
			String operatorName = (op != null) ? op.getName() : NOT_ASSIGNED;
			machine.setOperator(operatorName);
			alreadyAssigned.add(machine);
			if (op != null) {
				operatorsUsed.add(op);
			}
		}
	}
	
	// Finds the first unassigned machine of a specific difficulty
	private int findFirstMachineByDifficultyAndUnassigned(List<Machine> machineList, int difficulty, int startIndex, List<Machine> alreadyAssigned) {
		for (int i = startIndex; i < machineList.size(); i++) {
			Machine m = machineList.get(i);
			if (m.getDifficulty() == difficulty && m.getOperator().isEmpty() && !alreadyAssigned.contains(m)) {
				return i;
			}
		}
		for (int i = 0; i < startIndex; i++) {
			Machine m = machineList.get(i);
			if (m.getDifficulty() == difficulty && m.getOperator().isEmpty() && !alreadyAssigned.contains(m)) {
				return i;
			}
		}
		return -1;
	}
	
	// Finds an adjacent unassigned machine of a specific difficulty
	private int findAdjacentMachineByDifficultyAndUnassigned(List<Machine> machineList, int pos, int difficulty, List<Machine> alreadyAssigned) {
		int leftIdx = pos - 1;
		if (leftIdx >= 0) {
			Machine adjacent = machineList.get(leftIdx);
			if (adjacent.getDifficulty() == difficulty && adjacent.getOperator().isEmpty() && !alreadyAssigned.contains(adjacent)) {
				return leftIdx;
			}
		}
		int rightIdx = pos + 1;
		if (rightIdx < machineList.size()) {
			Machine adjacent = machineList.get(rightIdx);
			if (adjacent.getDifficulty() == difficulty && adjacent.getOperator().isEmpty() && !alreadyAssigned.contains(adjacent)) {
				return rightIdx;
			}
		}
		return -1;
	}
	
	// Assigns any machine in the list that still has no operator to NOT_ASSIGNED
	private void assignRemainingToNotAssigned(List<Machine> machineList) {
		for (Machine machine : machineList) {
			if (machine.getOperator().isEmpty()) {
				machine.setOperator(NOT_ASSIGNED);
			}
		}
	}
}