package com.th.scala;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {
	private List<Machine> machineList = new ArrayList<>();
	private MachineAdapter adapter;
	private List<String> operators = new ArrayList<>();
	private int lastAssignedOperatorIndex = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Operadores disponíveis
		operators.add("Operador 1");
		operators.add("Operador 2");
		operators.add("Operador 3");
		operators.add("Operador 4");
		operators.add("Operador 5");
		
		ListView listView = findViewById(R.id.list_view);
		machineList = createMachinesWithSpecificProducts();
		adapter = new MachineAdapter(this, machineList);
		listView.setAdapter(adapter);
		
		Button distributeButton = findViewById(R.id.distribute_button);
		distributeButton.setOnClickListener(v -> {
			distributeMachinesWithProximity();
			adapter.notifyDataSetChanged();
			logDistribution();
		});
	}
	
	private List<Machine> createMachinesWithSpecificProducts() {
		List<Machine> machines = new ArrayList<>();
		
		// Máquinas com posições simuladas (ordem na lista representa proximidade)
		// Máquinas difíceis (3)
		machines.add(new Machine("Máquina 1", 901, "", "Motor Diesel", 3, true, 1));
		machines.add(new Machine("Máquina 2", 902, "", "Turbina", 3, true, 2));
		machines.add(new Machine("Máquina 3", 903, "", "Transmissão", 3, true, 3));
		
		// Máquinas médias (5)
		machines.add(new Machine("Máquina 4", 904, "", "Eixo Traseiro", 2, false, 4));
		machines.add(new Machine("Máquina 5", 905, "", "Suspensão", 2, false, 5));
		machines.add(new Machine("Máquina 6", 906, "", "Freios", 2, false, 6));
		machines.add(new Machine("Máquina 7", 907, "", "Direção", 2, false, 7));
		machines.add(new Machine("Máquina 8", 908, "", "Radiador", 2, false, 8));
		
		// Máquinas fáceis (6)
		machines.add(new Machine("Máquina 9", 909, "", "Parafusos", 1, false, 9));
		machines.add(new Machine("Máquina 10", 910, "", "Arruelas", 1, false, 10));
		machines.add(new Machine("Máquina 11", 911, "", "Porcas", 1, false, 11));
		machines.add(new Machine("Máquina 12", 912, "", "Buchas", 1, false, 12));
		machines.add(new Machine("Máquina 13", 913, "", "Retentores", 1, false, 13));
		machines.add(new Machine("Máquina 14", 914, "", "Anéis", 1, false, 14));
		
		return machines;
	}
	
	private void distributeMachinesWithProximity() {
		// Limpa operadores atuais
		for (Machine machine : machineList) {
			machine.setOperator("");
		}
		
		// Embaralha operadores para rotatividade (mantendo a ordem circular)
		if (lastAssignedOperatorIndex == -1) {
			Collections.shuffle(operators);
			} else {
			// Rotaciona a lista começando pelo próximo operador
			Collections.rotate(operators, - (lastAssignedOperatorIndex + 1) % operators.size());
		}
		
		// Separa máquinas por dificuldade
		List<Machine> hardMachines = new ArrayList<>();
		List<Machine> mediumMachines = new ArrayList<>();
		List<Machine> easyMachines = new ArrayList<>();
		
		for (Machine machine : machineList) {
			switch (machine.getDifficulty()) {
				case 3: hardMachines.add(machine); break;
				case 2: mediumMachines.add(machine); break;
				case 1: easyMachines.add(machine); break;
			}
		}
		
		// Distribuição com prioridade de proximidade
		distributeHardMachines(hardMachines);
		distributeMediumMachines(mediumMachines);
		distributeEasyMachines(easyMachines);
	}
	
	private void distributeHardMachines(List<Machine> hardMachines) {
		// Cada máquina difícil para um operador diferente
		for (int i = 0; i < hardMachines.size() && i < operators.size(); i++) {
			hardMachines.get(i).setOperator(operators.get(i));
			lastAssignedOperatorIndex = i;
		}
	}
	
	private void distributeMediumMachines(List<Machine> mediumMachines) {
		int operatorIndex = (lastAssignedOperatorIndex + 1) % operators.size();
		
		while (!mediumMachines.isEmpty()) {
			String currentOperator = operators.get(operatorIndex);
			
			// Encontra a próxima máquina média disponível
			Machine firstMachine = findNextAvailableMachine(mediumMachines, -1);
			if (firstMachine == null) break;
			
			firstMachine.setOperator(currentOperator);
			mediumMachines.remove(firstMachine);
			
			// Tenta encontrar uma máquina próxima (até 2 posições de distância)
			Machine secondMachine = findClosestAvailableMachine(mediumMachines, firstMachine.getPosition(), 2);
			if (secondMachine != null) {
				secondMachine.setOperator(currentOperator);
				mediumMachines.remove(secondMachine);
			}
			
			operatorIndex = (operatorIndex + 1) % operators.size();
			lastAssignedOperatorIndex = operatorIndex;
		}
	}
	
	private void distributeEasyMachines(List<Machine> easyMachines) {
		int operatorIndex = (lastAssignedOperatorIndex + 1) % operators.size();
		
		while (!easyMachines.isEmpty()) {
			String currentOperator = operators.get(operatorIndex);
			
			// Encontra a próxima máquina fácil disponível
			Machine firstMachine = findNextAvailableMachine(easyMachines, -1);
			if (firstMachine == null) break;
			
			firstMachine.setOperator(currentOperator);
			easyMachines.remove(firstMachine);
			
			// Tenta encontrar duas máquinas próximas (até 2 posições de distância)
			for (int i = 0; i < 2 && !easyMachines.isEmpty(); i++) {
				Machine nextMachine = findClosestAvailableMachine(easyMachines, firstMachine.getPosition(), 2);
				if (nextMachine != null) {
					nextMachine.setOperator(currentOperator);
					easyMachines.remove(nextMachine);
				}
			}
			
			operatorIndex = (operatorIndex + 1) % operators.size();
			lastAssignedOperatorIndex = operatorIndex;
		}
	}
	
	private Machine findNextAvailableMachine(List<Machine> machines, int lastPosition) {
		if (machines.isEmpty()) return null;
		
		// Ordena por posição para garantir proximidade
		machines.sort((m1, m2) -> Integer.compare(m1.getPosition(), m2.getPosition()));
		
		return machines.get(0);
	}
	
	private Machine findClosestAvailableMachine(List<Machine> machines, int referencePosition, int maxDistance) {
		if (machines.isEmpty()) return null;
		
		Machine closest = null;
		int minDistance = Integer.MAX_VALUE;
		
		for (Machine machine : machines) {
			int distance = Math.abs(machine.getPosition() - referencePosition);
			if (distance <= maxDistance && distance < minDistance) {
				closest = machine;
				minDistance = distance;
			}
		}
		
		return closest;
	}
	
	private void logDistribution() {
		for (String operator : operators) {
			StringBuilder assignedMachines = new StringBuilder();
			for (Machine machine : machineList) {
				if (operator.equals(machine.getOperator())) {
					assignedMachines.append(machine.getName())
					.append("(")
					.append(getDifficultyLetter(machine.getDifficulty()))
					.append(") ");
				}
			}
			Log.d("Distribuição", operator + ": " + assignedMachines.toString());
		}
	}
	
	private String getDifficultyLetter(int difficulty) {
		switch (difficulty) {
			case 3: return "D";
			case 2: return "M";
			case 1: return "F";
			default: return "?";
		}
	}
}