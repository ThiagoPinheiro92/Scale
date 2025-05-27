package com.th.scala.core.services;

import com.th.scala.core.models.Machine;
import com.th.scala.core.models.Operator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Renomeando para refletir a nova funcionalidade
public class MachineDistributor {
	
	private static final String NOT_ASSIGNED = "(não atribuído)";
	
	// O método principal agora recebe a lista de operadores já rotacionada
	public void distributeMachines(List<Machine> machines, List<Operator> rotatedOperators) {
		clearCurrentDistribution(machines);
		
		long easyCount = machines.stream().filter(m -> m.getDifficulty() == 1).count();
		long mediumCount = machines.stream().filter(m -> m.getDifficulty() == 2).count();
		long difficultCount = machines.stream().filter(m -> m.getDifficulty() == 3).count();
		int totalMachines = machines.size();
		
		// Determina o cenário e chama o método apropriado, passando a lista rotacionada
		if (mediumCount == 0 && difficultCount == 0 && easyCount == totalMachines && totalMachines == 14) {
			System.out.println("--- Executando Cenário: Tudo Fácil (Rotacionado) ---");
			distributeAllEasyScenarioRotated(machines, rotatedOperators);
			} else if (mediumCount >= 1 && difficultCount == 0) {
			System.out.println("--- Executando Cenário: Pelo Menos Uma Média (Rotacionado) ---");
			// Assumindo que a lógica complexa de média/difícil também usa a ordem rotacionada
			distributeOneMediumScenarioRotated(machines, rotatedOperators);
			} else if (difficultCount >= 1) {
			System.out.println("--- Executando Cenário: Pelo Menos Uma Difícil (Rotacionado) ---");
			distributeOneDifficultScenarioRotated(machines, rotatedOperators);
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
		
		// Verifica se há operadores suficientes para a regra 111, 222, 333, 444, 55 (precisa de 5)
		if (numOperators < 5) {
			System.out.println("Aviso: Menos de 5 operadores disponíveis para o cenário fácil.");
			// Fallback simples: distribuir equitativamente?
			assignRemainingEquitably(machines, rotatedOperators, 0);
			return;
		}
		
		// Assume 14 máquinas e pelo menos 5 operadores na lista rotacionada
		// A ordem dos operadores é determinada pela lista `rotatedOperators`
		assignedCount += assignMachinesToOperator(machines, getOperatorAtIndex(rotatedOperators, opIndex++), 3, assignedCount);
		assignedCount += assignMachinesToOperator(machines, getOperatorAtIndex(rotatedOperators, opIndex++), 3, assignedCount);
		assignedCount += assignMachinesToOperator(machines, getOperatorAtIndex(rotatedOperators, opIndex++), 3, assignedCount);
		assignedCount += assignMachinesToOperator(machines, getOperatorAtIndex(rotatedOperators, opIndex++), 3, assignedCount);
		assignedCount += assignMachinesToOperator(machines, getOperatorAtIndex(rotatedOperators, opIndex++), 2, assignedCount);
		
		assignRemainingToNotAssigned(machines);
	}
	
	// Cenário Médio: Adapta a lógica para usar índices da lista rotacionada
	private void distributeOneMediumScenarioRotated(List<Machine> machines, List<Operator> rotatedOperators) {
		// Regra original: 111,(3 adj fácil)(3 média),(4 média)(4 adj fácil),222,555,6
		// Mapeando para índices da lista rotacionada (0 a 5, se houver 6 operadores)
		List<Machine> assignedInThisScenario = new ArrayList<>();
		int numOperators = rotatedOperators.size();
		
		// Verifica se há operadores suficientes
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
		Operator opRotated5 = getOperatorAtIndex(rotatedOperators, 5); // Corresponde ao '6' original
		
		// 111 (fáceis) -> Operador no índice 0 da lista rotacionada
		assignSpecificMachines(machines, opRotated0, 1, 3, 0, assignedInThisScenario);
		
		// (3 média) + (3 adjacente fácil) -> Operador no índice 2
		int mediumMachineIndex3 = findFirstMachineByDifficultyAndUnassigned(machines, 2, 0, assignedInThisScenario);
		if (mediumMachineIndex3 != -1) {
			assignOperatorToMachine(machines.get(mediumMachineIndex3), opRotated2, assignedInThisScenario);
			int adjacentEasyIndex3 = findAdjacentMachineByDifficultyAndUnassigned(machines, mediumMachineIndex3, 1, assignedInThisScenario);
			if (adjacentEasyIndex3 != -1) {
				assignOperatorToMachine(machines.get(adjacentEasyIndex3), opRotated2, assignedInThisScenario);
			}
		}
		
		// (4 média) + (4 adjacente fácil) -> Operador no índice 3
		int mediumMachineIndex4 = findFirstMachineByDifficultyAndUnassigned(machines, 2, 0, assignedInThisScenario);
		if (mediumMachineIndex4 != -1) {
			assignOperatorToMachine(machines.get(mediumMachineIndex4), opRotated3, assignedInThisScenario);
			int adjacentEasyIndex4 = findAdjacentMachineByDifficultyAndUnassigned(machines, mediumMachineIndex4, 1, assignedInThisScenario);
			if (adjacentEasyIndex4 != -1) {
				assignOperatorToMachine(machines.get(adjacentEasyIndex4), opRotated3, assignedInThisScenario);
			}
		}
		
		// 222 (fáceis restantes) -> Operador no índice 1
		assignSpecificMachines(machines, opRotated1, 1, 3, 0, assignedInThisScenario);
		
		// 555 (fáceis restantes) -> Operador no índice 4
		assignSpecificMachines(machines, opRotated4, 1, 3, 0, assignedInThisScenario);
		
		// 6 (última máquina da lista) -> Operador no índice 5
		if (machines.size() > 0) {
			Machine lastMachine = machines.get(machines.size() - 1);
			if (lastMachine.getOperator().isEmpty()) {
				assignOperatorToMachine(lastMachine, opRotated5, assignedInThisScenario);
			}
		}
		
		assignRemainingToNotAssigned(machines);
	}
	
	// Cenário Difícil: Adapta a lógica para usar índices da lista rotacionada
	private void distributeOneDifficultScenarioRotated(List<Machine> machines, List<Operator> rotatedOperators) {
		// Regra original: 111, 2, (2 média), (5 média), 5, 333, 4, 4, 1(difícil), 4(adj fácil da difícil)
		// Mapeando para índices da lista rotacionada (0 a 4, se houver 5 operadores)
		List<Machine> assignedInThisScenario = new ArrayList<>();
		int numOperators = rotatedOperators.size();
		
		// Cenário difícil parece usar apenas 5 operadores na regra original
		if (numOperators < 5) {
			System.out.println("Aviso: Menos de 5 operadores disponíveis para o cenário difícil.");
			assignRemainingEquitably(machines, rotatedOperators, 0);
			return;
		}
		
		Operator opRotated0 = getOperatorAtIndex(rotatedOperators, 0); // Corresponde ao '1' original
		Operator opRotated1 = getOperatorAtIndex(rotatedOperators, 1); // Corresponde ao '2' original
		Operator opRotated2 = getOperatorAtIndex(rotatedOperators, 2); // Corresponde ao '3' original
		Operator opRotated3 = getOperatorAtIndex(rotatedOperators, 3); // Corresponde ao '4' original
		Operator opRotated4 = getOperatorAtIndex(rotatedOperators, 4); // Corresponde ao '5' original
		
		// 111 (fáceis) -> Operador no índice 0
		assignSpecificMachines(machines, opRotated0, 1, 3, 0, assignedInThisScenario);
		
		// 2 (fácil) -> Operador no índice 1
		assignSpecificMachines(machines, opRotated1, 1, 1, 0, assignedInThisScenario);
		
		// (2 maquina média) -> Operador no índice 1
		int mediumMachineIndex2 = findFirstMachineByDifficultyAndUnassigned(machines, 2, 0, assignedInThisScenario);
		if (mediumMachineIndex2 != -1) {
			assignOperatorToMachine(machines.get(mediumMachineIndex2), opRotated1, assignedInThisScenario);
		}
		
		// (5 maquina média) -> Operador no índice 4
		int mediumMachineIndex5 = findFirstMachineByDifficultyAndUnassigned(machines, 2, 0, assignedInThisScenario);
		if (mediumMachineIndex5 != -1) {
			assignOperatorToMachine(machines.get(mediumMachineIndex5), opRotated4, assignedInThisScenario);
		}
		
		// 5 (fácil) -> Operador no índice 4
		assignSpecificMachines(machines, opRotated4, 1, 1, 0, assignedInThisScenario);
		
		// 333 (fáceis) -> Operador no índice 2
		assignSpecificMachines(machines, opRotated2, 1, 3, 0, assignedInThisScenario);
		
		// 4 (fácil) -> Operador no índice 3
		assignSpecificMachines(machines, opRotated3, 1, 1, 0, assignedInThisScenario);
		
		// 4 (fácil) -> Operador no índice 3
		assignSpecificMachines(machines, opRotated3, 1, 1, 0, assignedInThisScenario);
		
		// 1 (difícil) -> Operador no índice 0
		int difficultMachineIndex = findFirstMachineByDifficultyAndUnassigned(machines, 3, 0, assignedInThisScenario);
		if (difficultMachineIndex != -1) {
			assignOperatorToMachine(machines.get(difficultMachineIndex), opRotated0, assignedInThisScenario);
			
			// 4 (fácil adjacente mais próxima da difícil) -> Operador no índice 3
			int adjacentEasyIndex = findAdjacentMachineByDifficultyAndUnassigned(machines, difficultMachineIndex, 1, assignedInThisScenario);
			if (adjacentEasyIndex != -1) {
				assignOperatorToMachine(machines.get(adjacentEasyIndex), opRotated3, assignedInThisScenario);
			}
		}
		
		assignRemainingToNotAssigned(machines);
	}
	
	// --- Métodos Auxiliares (mantidos, mas alguns podem ser simplificados se não precisarem mais de 'alreadyAssigned') ---
	
	private Operator getOperatorAtIndex(List<Operator> operators, int index) {
		if (operators == null || operators.isEmpty() || index < 0) {
			return null; // Retorna null se a lista for inválida ou índice fora
		}
		// Usa módulo para lidar com rotação e índices maiores que o tamanho
		return operators.get(index % operators.size());
	}
	
	private void clearCurrentDistribution(List<Machine> machines) {
		for (Machine machine : machines) {
			machine.setOperator("");
		}
	}
	
	// Atribui 'count' máquinas (qualquer dificuldade) para um operador a partir de 'startIndex'
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
		return assigned; // Retorna quantas foram atribuídas
	}
	
