package com.th.scala.core.services;

import com.th.scala.core.models.Machine;
import com.th.scala.core.models.Operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// v8: Refactored rule application for reduced scenarios (<14 active) to ensure specific patterns (adjacency, 3-by-3) are consistently applied before equitable fallback.
public class MachineDistributor {
	
	private static final String NOT_ASSIGNED = "(não atribuído)";
	private List<Machine> originalMachineListForAdjacency;
	
	public void distributeMachines(List<Machine> allMachines, List<Operator> rotatedOperators, List<Operator> originalOperators) {
		this.originalMachineListForAdjacency = new ArrayList<>(allMachines);
		clearCurrentDistribution(allMachines);
		
		List<Machine> activeMachines = allMachines.stream()
		.filter(Machine::isOn)
		.collect(Collectors.toList());
		
		System.out.println("--- Iniciando Distribuição v8 --- ");
		System.out.println("Máquinas LIGADAS: " + activeMachines.size());
		
		if (activeMachines.isEmpty()) {
			System.out.println("Nenhuma máquina ativa.");
			assignRemainingToNotAssigned(allMachines);
			return;
		}
		
		int numberOfOperatorsToUse = determineOperatorCountDetailedCorrected(activeMachines);
		if (numberOfOperatorsToUse > originalOperators.size()) {
			numberOfOperatorsToUse = originalOperators.size();
		}
		if (numberOfOperatorsToUse <= 0) {
			assignRemainingToNotAssigned(allMachines);
			return;
		}
		
		List<Operator> selectedOperators = selectOperators(activeMachines, rotatedOperators, originalOperators, numberOfOperatorsToUse);
		System.out.println("Operadores selecionados (" + selectedOperators.size() + "): " + getOperatorNamesSimple(selectedOperators));
		
		// Apply distribution rules, prioritizing specific scenarios
		boolean specificRuleFullyApplied = applyDistributionRulesRefactored(activeMachines, selectedOperators, this.originalMachineListForAdjacency);
		
		// Check if there are still unassigned ACTIVE machines after attempting specific rules
		long unassignedActiveCount = activeMachines.stream().filter(m -> m.getOperator().isEmpty()).count();
		
		// Use equitable distribution only if a specific rule wasn't fully applied OR if there are still unassigned active machines
		if (!specificRuleFullyApplied || unassignedActiveCount > 0) {
			if (!specificRuleFullyApplied) {
				System.out.println("Nenhuma regra específica foi aplicada ou completada. Usando distribuição equitativa final.");
				} else {
				System.out.println("Regra específica aplicada, mas sobraram " + unassignedActiveCount + " máquinas ativas. Distribuindo restantes equitativamente.");
			}
			assignRemainingEquitably(activeMachines, selectedOperators);
		}
		
		assignRemainingToNotAssigned(allMachines); // Mark inactive/unassigned
		System.out.println("--- Distribuição v8 Concluída ---");
	}
	
	// Selects operators: Fixed for < 5/6 ops, Rotated for 14-machine scenarios
	private List<Operator> selectOperators(List<Machine> activeMachines, List<Operator> rotatedOperators, List<Operator> originalOperators, int numberOfOperatorsToUse) {
		boolean useRotation = false;
		if (activeMachines.size() == 14) {
			long easyCount = activeMachines.stream().filter(m -> m.getDifficulty() == 1).count();
			long mediumCount = activeMachines.stream().filter(m -> m.getDifficulty() == 2).count();
			long difficultCount = activeMachines.stream().filter(m -> m.getDifficulty() == 3).count();
			if ((easyCount == 14 && numberOfOperatorsToUse == 5) || ((mediumCount >= 1 || difficultCount >= 1) && numberOfOperatorsToUse == 6)) {
				useRotation = true;
			}
		}
		
		List<Operator> sourceList = useRotation ? rotatedOperators : originalOperators;
		String listType = useRotation ? "ROTACIONADA" : "ORIGINAL FIXA";
		// System.out.println("Selecionando operadores da lista " + listType);
		
		if (sourceList == null || sourceList.isEmpty()) {
			System.out.println("ERRO: Lista de operadores fonte (" + listType + ") está vazia ou nula!");
			return new ArrayList<>(); // Return empty list to avoid crash
		}
		
		if (sourceList.size() < numberOfOperatorsToUse) {
			// System.out.println("Aviso: Lista " + listType + " menor que operadores necessários. Usando todos da lista.");
			return new ArrayList<>(sourceList);
			} else {
			// Return a new list to avoid modifying the original sublist view
			return new ArrayList<>(sourceList.subList(0, numberOfOperatorsToUse));
		}
	}
	
