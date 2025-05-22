package com.th.scala;

import android.os.Bundle;
import android.app.Activity;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
	private List<Machine> machineList = new ArrayList<>();
	private MachineAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ListView listView = findViewById(R.id.list_view);
		machineList = getMachines();
		adapter = new MachineAdapter(this, machineList);
		listView.setAdapter(adapter);
	}
	
	private List<Machine> getMachines() {
		List<Machine> list = new ArrayList<>();
		String[] products = {"Parafuso", "Porca", "Arruela", "Engrenagem"};
		
		for (int i = 1; i <= 16; i++) {
			String product = products[i % products.length];
			int difficulty = (i % 3) + 1; // Dificuldade entre 1 e 3
			list.add(new Machine(
			"Máquina " + i,
			900 + i,
			"Operador " + i,
			product,
			difficulty
			));
		}
		return list;
	}
}