	// Fallback para distribuir máquinas restantes equitativamente
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
	
	// Atribui 'count' máquinas de uma dificuldade específica para um operador, procurando a partir de 'startIndex' e depois do início
	private void assignSpecificMachines(List<Machine> machines, Operator op, int difficulty, int count, int startIndex, List<Machine> alreadyAssigned) {
		int assigned = 0;
		String operatorName = (op != null) ? op.getName() : NOT_ASSIGNED;
		
		// Tenta a partir de startIndex
		for (int i = startIndex; i < machines.size() && assigned < count; i++) {
			Machine machine = machines.get(i);
			// Verifica se a máquina não está na lista de já atribuídas *neste cenário específico*
			if (machine.getDifficulty() == difficulty && machine.getOperator().isEmpty() && !alreadyAssigned.contains(machine)) {
				machine.setOperator(operatorName);
				alreadyAssigned.add(machine);
				assigned++;
			}
		}
		
		// Se não encontrou todas, tenta do início até startIndex
		if (assigned < count && startIndex > 0) {
			for (int i = 0; i < startIndex && assigned < count; i++) {
				Machine machine = machines.get(i);
				if (machine.getDifficulty() == difficulty && machine.getOperator().isEmpty() && !alreadyAssigned.contains(machine)) {
					machine.setOperator(operatorName);
					alreadyAssigned.add(machine);
					assigned++;
				}
			}
		}
		// Se ainda não encontrou todas, tenta do início completo
		if (assigned < count) {
			for (int i = 0; i < machines.size() && assigned < count; i++) {
				Machine machine = machines.get(i);
				if (machine.getDifficulty() == difficulty && machine.getOperator().isEmpty() && !alreadyAssigned.contains(machine)) {
					machine.setOperator(operatorName);
					alreadyAssigned.add(machine);
					assigned++;
				}
			}
		}
	}
	