	// Determines operator count based on active machine count AND difficulty (Corrected Logic)
	private int determineOperatorCountDetailedCorrected(List<Machine> activeMachines) {
		int activeCount = activeMachines.size();
		long easyCount = activeMachines.stream().filter(m -> m.getDifficulty() == 1).count();
		long mediumCount = activeMachines.stream().filter(m -> m.getDifficulty() == 2).count();
		long difficultCount = activeMachines.stream().filter(m -> m.getDifficulty() == 3).count();
		
		if (activeCount <= 0) return 0;
		if (activeCount == 1) return 1;
		if (activeCount == 2) { return (difficultCount >= 1) ? 2 : 1; }
		if (activeCount == 3) { return (mediumCount == 1 && easyCount == 2) ? 2 : 1; }
		if (activeCount == 4) {
			if (easyCount == 4) return 2;
			return (mediumCount == 2 && easyCount == 2) || (difficultCount == 1 && easyCount == 3) ? 2 : 3;
		}
		if (activeCount == 5) {
			if (difficultCount == 1 && easyCount == 4) return 3;
			if (easyCount == 5) return 2;
			if (mediumCount == 1 && easyCount == 4) return 2;
			return 3;
		}
		if (activeCount == 6) {
			if (easyCount == 6) return 2;
			if (difficultCount == 1 && mediumCount == 1 && easyCount == 4) return 3;
			if ((difficultCount == 1 && mediumCount == 0 && easyCount == 5) || (difficultCount == 0 && mediumCount == 1 && easyCount == 5)) return 3;
			if (mediumCount >= 1 || difficultCount >= 1) return 3;
			return 2;
		}
		if (activeCount == 7) { return 3; }
		if (activeCount >= 8 && activeCount <= 9) return 3;
		if (activeCount >= 10 && activeCount <= 12) return 4;
		if (activeCount == 13) return 5;
		if (activeCount == 14) {
			if (difficultCount >= 1) return 6;
			if (mediumCount >= 1) return 6;
			if (easyCount == 14) return 5;
			return 6;
		}
		if (activeCount > 14) return 6;
		return 1;
	}
	
