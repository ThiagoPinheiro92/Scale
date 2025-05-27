package com.th.scala.core.services;

import com.th.scala.core.models.Machine;
import com.th.scala.core.models.Operator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MachineDistributor {
	
	private static final String NOT_ASSIGNED = "(não atribuído)";
	
	public void distributeMachines(List<Machine> machines, List<Operator> rotatedOperators) {
		clearCurrentDistribution(machines);
		
		long easyCount = machines.stream().filter(m -> m.getDifficulty() == 1).count();
		long mediumCount = machines.stream().filter(m -> m.getDifficulty() == 2).count();
		long difficultCount = machines.stream().filter(m -> m.getDifficulty() == 3).count();
		int totalMachines = machines.size();
		
		if (mediumCount == 0 && difficultCount == 0 && easyCount == totalMachines && totalMachines == 14) {
			System.out.println("--- Executando Cenário: Tudo Fácil (Rotacionado) ---");
			distributeAllEasyScenarioRotated(machines, rotatedOperators);
			} else if (mediumCount >= 1 && difficultCount == 0) {
			System.out.println("--- Executando Cenário: Pelo Menos Uma Média (Rotacionado) ---");
			distributeOneMediumScenarioRotated(machines, rotatedOperators);
			} else if (difficultCount >= 1) {
			System.out.println("--- Executando Cenário: Pelo Menos Uma Difícil (Rotacionado e Corrigido) ---");
			distributeOneDifficultScenarioRotatedCorrected(machines, rotatedOperators);
			} else {
			System.out.println("--- Cenário de distribuição não reconhecido. Atribuindo restantes a 'não atribuído'. ---");
			assignRemainingToNotAssigned(machines);
		}
	}
	
	// Cenário Fácil: Usa a ordem da lista rotacionada
	private void distributeAllEasyScenarioRotated(List<Machine> machines, List<Operator> rotatedOperators) {
		int assignedCount = 0;
		int opIndex = 0;
		int numOperators = rotatedOperators.size();
		
		if (numOperators < 5) {
			System.out.println("Aviso: Menos de 5 operadores disponíveis para o cenário fácil.");
			assignRemainingEquitably(machines, rotatedOperators, 0);
			return;
		}
		
		assignedCount += assignMachinesToOperator(machines, getOperatorAtIndex(rotatedOperators, opIndex++), 3, assignedCount);
		assignedCount += assignMachinesToOperator(machines, getOperatorAtIndex(rotatedOperators, opIndex++), 3, assignedCount);
		assignedCount += assignMachinesToOperator(machines, getOperatorAtIndex(rotatedOperators, opIndex++), 3, assignedCount);
		assignedCount += assignMachinesToOperator(machines, getOperatorAtIndex(rotatedOperators, opIndex++), 3, assignedCount);
		assignedCount += assignMachinesToOperator(machines, getOperatorAtIndex(rotatedOperators, opIndex++), 2, assignedCount);
		
		assignRemainingToNotAssigned(machines);
	}
	
	// Cenário Médio: Adapta a lógica para usar índices da lista rotacionada
	private void distributeOneMediumScenarioRotated(List<Machine> machines, List<Operator> rotatedOperators) {
		List<Machine> assignedInThisScenario = new ArrayList<>();
		Set<Operator> operatorsUsedInThisScenario = new HashSet<>(); // Track used operators
		int numOperators = rotatedOperators.size();
		
		if (numOperators < 6) {
			System.out.println("Aviso: Menos de 6 operadores disponíveis para o cenário médio.");
			assignRemainingEquitably(machines, rotatedOperators, 0);
			return;
		}
		
		Operator opRotated0 = getOperatorAtIndex(rotatedOperators, 0);
		Operator opRotated1 = getOperatorAtIndex(rotatedOperators, 1);
		Operator opRotated2 = getOperatorAtIndex(rotatedOperators, 2);
		Operator opRotated3 = getOperatorAtIndex(rotatedOperators, 3);
		Operator opRotated4 = getOperatorAtIndex(rotatedOperators, 4);
		Operator opRotated5 = getOperatorAtIndex(rotatedOperators, 5);
		
		// 111 (fáceis) -> Operador no índice 0
		assignSpecificMachines(machines, opRotated0, 1, 3, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		
		// (3 média) + (3 adjacente fácil) -> Operador no índice 2
		int mediumMachineIndex3 = findFirstMachineByDifficultyAndUnassigned(machines, 2, 0, assignedInThisScenario);
		if (mediumMachineIndex3 != -1) {
			assignOperatorToMachine(machines.get(mediumMachineIndex3), opRotated2, assignedInThisScenario, operatorsUsedInThisScenario);
			int adjacentEasyIndex3 = findAdjacentMachineByDifficultyAndUnassigned(machines, mediumMachineIndex3, 1, assignedInThisScenario);
			if (adjacentEasyIndex3 != -1) {
				assignOperatorToMachine(machines.get(adjacentEasyIndex3), opRotated2, assignedInThisScenario, operatorsUsedInThisScenario);
			}
		}
		
		// (4 média) + (4 adjacente fácil) -> Operador no índice 3
		int mediumMachineIndex4 = findFirstMachineByDifficultyAndUnassigned(machines, 2, 0, assignedInThisScenario);
		if (mediumMachineIndex4 != -1) {
			assignOperatorToMachine(machines.get(mediumMachineIndex4), opRotated3, assignedInThisScenario, operatorsUsedInThisScenario);
			int adjacentEasyIndex4 = findAdjacentMachineByDifficultyAndUnassigned(machines, mediumMachineIndex4, 1, assignedInThisScenario);
			if (adjacentEasyIndex4 != -1) {
				assignOperatorToMachine(machines.get(adjacentEasyIndex4), opRotated3, assignedInThisScenario, operatorsUsedInThisScenario);
			}
		}
		
		// 222 (fáceis restantes) -> Operador no índice 1
		assignSpecificMachines(machines, opRotated1, 1, 3, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		
		// 555 (fáceis restantes) -> Operador no índice 4
		assignSpecificMachines(machines, opRotated4, 1, 3, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		
		// 6 (última máquina da lista) -> Operador no índice 5
		if (machines.size() > 0) {
			Machine lastMachine = machines.get(machines.size() - 1);
			if (lastMachine.getOperator().isEmpty()) {
				assignOperatorToMachine(lastMachine, opRotated5, assignedInThisScenario, operatorsUsedInThisScenario);
			}
		}
		
		assignRemainingToNotAssigned(machines);
	}
	
	// Cenário Difícil: CORRIGIDO para atribuir difícil a operador não utilizado
	private void distributeOneDifficultScenarioRotatedCorrected(List<Machine> machines, List<Operator> rotatedOperators) {
		// Regra original: 111, 2, (2 média), (5 média), 5, 333, 4, 4, 1(difícil), 4(adj fácil da difícil)
		// Correção: Máquina difícil vai para operador ainda não usado neste cenário.
		List<Machine> assignedInThisScenario = new ArrayList<>();
		Set<Operator> operatorsUsedInThisScenario = new HashSet<>(); // Track used operators
		int numOperators = rotatedOperators.size();
		
		// Cenário difícil parece usar apenas 5 operadores na regra original, mas precisa de 6 para ter um livre para a difícil
		if (numOperators < 6) {
			System.out.println("Aviso: Menos de 6 operadores disponíveis para o cenário difícil (necessário para ter um operador livre para a máquina difícil).");
			assignRemainingEquitably(machines, rotatedOperators, 0);
			return;
		}
		
		Operator opRotated0 = getOperatorAtIndex(rotatedOperators, 0);
		Operator opRotated1 = getOperatorAtIndex(rotatedOperators, 1);
		Operator opRotated2 = getOperatorAtIndex(rotatedOperators, 2);
		Operator opRotated3 = getOperatorAtIndex(rotatedOperators, 3);
		Operator opRotated4 = getOperatorAtIndex(rotatedOperators, 4);
		// opRotated5 (índice 5) é o potencial receptor da máquina difícil se estiver livre
		
		// 111 (fáceis) -> Operador no índice 0
		assignSpecificMachines(machines, opRotated0, 1, 3, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		
		// 2 (fácil) -> Operador no índice 1
		assignSpecificMachines(machines, opRotated1, 1, 1, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		
		// (2 maquina média) -> Operador no índice 1
		int mediumMachineIndex2 = findFirstMachineByDifficultyAndUnassigned(machines, 2, 0, assignedInThisScenario);
		if (mediumMachineIndex2 != -1) {
			assignOperatorToMachine(machines.get(mediumMachineIndex2), opRotated1, assignedInThisScenario, operatorsUsedInThisScenario);
		}
		
		// (5 maquina média) -> Operador no índice 4
		int mediumMachineIndex5 = findFirstMachineByDifficultyAndUnassigned(machines, 2, 0, assignedInThisScenario);
		if (mediumMachineIndex5 != -1) {
			assignOperatorToMachine(machines.get(mediumMachineIndex5), opRotated4, assignedInThisScenario, operatorsUsedInThisScenario);
		}
		
		// 5 (fácil) -> Operador no índice 4
		assignSpecificMachines(machines, opRotated4, 1, 1, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		
		// 333 (fáceis) -> Operador no índice 2
		assignSpecificMachines(machines, opRotated2, 1, 3, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		
		// 4 (fácil) -> Operador no índice 3
		assignSpecificMachines(machines, opRotated3, 1, 1, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		
		// 4 (fácil) -> Operador no índice 3
		assignSpecificMachines(machines, opRotated3, 1, 1, 0, assignedInThisScenario, operatorsUsedInThisScenario);
		
		// (difícil) -> Operador AINDA NÃO UTILIZADO neste cenário
		int difficultMachineIndex = findFirstMachineByDifficultyAndUnassigned(machines, 3, 0, assignedInThisScenario);
		if (difficultMachineIndex != -1) {
			Operator operatorForDifficult = null;
			// Encontra o primeiro operador na lista rotacionada que ainda não foi usado
			for (Operator op : rotatedOperators) {
				if (!operatorsUsedInThisScenario.contains(op)) {
					operatorForDifficult = op;
					break;
				}
			}
			
			if (operatorForDifficult != null) {
				System.out.println("Atribuindo máquina difícil (" + machines.get(difficultMachineIndex).getName() + ") para operador livre: " + operatorForDifficult.getName());
				assignOperatorToMachine(machines.get(difficultMachineIndex), operatorForDifficult, assignedInThisScenario, operatorsUsedInThisScenario);
				} else {
				System.out.println("Aviso: NENHUM operador livre encontrado para a máquina difícil. Atribuindo a 'não atribuído'.");
				assignOperatorToMachine(machines.get(difficultMachineIndex), null, assignedInThisScenario, operatorsUsedInThisScenario); // Atribui a null -> NOT_ASSIGNED
			}
			
			// 4 (fácil adjacente mais próxima da difícil) -> Operador no índice 3 (opRotated3)
			// Esta regra permanece a mesma, atribuindo ao 4º operador da sequência rotacionada
			int adjacentEasyIndex = findAdjacentMachineByDifficultyAndUnassigned(machines, difficultMachineIndex, 1, assignedInThisScenario);
			if (adjacentEasyIndex != -1) {
				assignOperatorToMachine(machines.get(adjacentEasyIndex), opRotated3, assignedInThisScenario, operatorsUsedInThisScenario);
			}
		}
		
		assignRemainingToNotAssigned(machines);
	}
	
	// --- Métodos Auxiliares (adaptados para usar Set de operadores usados) ---
	
	private Operator getOperatorAtIndex(List<Operator> operators, int index) {
		if (operators == null || operators.isEmpty() || index < 0) {
			return null;
		}
		return operators.get(index % operators.size());
	}
	
	private void clearCurrentDistribution(List<Machine> machines) {
		for (Machine machine : machines) {
			machine.setOperator("");
		}
	}
	
	private int assignMachinesToOperator(List<Machine> machines, Operator op, int count, int startIndex) {
		int assigned = 0;
		String operatorName = (op != null) ? op.getName() : NOT_ASSIGNED;
		for (int i = startIndex; i < machines.size() && assigned < count; i++) {
			Machine machine = machines.get(i);
			if (machine.getOperator().isEmpty()) {
				machine.setOperator(operatorName);
				assigned++;
			}
		}
		return assigned;
	}
	
	private void assignRemainingEquitably(List<Machine> machines, List<Operator> operators, int startIndex) {
		if (operators == null || operators.isEmpty()) {
			assignRemainingToNotAssigned(machines);
			return;
		}
		int opIndex = 0;
		for (int i = startIndex; i < machines.size(); i++) {
			Machine machine = machines.get(i);
			if (machine.getOperator().isEmpty()) {
				Operator currentOp = operators.get(opIndex % operators.size());
				machine.setOperator(currentOp.getName());
				opIndex++;
			}
		}
	}
	
	// Modificado para adicionar operador ao Set de usados
	private void assignSpecificMachines(List<Machine> machines, Operator op, int difficulty, int count, int startIndex, List<Machine> alreadyAssigned, Set<Operator> operatorsUsed) {
		int assigned = 0;
		String operatorName = (op != null) ? op.getName() : NOT_ASSIGNED;
		boolean operatorAddedToUsedSet = false;
		
		for (int i = startIndex; i < machines.size() && assigned < count; i++) {
			Machine machine = machines.get(i);
			if (machine.getDifficulty() == difficulty && machine.getOperator().isEmpty() && !alreadyAssigned.contains(machine)) {
				machine.setOperator(operatorName);
				alreadyAssigned.add(machine);
				assigned++;
				if (op != null && !operatorAddedToUsedSet) {
					operatorsUsed.add(op);
					operatorAddedToUsedSet = true; // Adiciona apenas uma vez por chamada
				}
			}
		}
		
		if (assigned < count && startIndex > 0) {
			for (int i = 0; i < startIndex && assigned < count; i++) {
				Machine machine = machines.get(i);
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
		if (assigned < count) {
			for (int i = 0; i < machines.size() && assigned < count; i++) {
				Machine machine = machines.get(i);
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
	
	// Modificado para adicionar operador ao Set de usados
	private void assignOperatorToMachine(Machine machine, Operator op, List<Machine> alreadyAssigned, Set<Operator> operatorsUsed) {
		if (machine != null && machine.getOperator().isEmpty() && !alreadyAssigned.contains(machine)) {
			String operatorName = (op != null) ? op.getName() : NOT_ASSIGNED;
			machine.setOperator(operatorName);
			alreadyAssigned.add(machine);
			if (op != null) {
				operatorsUsed.add(op); // Adiciona o operador ao conjunto de usados
			}
		}
	}
	
	private int findFirstMachineByDifficultyAndUnassigned(List<Machine> machines, int difficulty, int startIndex, List<Machine> alreadyAssigned) {
		for (int i = startIndex; i < machines.size(); i++) {
			Machine m = machines.get(i);
			if (m.getDifficulty() == difficulty && m.getOperator().isEmpty() && !alreadyAssigned.contains(m)) {
				return i;
			}
		}
		for (int i = 0; i < startIndex; i++) {
			Machine m = machines.get(i);
			if (m.getDifficulty() == difficulty && m.getOperator().isEmpty() && !alreadyAssigned.contains(m)) {
				return i;
			}
		}
		return -1;
	}
	
	private int findAdjacentMachineByDifficultyAndUnassigned(List<Machine> machines, int pos, int difficulty, List<Machine> alreadyAssigned) {
		int leftIdx = pos - 1;
		if (leftIdx >= 0) {
			Machine adjacent = machines.get(leftIdx);
			if (adjacent.getDifficulty() == difficulty && adjacent.getOperator().isEmpty() && !alreadyAssigned.contains(adjacent)) {
				return leftIdx;
			}
		}
		int rightIdx = pos + 1;
		if (rightIdx < machines.size()) {
			Machine adjacent = machines.get(rightIdx);
			if (adjacent.getDifficulty() == difficulty && adjacent.getOperator().isEmpty() && !alreadyAssigned.contains(adjacent)) {
				return rightIdx;
			}
		}
		return -1;
	}
	
	private void assignRemainingToNotAssigned(List<Machine> machines) {
		for (Machine machine : machines) {
			if (machine.getOperator().isEmpty()) {
				machine.setOperator(NOT_ASSIGNED);
			}
		}
	}
}