	private void assignOperatorToMachine(Machine machine, Operator op, List<Machine> alreadyAssigned) {
		// Verifica se a máquina não está na lista de já atribuídas *neste cenário específico*
		if (machine != null && machine.getOperator().isEmpty() && !alreadyAssigned.contains(machine)) {
			machine.setOperator((op != null) ? op.getName() : NOT_ASSIGNED);
			alreadyAssigned.add(machine);
		}
	}
	
	private int findFirstMachineByDifficultyAndUnassigned(List<Machine> machines, int difficulty, int startIndex, List<Machine> alreadyAssigned) {
		for (int i = startIndex; i < machines.size(); i++) {
			Machine m = machines.get(i);
			// Verifica se a máquina não está na lista de já atribuídas *neste cenário específico*
			if (m.getDifficulty() == difficulty && m.getOperator().isEmpty() && !alreadyAssigned.contains(m)) {
				return i;
			}
		}
		for (int i = 0; i < startIndex; i++) { // Procura antes do startIndex também
			Machine m = machines.get(i);
			if (m.getDifficulty() == difficulty && m.getOperator().isEmpty() && !alreadyAssigned.contains(m)) {
				return i;
			}
		}
		return -1; // Não encontrada
	}
	
	// Procura máquina adjacente (primeiro à esquerda, depois à direita) com a dificuldade especificada e não atribuída
	private int findAdjacentMachineByDifficultyAndUnassigned(List<Machine> machines, int pos, int difficulty, List<Machine> alreadyAssigned) {
		// Verifica à esquerda
		int leftIdx = pos - 1;
		if (leftIdx >= 0) {
			Machine adjacent = machines.get(leftIdx);
			// Verifica se a máquina não está na lista de já atribuídas *neste cenário específico*
			if (adjacent.getDifficulty() == difficulty && adjacent.getOperator().isEmpty() && !alreadyAssigned.contains(adjacent)) {
				return leftIdx;
			}
		}
		// Verifica à direita
		int rightIdx = pos + 1;
		if (rightIdx < machines.size()) {
			Machine adjacent = machines.get(rightIdx);
			// Verifica se a máquina não está na lista de já atribuídas *neste cenário específico*
			if (adjacent.getDifficulty() == difficulty && adjacent.getOperator().isEmpty() && !alreadyAssigned.contains(adjacent)) {
				return rightIdx;
			}
		}
		return -1; // Nenhuma adjacente encontrada
	}
	
	private void assignRemainingToNotAssigned(List<Machine> machines) {
		for (Machine machine : machines) {
			if (machine.getOperator().isEmpty()) {
				machine.setOperator(NOT_ASSIGNED);
			}
		}
	}
}