	// REFACTORED Main logic dispatcher - returns true if a specific rule was applied AND handled all active machines, false otherwise
	private boolean applyDistributionRulesRefactored(List<Machine> activeMachines, List<Operator> selectedOperators, List<Machine> originalListForAdjacency) {
		int activeCount = activeMachines.size();
		int operatorCount = selectedOperators.size();
		long easyCount = activeMachines.stream().filter(m -> m.getDifficulty() == 1).count();
		long mediumCount = activeMachines.stream().filter(m -> m.getDifficulty() == 2).count();
		long difficultCount = activeMachines.stream().filter(m -> m.getDifficulty() == 3).count();
		boolean ruleAppliedAndCompleted = false;
		
		// Handle single operator case directly
		if (operatorCount == 1) {
			System.out.println("Aplicando regra: 1 Operador -> Todas as " + activeCount + " máquinas ativas.");
			assignAllMachinesToOperator(activeMachines, selectedOperators.get(0));
			ruleAppliedAndCompleted = true;
		}
		// Try "All Easy, < 14 Active" scenario first
		else if (activeCount < 14 && easyCount == activeCount && mediumCount == 0 && difficultCount == 0) {
			System.out.println("Aplicando regra: Todas Fáceis (<14 Ativas) -> Distribuição 3-por-3 (último pega resto)");
			distributeAllEasyReducedScenario(activeMachines, selectedOperators);
			ruleAppliedAndCompleted = checkAllActiveAssigned(activeMachines);
		}
		// Try 14-machine original scenarios
		else if (activeCount == 14) {
			System.out.println("Tentando aplicar regras originais para 14 máquinas ativas");
			int expectedOpsFor14 = determineOperatorCountDetailedCorrected(activeMachines);
			if (selectedOperators.size() == expectedOpsFor14) {
				if (tryOriginalScenarios(activeMachines, selectedOperators, originalListForAdjacency)) {
					ruleAppliedAndCompleted = checkAllActiveAssigned(activeMachines);
					} else {
					System.out.println("Mix de 14 máquinas não corresponde a cenário original.");
				}
				} else {
				System.out.println("Aviso: Número de operadores selecionados (" + selectedOperators.size() + ") não corresponde ao esperado (" + expectedOpsFor14 + ") para 14 máquinas.");
			}
		}
		// Try other specific reduced scenarios (2-13 active, with mix)
		else {
			switch (activeCount) {
				case 2:
				if (difficultCount >= 1 && operatorCount == 2) {
					System.out.println("Aplicando regra 2 Ativas (1 Difícil): Equitativa 1/1");
					assignRemainingEquitably(activeMachines, selectedOperators);
					ruleAppliedAndCompleted = checkAllActiveAssigned(activeMachines);
				}
				break;
				case 3:
				if (mediumCount == 1 && easyCount == 2 && operatorCount == 2) {
					System.out.println("Aplicando regra 3 Ativas (1M/2E): Equitativa (aprox 2/1)");
					assignRemainingEquitably(activeMachines, selectedOperators);
					ruleAppliedAndCompleted = checkAllActiveAssigned(activeMachines);
				}
				break;
				case 4:
				if (easyCount == 4 && operatorCount == 2) {
					System.out.println("Aplicando regra 4 Ativas (Todas F): Distribuição 3/1 - DEPRECATED, handled by AllEasy rule");
					// distribute4ActiveMachinesSpecial(activeMachines, selectedOperators);
					// ruleAppliedAndCompleted = checkAllActiveAssigned(activeMachines);
					} else if (((mediumCount == 2 && easyCount == 2) || (difficultCount == 1 && easyCount == 3)) && operatorCount == 2) {
					System.out.println("Aplicando regra 4 Ativas (2M/2E ou 1D/3E): Distribuição 3/1");
					distribute4ActiveMachinesSpecial(activeMachines, selectedOperators);
					ruleAppliedAndCompleted = checkAllActiveAssigned(activeMachines);
					} else if (operatorCount == 3) {
					System.out.println("Aplicando regra 4 Ativas (Outras): Equitativa (aprox 2/1/1)");
					assignRemainingEquitably(activeMachines, selectedOperators);
					ruleAppliedAndCompleted = checkAllActiveAssigned(activeMachines);
				}
				break;
				case 5:
				if (difficultCount == 1 && easyCount == 4 && operatorCount == 3) {
					System.out.println("Aplicando regra 5 Ativas (1D/4E): Equitativa (aprox 2/2/1)");
					assignRemainingEquitably(activeMachines, selectedOperators);
					ruleAppliedAndCompleted = checkAllActiveAssigned(activeMachines);
					} else if ((easyCount == 5 || (mediumCount == 1 && easyCount == 4)) && operatorCount == 2) {
					System.out.println("Aplicando regra 5 Ativas (Todas F ou 1M/4E): DEPRECATED, handled by AllEasy rule");
					// assignRemainingEquitably(activeMachines, selectedOperators);
					// ruleAppliedAndCompleted = checkAllActiveAssigned(activeMachines);
					} else if (operatorCount == 3){ // Default 3 operators for other mixes
					System.out.println("Aplicando regra 5 Ativas (Mix Complexo): Equitativa (aprox 2/2/1)");
					assignRemainingEquitably(activeMachines, selectedOperators);
					ruleAppliedAndCompleted = checkAllActiveAssigned(activeMachines);
				}
				break;
				case 6:
				if (easyCount == 6 && operatorCount == 2) {
					System.out.println("Aplicando regra 6 Ativas (Todas F): DEPRECATED, handled by AllEasy rule");
					// assignRemainingEquitably(activeMachines, selectedOperators);
					// ruleAppliedAndCompleted = checkAllActiveAssigned(activeMachines);
					} else if (operatorCount == 3) {
					System.out.println("Aplicando regra 6 Ativas (Com M ou D): Distribuição 1D / 1M+1E adj / Restantes E");
					distribute6ActiveMachinesMixedRevised(activeMachines, selectedOperators, originalListForAdjacency);
					ruleAppliedAndCompleted = checkAllActiveAssigned(activeMachines);
				}
				break;
				case 7:
				if (operatorCount == 3) {
					System.out.println("Aplicando regra específica para 7 máquinas ativas");
					distribute7ActiveMachines(activeMachines, selectedOperators);
					ruleAppliedAndCompleted = checkAllActiveAssigned(activeMachines);
				}
				break;
				// Cases 8-13: If not all easy (handled above), fall through to equitable distribution
				default:
				if (activeCount >= 8 && activeCount <= 13) {
					System.out.println("Aplicando regra para " + activeCount + " Ativas (Mix): Equitativa");
					assignRemainingEquitably(activeMachines, selectedOperators);
					ruleAppliedAndCompleted = checkAllActiveAssigned(activeMachines);
				}
				break;
			}
		}
		
		// Final check and warning if rule applied but didn't complete
		if (ruleAppliedAndCompleted) {
			long unassignedAfterRule = activeMachines.stream().filter(m -> m.getOperator().isEmpty()).count();
			if (unassignedAfterRule > 0) {
				System.out.println("ERRO INTERNO: Regra específica deveria ter sido aplicada e completada, mas " + unassignedAfterRule + " máquinas ativas permaneceram não atribuídas.");
				return false; // Indicate rule didn't fully complete, force fallback
			}
			} else {
			// If no specific rule was applied or completed, the fallback will handle it.
		}
		
		return ruleAppliedAndCompleted; // Return true only if a specific rule was applied AND handled all machines
	}
	
