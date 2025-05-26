package com.th.scala.core.services;



import com.th.scala.core.models.Machine;
import com.th.scala.core.models.Operator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MachineDistributor {
	
	// Constantes para nomes de operadores (assumindo que os nomes são fixos)
	private static final String OP1 = "Operador 1";
	private static final String OP2 = "Operador 2";
	private static final String OP3 = "Operador 3";
	private static final String OP4 = "Operador 4";
	private static final String OP5 = "Operador 5";
	private static final String OP6 = "Operador 6";
	private static final String NOT_ASSIGNED = "(não atribuído)";
	
	public void distributeMachines(List<Machine> machines, List<Operator> operators) {
		clearCurrentDistribution(machines);
		
		// Verifica a composição das dificuldades
		long easyCount = machines.stream().filter(m -> m.getDifficulty() == 1).count();
		long mediumCount = machines.stream().filter(m -> m.getDifficulty() == 2).count();
		long difficultCount = machines.stream().filter(m -> m.getDifficulty() == 3).count();
		int totalMachines = machines.size();
		
		// Mapeia nomes de operadores para objetos Operator para fácil acesso
		// (Considera que a lista de operadores pode não ter todos de 1 a 6)
		Operator operator1 = findOperatorByName(operators, OP1);
		Operator operator2 = findOperatorByName(operators, OP2);
		Operator operator3 = findOperatorByName(operators, OP3);
		Operator operator4 = findOperatorByName(operators, OP4);
		Operator operator5 = findOperatorByName(operators, OP5);
		Operator operator6 = findOperatorByName(operators, OP6);
		
		// Aplica a lógica baseada no cenário
		if (mediumCount == 0 && difficultCount == 0 && easyCount == totalMachines && totalMachines == 14) {
			distributeAllEasyScenario(machines, operator1, operator2, operator3, operator4, operator5);
			} else if (mediumCount >= 1 && difficultCount == 0) { // Cenário com pelo menos uma média
			// A lógica exata para "uma máquina média" é complexa e precisa de mais clareza.
			// Implementando a lógica descrita o mais próximo possível.
			distributeOneMediumScenario(machines, operator1, operator2, operator3, operator4, operator5, operator6);
			} else if (difficultCount >= 1) { // Cenário com pelo menos uma difícil
			// A lógica exata para "uma máquina difícil" também é complexa.
			// Implementando a lógica descrita o mais próximo possível.
			distributeOneDifficultScenario(machines, operator1, operator2, operator3, operator4, operator5);
			} else {
			// Fallback: Se nenhum cenário específico corresponder, talvez aplicar uma lógica padrão?
			// Por enquanto, deixaremos sem atribuição se não cair nos cenários.
			System.out.println("Cenário de distribuição não reconhecido.");
		}
	}
	
	private void distributeAllEasyScenario(List<Machine> machines, Operator op1, Operator op2, Operator op3, Operator op4, Operator op5) {
		int assignedCount = 0;
		// 111
		assignedCount += assignMachinesToOperator(machines, op1, 3, assignedCount);
		// 222
		assignedCount += assignMachinesToOperator(machines, op2, 3, assignedCount);
		// 333
		assignedCount += assignMachinesToOperator(machines, op3, 3, assignedCount);
		// 444
		assignedCount += assignMachinesToOperator(machines, op4, 3, assignedCount);
		// 55
		assignedCount += assignMachinesToOperator(machines, op5, 2, assignedCount);
		
		// Atribui não atribuído às restantes, se houver
		assignRemainingToNotAssigned(machines);
	}
	
	// Implementação preliminar baseada na interpretação da regra "uma média"
	private void distributeOneMediumScenario(List<Machine> machines, Operator op1, Operator op2, Operator op3, Operator op4, Operator op5, Operator op6) {
		// Regra: 111,(3máquina adjacente fácil)(3maquina média),(4máquina médio)(4maquina fácil adjacente),222,555,6
		// Esta regra é muito específica e depende da ordem e adjacência exatas.
		// Requer uma implementação cuidadosa e possivelmente mais informações sobre a lista de máquinas.
		// Simplificação / Interpretação:
		
		int currentMachineIndex = 0;
		
		// 111 (fáceis)
		currentMachineIndex = assignSpecificMachines(machines, op1, 1, 3, currentMachineIndex);
		
		// (3 máquina média) + (3 máquina adjacente fácil)
		int mediumMachineIndex3 = findFirstMachineByDifficulty(machines, 2, currentMachineIndex);
		if (mediumMachineIndex3 != -1) {
			assignOperatorToMachine(machines.get(mediumMachineIndex3), op3);
			int adjacentEasyIndex3 = findAdjacentMachineByDifficulty(machines, mediumMachineIndex3, 1);
			if (adjacentEasyIndex3 != -1) {
				assignOperatorToMachine(machines.get(adjacentEasyIndex3), op3);
			}
			// Atualiza o índice para evitar reatribuição
			if (mediumMachineIndex3 >= currentMachineIndex) currentMachineIndex = mediumMachineIndex3 + 1;
			if (adjacentEasyIndex3 >= currentMachineIndex) currentMachineIndex = adjacentEasyIndex3 + 1;
		}
		
		// (4 máquina média) + (4 máquina fácil adjacente)
		int mediumMachineIndex4 = findFirstMachineByDifficulty(machines, 2, currentMachineIndex); // Procura a *próxima* média
		if (mediumMachineIndex4 == -1 && mediumMachineIndex3 != -1) { // Se não achar outra, talvez seja a mesma?
			mediumMachineIndex4 = mediumMachineIndex3; // Requer clarificação da regra
		}
		
		if (mediumMachineIndex4 != -1 && machines.get(mediumMachineIndex4).getOperator().isEmpty()) { // Garante que não foi atribuída
			assignOperatorToMachine(machines.get(mediumMachineIndex4), op4);
			int adjacentEasyIndex4 = findAdjacentMachineByDifficulty(machines, mediumMachineIndex4, 1);
			if (adjacentEasyIndex4 != -1 && machines.get(adjacentEasyIndex4).getOperator().isEmpty()) {
				assignOperatorToMachine(machines.get(adjacentEasyIndex4), op4);
				if (adjacentEasyIndex4 >= currentMachineIndex) currentMachineIndex = adjacentEasyIndex4 + 1;
			}
			if (mediumMachineIndex4 >= currentMachineIndex) currentMachineIndex = mediumMachineIndex4 + 1;
		}
		
		// 222 (fáceis restantes)
		currentMachineIndex = assignSpecificMachines(machines, op2, 1, 3, 0); // Reinicia busca por fáceis
		
		// 555 (fáceis restantes)
		currentMachineIndex = assignSpecificMachines(machines, op5, 1, 3, 0); // Reinicia busca por fáceis
		
		// 6 (última máquina da lista, se operador 6 existir)
		if (machines.size() > 0) {
			Machine lastMachine = machines.get(machines.size() - 1);
			if (lastMachine.getOperator().isEmpty()) { // Só atribui se não tiver dono
				assignOperatorToMachine(lastMachine, op6); // Usa assignOperatorToMachine que trata op null
			}
		}
		
		// Atribui não atribuído às restantes
		assignRemainingToNotAssigned(machines);
	}
	
	// Implementação preliminar baseada na interpretação da regra "uma difícil"
	private void distributeOneDifficultScenario(List<Machine> machines, Operator op1, Operator op2, Operator op3, Operator op4, Operator op5) {
		// Regra: 111, 2, (2 maquina média), (5 maquina média), 5, 333, 4, 4, 1(difícil), 4(fácil adjacente mais próxima)
		// Novamente, muito específico e dependente da ordem.
		
		int currentMachineIndex = 0;
		List<Machine> assignedMachines = new ArrayList<>(); // Para rastrear máquinas já atribuídas nesta lógica
		
		// 111 (fáceis)
		currentMachineIndex = assignSpecificMachines(machines, op1, 1, 3, currentMachineIndex, assignedMachines);
		
		// 2 (fácil)
		currentMachineIndex = assignSpecificMachines(machines, op2, 1, 1, currentMachineIndex, assignedMachines);
		
		// (2 maquina média)
		int mediumMachineIndex2 = findFirstMachineByDifficultyAndUnassigned(machines, 2, 0, assignedMachines);
		if (mediumMachineIndex2 != -1) {
			assignOperatorToMachine(machines.get(mediumMachineIndex2), op2, assignedMachines);
		}
		
		// (5 maquina média)
		int mediumMachineIndex5 = findFirstMachineByDifficultyAndUnassigned(machines, 2, 0, assignedMachines);
		if (mediumMachineIndex5 != -1) {
			assignOperatorToMachine(machines.get(mediumMachineIndex5), op5, assignedMachines);
		}
		
		// 5 (fácil)
		currentMachineIndex = assignSpecificMachines(machines, op5, 1, 1, 0, assignedMachines); // Reinicia busca por fáceis não atribuídos
		
		// 333 (fáceis)
		currentMachineIndex = assignSpecificMachines(machines, op3, 1, 3, 0, assignedMachines);
		
		// 4 (fácil)
		currentMachineIndex = assignSpecificMachines(machines, op4, 1, 1, 0, assignedMachines);
		
		// 4 (fácil)
		currentMachineIndex = assignSpecificMachines(machines, op4, 1, 1, 0, assignedMachines);
		
		// 1 (difícil)
		int difficultMachineIndex = findFirstMachineByDifficultyAndUnassigned(machines, 3, 0, assignedMachines);
		if (difficultMachineIndex != -1) {
			assignOperatorToMachine(machines.get(difficultMachineIndex), op1, assignedMachines);
			
			// 4 (fácil adjacente mais próxima da difícil)
			int adjacentEasyIndex = findAdjacentMachineByDifficultyAndUnassigned(machines, difficultMachineIndex, 1, assignedMachines);
			if (adjacentEasyIndex != -1) {
				assignOperatorToMachine(machines.get(adjacentEasyIndex), op4, assignedMachines);
			}
		}
		
		// Atribui não atribuído às restantes
		assignRemainingToNotAssigned(machines);
	}
	
	// --- Métodos Auxiliares ---
	
	private Operator findOperatorByName(List<Operator> operators, String name) {
		return operators.stream().filter(op -> op.getName().equals(name)).findFirst().orElse(null);
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
	
	// Atribui 'count' máquinas de uma dificuldade específica para um operador a partir de 'startIndex'
	private int assignSpecificMachines(List<Machine> machines, Operator op, int difficulty, int count, int startIndex) {
		return assignSpecificMachines(machines, op, difficulty, count, startIndex, null);
	}
	
	// Sobrecarga para rastrear máquinas já atribuídas em lógicas complexas
	private int assignSpecificMachines(List<Machine> machines, Operator op, int difficulty, int count, int startIndex, List<Machine> alreadyAssigned) {
		int assigned = 0;
		int lastIndex = startIndex;
		String operatorName = (op != null) ? op.getName() : NOT_ASSIGNED;
		for (int i = startIndex; i < machines.size() && assigned < count; i++) {
			Machine machine = machines.get(i);
			if (machine.getDifficulty() == difficulty && machine.getOperator().isEmpty() && (alreadyAssigned == null || !alreadyAssigned.contains(machine))) {
				machine.setOperator(operatorName);
				if (alreadyAssigned != null) alreadyAssigned.add(machine);
				assigned++;
				lastIndex = i + 1;
			}
		}
		// Se não encontrou a partir de startIndex, tenta do início (necessário para cenários complexos)
		if (assigned < count && startIndex > 0) {
			for (int i = 0; i < startIndex && assigned < count; i++) {
				Machine machine = machines.get(i);
				if (machine.getDifficulty() == difficulty && machine.getOperator().isEmpty() && (alreadyAssigned == null || !alreadyAssigned.contains(machine))) {
					machine.setOperator(operatorName);
					if (alreadyAssigned != null) alreadyAssigned.add(machine);
					assigned++;
					// Não atualiza lastIndex aqui para não confundir a chamada original
				}
			}
		}
		return lastIndex; // Retorna o próximo índice após a última atribuição feita a partir do startIndex original
	}
	
	private void assignOperatorToMachine(Machine machine, Operator op) {
		assignOperatorToMachine(machine, op, null);
	}
	
	// Sobrecarga para rastrear máquinas já atribuídas em lógicas complexas
	private void assignOperatorToMachine(Machine machine, Operator op, List<Machine> alreadyAssigned) {
		if (machine != null && machine.getOperator().isEmpty() && (alreadyAssigned == null || !alreadyAssigned.contains(machine))) {
			machine.setOperator((op != null) ? op.getName() : NOT_ASSIGNED);
			if (alreadyAssigned != null) alreadyAssigned.add(machine);
		}
	}
	
	private int findFirstMachineByDifficulty(List<Machine> machines, int difficulty, int startIndex) {
		for (int i = startIndex; i < machines.size(); i++) {
			if (machines.get(i).getDifficulty() == difficulty && machines.get(i).getOperator().isEmpty()) {
				return i;
			}
		}
		// Se não achar a partir de startIndex, procura desde o início
		for (int i = 0; i < startIndex; i++) {
			if (machines.get(i).getDifficulty() == difficulty && machines.get(i).getOperator().isEmpty()) {
				return i;
			}
		}
		return -1; // Não encontrada
	}
	
	private int findFirstMachineByDifficultyAndUnassigned(List<Machine> machines, int difficulty, int startIndex, List<Machine> alreadyAssigned) {
		for (int i = startIndex; i < machines.size(); i++) {
			Machine m = machines.get(i);
			if (m.getDifficulty() == difficulty && m.getOperator().isEmpty() && (alreadyAssigned == null || !alreadyAssigned.contains(m))) {
				return i;
			}
		}
		// Se não achar a partir de startIndex, procura desde o início
		for (int i = 0; i < startIndex; i++) {
			Machine m = machines.get(i);
			if (m.getDifficulty() == difficulty && m.getOperator().isEmpty() && (alreadyAssigned == null || !alreadyAssigned.contains(m))) {
				return i;
			}
		}
		return -1; // Não encontrada
	}
	
	private int findAdjacentMachineByDifficulty(List<Machine> machines, int pos, int difficulty) {
		return findAdjacentMachineByDifficultyAndUnassigned(machines, pos, difficulty, null);
	}
	
	// Procura máquina adjacente (primeiro à esquerda, depois à direita) com a dificuldade especificada e não atribuída
	private int findAdjacentMachineByDifficultyAndUnassigned(List<Machine> machines, int pos, int difficulty, List<Machine> alreadyAssigned) {
		// Verifica à esquerda
		int leftIdx = pos - 1;
		if (leftIdx >= 0) {
			Machine adjacent = machines.get(leftIdx);
			if (adjacent.getDifficulty() == difficulty && adjacent.getOperator().isEmpty() && (alreadyAssigned == null || !alreadyAssigned.contains(adjacent))) {
				return leftIdx;
			}
		}
		// Verifica à direita
		int rightIdx = pos + 1;
		if (rightIdx < machines.size()) {
			Machine adjacent = machines.get(rightIdx);
			if (adjacent.getDifficulty() == difficulty && adjacent.getOperator().isEmpty() && (alreadyAssigned == null || !alreadyAssigned.contains(adjacent))) {
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