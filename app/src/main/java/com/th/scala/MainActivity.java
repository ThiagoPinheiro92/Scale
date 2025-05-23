package com.th.scala;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
	private List<Machine> machineList = new ArrayList<>();
	private MachineAdapter adapter;
	private List<String> operators = new ArrayList<>();
	private Random random = new Random();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Inicializa a lista de operadores
		operators.add("Operador 1");
		operators.add("Operador 2");
		operators.add("Operador 3");
		operators.add("Operador 4");
		operators.add("Operador 5");
		
		ListView listView = findViewById(R.id.list_view);
		machineList = getMachines();
		adapter = new MachineAdapter(this, machineList);
		listView.setAdapter(adapter);
		
		Button distributeButton = findViewById(R.id.distribute_button);
		distributeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				distributeMachines();
				adapter.notifyDataSetChanged();
			}
		});
	}
	
	private List<Machine> getMachines() {
		List<Machine> list = new ArrayList<>();
		String[] products = {"Parafuso", "Porca", "Arruela", "Engrenagem"};
		
		for (int i = 1; i <= 14; i++) {
			String product = products[i % products.length];
			int difficulty;
			
			if (i <= 3) {
				difficulty = 3; // Difícil (D)
				} else if (i <= 8) {
				difficulty = 2; // Médio (M)
				} else {
				difficulty = 1; // Fácil (F)
			}
			
			list.add(new Machine(
			"Máquina " + i,
			900 + i,
			"", // Operador vazio inicialmente
			product,
			difficulty
			));
		}
		return list;
	}
	
	private void distributeMachines() {
		// Limpa os operadores atuais
		for (Machine machine : machineList) {
			machine.setOperator("");
		}
		
		// Embaralha a lista de operadores para rotatividade
		Collections.shuffle(operators);
		
		// Separa as máquinas por dificuldade
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
		
		// Distribui máquinas difíceis (1 por operador)
		for (int i = 0; i < hardMachines.size() && i < operators.size(); i++) {
			hardMachines.get(i).setOperator(operators.get(i));
		}
		
		// Distribui máquinas médias
		int operatorIndex = 0;
		while (!mediumMachines.isEmpty()) {
			String currentOperator = operators.get(operatorIndex % operators.size());
			
			// Pega 2 máquinas médias ou 1 média e 1 fácil
			if (mediumMachines.size() >= 2) {
				mediumMachines.remove(0).setOperator(currentOperator);
				mediumMachines.remove(0).setOperator(currentOperator);
				} else if (!mediumMachines.isEmpty() && !easyMachines.isEmpty()) {
				mediumMachines.remove(0).setOperator(currentOperator);
				if (!easyMachines.isEmpty()) {
					easyMachines.remove(0).setOperator(currentOperator);
				}
			}
			
			operatorIndex++;
		}
		
		// Distribui máquinas fáceis (3 por operador)
		operatorIndex = 0;
		while (!easyMachines.isEmpty()) {
			String currentOperator = operators.get(operatorIndex % operators.size());
			
			for (int i = 0; i < 3 && !easyMachines.isEmpty(); i++) {
				easyMachines.remove(0).setOperator(currentOperator);
			}
			
			operatorIndex++;
		}
	}
}