	// Helper to check if all active machines have an operator assigned
	private boolean checkAllActiveAssigned(List<Machine> activeMachines) {
		return activeMachines.stream().noneMatch(m -> m.getOperator().isEmpty());
	}
	
	// NEW: Specific rule for "All Easy, < 14 Active" scenario
	private void distributeAllEasyReducedScenario(List<Machine> activeMachines, List<Operator> selectedOperators) {
		if (selectedOperators == null || selectedOperators.isEmpty()) return;
		int numOperators = selectedOperators.size();
		if (numOperators == 1) {
			assignAllMachinesToOperator(activeMachines, selectedOperators.get(0));
			return;
		}
		// Assign 3 to all operators except the last one
		for (int i = 0; i < numOperators - 1; i++) {
			assignMachinesToOperator(activeMachines, selectedOperators.get(i), 3);
		}
		// Assign all remaining machines to the last operator
		assignRemainingMachinesToOperator(activeMachines, selectedOperators.get(numOperators - 1));
	}
	
	// Specific rule for 4 active machines -> 2 operators (3/1 distribution)
	private void distribute4ActiveMachinesSpecial(List<Machine> activeMachines, List<Operator> selectedOperators) {
		if (selectedOperators.size() < 2) return;
		assignMachinesToOperator(activeMachines, selectedOperators.get(0), 3);
		assignMachinesToOperator(activeMachines, selectedOperators.get(1), 1);
		// No fallback here, assume this rule handles all 4 machines
	}
	
	// Revised specific rule for 6 active machines with M or D -> 3 operators
	private void distribute6ActiveMachinesMixedRevised(List<Machine> activeMachines, List<Operator> selectedOperators, List<Machine> originalListForAdjacency) {
		if (selectedOperators.size() < 3) return;
		List<Machine> assignedInThisScenario = new ArrayList<>();
		Set<Operator> operatorsUsed = new HashSet<>();
		
		// Assign Difficult (if any) to Operator 1 (index 0)
		assignSpecificMachines(activeMachines, selectedOperators.get(0), 3, 1, assignedInThisScenario, operatorsUsed);
		
		// Assign Medium (if any) and its adjacent Easy to Operator 2 (index 1)
		int mediumMachineIndexInActive = findFirstMachineByDifficultyAndUnassigned(activeMachines, 2, assignedInThisScenario);
		if (mediumMachineIndexInActive != -1) {
			Machine mediumMachine = activeMachines.get(mediumMachineIndexInActive);
			assignOperatorToMachine(mediumMachine, selectedOperators.get(1), assignedInThisScenario, operatorsUsed);
			int originalMediumIndex = findMachineIndexInList(originalListForAdjacency, mediumMachine);
			if (originalMediumIndex != -1) {
				int adjacentEasyOriginalIndex = findAdjacentMachineAdvanced(originalListForAdjacency, originalMediumIndex, 1, assignedInThisScenario);
				if (adjacentEasyOriginalIndex != -1) {
					Machine adjacentEasy = originalListForAdjacency.get(adjacentEasyOriginalIndex);
					// Ensure the found adjacent machine is actually in the active list before assigning
					if (activeMachines.contains(adjacentEasy) && adjacentEasy.getOperator().isEmpty()) {
						System.out.println("Atribuindo adjacente " + adjacentEasy.getName() + " para " + selectedOperators.get(1).getName());
						assignOperatorToMachine(adjacentEasy, selectedOperators.get(1), assignedInThisScenario, operatorsUsed);
					}
				}
			}
			} else {
			// If no medium, assign one easy machine to operator 2 to keep it involved if possible
			assignSpecificMachines(activeMachines, selectedOperators.get(1), 1, 1, assignedInThisScenario, operatorsUsed);
		}
		
		// Assign ALL remaining unassigned active machines to Operator 3 (index 2)
		assignRemainingMachinesToOperator(activeMachines, selectedOperators.get(2));
	}
	
