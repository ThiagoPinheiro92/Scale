package com.th.scala.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.th.scala.R;
import com.th.scala.core.adapters.MachineAdapter;
import com.th.scala.core.models.Machine;
import com.th.scala.core.models.Operator;
import com.th.scala.core.services.MachineDistributor;
import com.th.scala.core.services.MachineFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {
	
	private MachineAdapter adapter;
	private MachineDistributor distributor;
	private MachineFactory factory;
	private List<Operator> operatorList; // Store the list of operators as a member
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Initialize services
		distributor = new MachineDistributor();
		factory = new MachineFactory();
		
		// Initialize the operator list once
		operatorList = createInitialOperators();
		
		// Configura ListView
		ListView listView = (ListView) findViewById(R.id.list_view);
		// Assuming createDefaultMachines gives the initial state
		adapter = new MachineAdapter(this, factory.createDefaultMachines());
		listView.setAdapter(adapter);
		
		// Configura botão de distribuição
		Button distributeBtn = (Button) findViewById(R.id.distribute_button);
		distributeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 1. Rotate the operator list before distribution
				if (operatorList != null && !operatorList.isEmpty()) {
					// Rotate left: the first operator becomes the last
					Collections.rotate(operatorList, -1);
					// Optional: Log or Toast the new order for debugging
					// Toast.makeText(MainActivity.this, "Nova ordem: " + getOperatorNames(operatorList), Toast.LENGTH_SHORT).show();
				}
				
				// 2. Distribute machines using the rotated list
				// Make sure adapter.getMachines() returns the current list of machines from the UI
				distributor.distributeMachines(adapter.getMachines(), operatorList);
				
				// 3. Update the UI
				adapter.notifyDataSetChanged();
				Toast.makeText(MainActivity.this, "Máquinas distribuídas com nova ordem!", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	// Creates the initial list of operators
	private List<Operator> createInitialOperators() {
		List<Operator> operators = new ArrayList<>();
		// Add operators in the initial desired order (e.g., 1 to 6)
		operators.add(new Operator("Operador 1", true, 3));
		operators.add(new Operator("Operador 2", true, 3));
		operators.add(new Operator("Operador 3", true, 3));
		operators.add(new Operator("Operador 4", true, 3));
		operators.add(new Operator("Operador 5", true, 3));
		operators.add(new Operator("Operador 6", true, 3));
		return operators;
	}
	
	// Helper method for debugging (optional)
	private String getOperatorNames(List<Operator> operators) {
		if (operators == null || operators.isEmpty()) {
			return "[]";
		}
		StringBuilder names = new StringBuilder("[");
		for (int i = 0; i < operators.size(); i++) {
			names.append(operators.get(i).getName());
			if (i < operators.size() - 1) {
				names.append(", ");
			}
		}
		names.append("]");
		return names.toString();
	}
}