	// Specific rule for 7 active machines -> 3 operators
	private void distribute7ActiveMachines(List<Machine> activeMachines, List<Operator> selectedOperators) {
		if (selectedOperators.size() < 3) return;
		long difficultCount = activeMachines.stream().filter(m -> m.getDifficulty() == 3 && m.getOperator().isEmpty()).count();
		List<Machine> assignedInThisScenario = new ArrayList<>();
		Set<Operator> operatorsUsed = new HashSet<>();
		if (difficultCount >= 1) {
			System.out.println("Regra 7 Ativas: 1+ Difícil (3 Fácil / 3 Fácil / 1 Difícil)");
			assignSpecificMachines(activeMachines, selectedOperators.get(0), 1, 3, assignedInThisScenario, operatorsUsed);
			assignSpecificMachines(activeMachines, selectedOperators.get(1), 1, 3, assignedInThisScenario, operatorsUsed);
			assignSpecificMachines(activeMachines, selectedOperators.get(2), 3, 1, assignedInThisScenario, operatorsUsed);
			} else {
			System.out.println("Regra 7 Ativas: Nenhuma Difícil (Distribuição 3/2/2)");
			assignMachinesToOperator(activeMachines, selectedOperators.get(0), 3);
			assignMachinesToOperator(activeMachines, selectedOperators.get(1), 2);
			assignMachinesToOperator(activeMachines, selectedOperators.get(2), 2);
		}
		// This rule should handle all 7 machines, no explicit fallback needed here
	}
	
	// Try applying the original complex scenarios for 14 machines - returns true if a scenario was applied AND handled all machines
	private boolean tryOriginalScenarios(List<Machine> activeMachines, List<Operator> scenarioOperators, List<Machine> originalListForAdjacency) {
		long easyCount = activeMachines.stream().filter(m -> m.getDifficulty() == 1).count();
		long mediumCount = activeMachines.stream().filter(m -> m.getDifficulty() == 2).count();
		long difficultCount = activeMachines.stream().filter(m -> m.getDifficulty() == 3).count();
		int requiredOps = (mediumCount == 0 && difficultCount == 0 && easyCount == 14) ? 5 : 6;
		boolean scenarioApplied = false;
		
		if (scenarioOperators.size() != requiredOps) {
			System.out.println("Aviso: Número de operadores fornecidos (" + scenarioOperators.size() + ") não corresponde ao necessário (" + requiredOps + ") para o cenário de 14 máquinas.");
			return false;
		}
		
		if (requiredOps == 5 && easyCount == 14) {
			System.out.println("--- Executando Cenário Original: Tudo Fácil (14) ---");
			distributeAllEasyScenarioRotated(activeMachines, scenarioOperators);
			scenarioApplied = true;
			} else if (requiredOps == 6) {
			if (mediumCount >= 1 && difficultCount == 0) {
				System.out.println("--- Executando Cenário Original: Pelo Menos Uma Média ---");
				distributeOneMediumScenarioRotated(activeMachines, scenarioOperators, originalListForAdjacency);
				scenarioApplied = true;
				} else if (difficultCount >= 1) {
				System.out.println("--- Executando Cenário Original: Pelo Menos Uma Difícil ---");
				distributeOneDifficultScenarioRotatedCorrected(activeMachines, scenarioOperators, originalListForAdjacency);
				scenarioApplied = true;
			}
		}
		
		if (!scenarioApplied) {
			System.out.println("Mix de 14 máquinas não corresponde a nenhum cenário original definido.");
			return false;
		}
		
		// Check if the scenario actually assigned all machines
		long unassignedAfterScenario = activeMachines.stream().filter(m -> m.getOperator().isEmpty()).count();
		if (unassignedAfterScenario > 0) {
			System.out.println("Aviso: Cenário original aplicado, mas " + unassignedAfterScenario + " máquinas ativas permaneceram não atribuídas.");
			return false; // Indicate fallback needed
		}
		
		return true; // Scenario applied and handled all machines
	}
	
	// --- Original Scenario Implementations (Should handle all 14 machines) ---
	private void distributeAllEasyScenarioRotated(List<Machine> activeMachines, List<Operator> scenarioOperators) {
		if (scenarioOperators.size() < 5) return;
		assignMachinesToOperator(activeMachines, scenarioOperators.get(0), 3);
		assignMachinesToOperator(activeMachines, scenarioOperators.get(1), 3);
		assignMachinesToOperator(activeMachines, scenarioOperators.get(2), 3);
		assignMachinesToOperator(activeMachines, scenarioOperators.get(3), 3);
		assignMachinesToOperator(activeMachines, scenarioOperators.get(4), 2);
	}
	
	private void distributeOneMediumScenarioRotated(List<Machine> activeMachines, List<Operator> scenarioOperators, List<Machine> originalListForAdjacency) {
		if (scenarioOperators.size() < 6) return;
		List<Machine> assignedInThisScenario = new ArrayList<>(); Set<Operator> operatorsUsed = new HashSet<>();
		Operator op0=scenarioOperators.get(0), op1=scenarioOperators.get(1), op2=scenarioOperators.get(2),
		op3=scenarioOperators.get(3), op4=scenarioOperators.get(4), op5=scenarioOperators.get(5);
		
		assignSpecificMachines(activeMachines, op0, 1, 3, assignedInThisScenario, operatorsUsed);
		
		int medIdx3_active = findFirstMachineByDifficultyAndUnassigned(activeMachines, 2, assignedInThisScenario);
		if (medIdx3_active != -1) {
			Machine medMachine3 = activeMachines.get(medIdx3_active);
			assignOperatorToMachine(medMachine3, op2, assignedInThisScenario, operatorsUsed);
			int medIdx3_orig = findMachineIndexInList(originalListForAdjacency, medMachine3);
			if(medIdx3_orig != -1) {
				int adjEasy3_orig = findAdjacentMachineAdvanced(originalListForAdjacency, medIdx3_orig, 1, assignedInThisScenario);
				if (adjEasy3_orig != -1) {
					Machine adjEasy3 = originalListForAdjacency.get(adjEasy3_orig);
					if(activeMachines.contains(adjEasy3) && adjEasy3.getOperator().isEmpty()) assignOperatorToMachine(adjEasy3, op2, assignedInThisScenario, operatorsUsed);
				}
			}
		}
		
		int medIdx4_active = findFirstMachineByDifficultyAndUnassigned(activeMachines, 2, assignedInThisScenario);
		if (medIdx4_active != -1) {
			Machine medMachine4 = activeMachines.get(medIdx4_active);
			assignOperatorToMachine(medMachine4, op3, assignedInThisScenario, operatorsUsed);
			int medIdx4_orig = findMachineIndexInList(originalListForAdjacency, medMachine4);
			if(medIdx4_orig != -1) {
				int adjEasy4_orig = findAdjacentMachineAdvanced(originalListForAdjacency, medIdx4_orig, 1, assignedInThisScenario);
				if (adjEasy4_orig != -1) {
					Machine adjEasy4 = originalListForAdjacency.get(adjEasy4_orig);
					if(activeMachines.contains(adjEasy4) && adjEasy4.getOperator().isEmpty()) assignOperatorToMachine(adjEasy4, op3, assignedInThisScenario, operatorsUsed);
				}
			}
		}
		
		assignSpecificMachines(activeMachines, op1, 1, 3, assignedInThisScenario, operatorsUsed);
		assignSpecificMachines(activeMachines, op4, 1, 3, assignedInThisScenario, operatorsUsed);
		
		Machine lastActiveMachine = findLastUnassignedMachine(activeMachines, assignedInThisScenario);
		if(lastActiveMachine != null) {
			assignOperatorToMachine(lastActiveMachine, op5, assignedInThisScenario, operatorsUsed);
		}
	}
	
	private void distributeOneDifficultScenarioRotatedCorrected(List<Machine> activeMachines, List<Operator> scenarioOperators, List<Machine> originalListForAdjacency) {
		if (scenarioOperators.size() < 6) return;
		List<Machine> assignedInThisScenario = new ArrayList<>(); Set<Operator> operatorsUsed = new HashSet<>();
		Operator op0=scenarioOperators.get(0), op1=scenarioOperators.get(1), op2=scenarioOperators.get(2),
		op3=scenarioOperators.get(3), op4=scenarioOperators.get(4), op5=scenarioOperators.get(5);
		
		assignSpecificMachines(activeMachines, op0, 1, 3, assignedInThisScenario, operatorsUsed);
		assignSpecificMachines(activeMachines, op1, 1, 1, assignedInThisScenario, operatorsUsed);
		int medIdx2_active = findFirstMachineByDifficultyAndUnassigned(activeMachines, 2, assignedInThisScenario);
		if (medIdx2_active != -1) assignOperatorToMachine(activeMachines.get(medIdx2_active), op1, assignedInThisScenario, operatorsUsed);
		int medIdx5_active = findFirstMachineByDifficultyAndUnassigned(activeMachines, 2, assignedInThisScenario);
		if (medIdx5_active != -1) assignOperatorToMachine(activeMachines.get(medIdx5_active), op4, assignedInThisScenario, operatorsUsed);
		assignSpecificMachines(activeMachines, op4, 1, 1, assignedInThisScenario, operatorsUsed);
		assignSpecificMachines(activeMachines, op2, 1, 3, assignedInThisScenario, operatorsUsed);
		assignSpecificMachines(activeMachines, op3, 1, 1, assignedInThisScenario, operatorsUsed);
		assignSpecificMachines(activeMachines, op3, 1, 1, assignedInThisScenario, operatorsUsed);
		
		int diffIdx_active = findFirstMachineByDifficultyAndUnassigned(activeMachines, 3, assignedInThisScenario);
		if (diffIdx_active != -1) {
			Machine diffMachine = activeMachines.get(diffIdx_active);
			Operator opForDifficult = null;
			for(Operator op : scenarioOperators) { if (!operatorsUsed.contains(op)) { opForDifficult = op; break; } }
			if (opForDifficult != null) {
				System.out.println("Atribuindo máquina difícil ("+diffMachine.getName()+") para operador livre: " + opForDifficult.getName());
				assignOperatorToMachine(diffMachine, opForDifficult, assignedInThisScenario, operatorsUsed);
				} else {
				System.out.println("Aviso: NENHUM operador livre encontrado para a máquina difícil (Cenário Original). Atribuindo a \"não atribuído\".");
				assignOperatorToMachine(diffMachine, null, assignedInThisScenario, operatorsUsed);
			}
			
			int diffIdx_orig = findMachineIndexInList(originalListForAdjacency, diffMachine);
			if(diffIdx_orig != -1) {
				int adjEasy_orig = findAdjacentMachineAdvanced(originalListForAdjacency, diffIdx_orig, 1, assignedInThisScenario);
				if (adjEasy_orig != -1) {
					Machine adjEasy = originalListForAdjacency.get(adjEasy_orig);
					if(activeMachines.contains(adjEasy) && adjEasy.getOperator().isEmpty()) assignOperatorToMachine(adjEasy, op3, assignedInThisScenario, operatorsUsed);
				}
			}
		}
	}
	
	// --- Helper Methods (Mostly unchanged, ensure correctness) ---
	private void assignAllMachinesToOperator(List<Machine> machineList, Operator op) {
		String operatorName = (op != null) ? op.getName() : NOT_ASSIGNED;
		for (Machine machine : machineList) {
			if (machine.getOperator().isEmpty()) {
				machine.setOperator(operatorName);
			}
		}
	}
	private void clearCurrentDistribution(List<Machine> machineList) {
		for (Machine machine : machineList) {
			machine.setOperator("");
		}
	}
	// Assigns UP TO 'count' unassigned machines to the operator
	private int assignMachinesToOperator(List<Machine> machineList, Operator op, int count) {
		int assigned = 0;
		String operatorName = (op != null) ? op.getName() : NOT_ASSIGNED;
		for (int i = 0; i < machineList.size() && assigned < count; i++) {
			Machine machine = machineList.get(i);
			if (machine.getOperator().isEmpty()) {
				machine.setOperator(operatorName);
				assigned++;
			}
		}
		return assigned;
	}
	// Assigns ALL remaining unassigned machines equitably
	private void assignRemainingEquitably(List<Machine> machineList, List<Operator> selectedOperators) {
		if (selectedOperators == null || selectedOperators.isEmpty()) {
			System.out.println("Aviso: Tentativa de distribuição equitativa sem operadores selecionados.");
			return;
		}
		int opIndex = 0;
		int assignedCount = 0;
		for (int i = 0; i < machineList.size(); i++) {
			Machine machine = machineList.get(i);
			if (machine.getOperator().isEmpty()) {
				Operator currentOp = selectedOperators.get(opIndex % selectedOperators.size());
				machine.setOperator(currentOp.getName());
				opIndex++;
				assignedCount++;
			}
		}
		if (assignedCount > 0) {
			System.out.println("Distribuídas " + assignedCount + " máquinas restantes equitativamente.");
		}
	}
	// Assigns UP TO 'count' unassigned machines of specific difficulty
	private void assignSpecificMachines(List<Machine> machineList, Operator op, int difficulty, int count, List<Machine> alreadyAssigned, Set<Operator> operatorsUsed) {
		int assigned = 0;
		String operatorName = (op != null) ? op.getName() : NOT_ASSIGNED;
		boolean operatorAddedToUsedSet = false;
		for (Machine machine : machineList) {
			if (assigned >= count) break;
			// Check if active (implicit as it's in activeMachines list), difficulty matches, is unassigned, and not already processed in this specific rule run
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
	// Assigns a single machine to an operator, tracking usage
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
	private int findFirstMachineByDifficultyAndUnassigned(List<Machine> activeMachineList, int difficulty, List<Machine> alreadyAssigned) {
		for (int i = 0; i < activeMachineList.size(); i++) {
			Machine m = activeMachineList.get(i);
			if (m.getDifficulty() == difficulty && m.getOperator().isEmpty() && !alreadyAssigned.contains(m)) {
				return i;
			}
		}
		return -1;
	}
	// Marks any machine without an operator as NOT_ASSIGNED
	private void assignRemainingToNotAssigned(List<Machine> machineList) {
		for (Machine machine : machineList) {
			if (machine.getOperator() == null || machine.getOperator().isEmpty()) {
				machine.setOperator(NOT_ASSIGNED);
			}
		}
	}
	// Gets operator names for logging
	private String getOperatorNamesSimple(List<Operator> operators) {
		if (operators == null || operators.isEmpty()) return "[]";
		return "[" + operators.stream().map(Operator::getName).collect(Collectors.joining(", ")) + "]";
	}
	// Assigns ALL remaining unassigned machines to a single operator
	private void assignRemainingMachinesToOperator(List<Machine> machineList, Operator op) {
		String operatorName = (op != null) ? op.getName() : NOT_ASSIGNED;
		for (Machine machine : machineList) {
			if (machine.getOperator().isEmpty()) {
				machine.setOperator(operatorName);
			}
		}
	}
	// Finds the index of a machine in a list (using equals and number fallback)
	private int findMachineIndexInList(List<Machine> list, Machine targetMachine) {
		if (targetMachine == null || list == null) return -1;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(targetMachine)) { return i; }
		}
		// Fallback to checking by number if equals doesn't match (e.g., different instances)
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getNumber() == targetMachine.getNumber()) { return i; }
		}
		return -1;
	}
	// Finds the last unassigned machine in the active list
	private Machine findLastUnassignedMachine(List<Machine> activeMachines, List<Machine> assignedInThisScenario) {
		for (int i = activeMachines.size() - 1; i >= 0; i--) {
			Machine m = activeMachines.get(i);
			if (m.getOperator().isEmpty() && !assignedInThisScenario.contains(m)) {
				return m;
			}
		}
		return null;
	}
	
	// Corrected adjacency search on the original list, skipping inactive/assigned
	private int findAdjacentMachineAdvanced(List<Machine> originalList, int originalPos, int targetDifficulty, List<Machine> assignedInThisScenario) {
		if (originalPos < 0 || originalPos >= originalList.size()) return -1;
		int maxOffset = originalList.size();
		for (int offset = 1; offset < maxOffset; offset++) {
			int rightIdx = originalPos + offset;
			if (rightIdx < originalList.size()) {
				Machine candidate = originalList.get(rightIdx);
				// Check if ON, difficulty matches, NOT assigned globally, and NOT assigned in this specific rule run
				if (candidate.isOn() && candidate.getOperator().isEmpty() && !assignedInThisScenario.contains(candidate) && candidate.getDifficulty() == targetDifficulty) {
					// System.out.println("Adjacente encontrado (direita, offset "+offset+"): " + candidate.getName());
					return rightIdx;
				}
			}
			int leftIdx = originalPos - offset;
			if (leftIdx >= 0) {
				Machine candidate = originalList.get(leftIdx);
				if (candidate.isOn() && candidate.getOperator().isEmpty() && !assignedInThisScenario.contains(candidate) && candidate.getDifficulty() == targetDifficulty) {
					// System.out.println("Adjacente encontrado (esquerda, offset "+offset+"): " + candidate.getName());
					return leftIdx;
				}
			}
			if (rightIdx >= originalList.size() && leftIdx < 0) {
				break; // No more possible candidates
			}
		}
		return -1;
	}
}