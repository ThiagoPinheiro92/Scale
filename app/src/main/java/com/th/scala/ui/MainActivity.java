package com.th.scala.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.th.scala.R;
import com.th.scala.core.adapters.MachineAdapter;
import com.th.scala.core.models.Machine;
import com.th.scala.core.models.Operator;
import com.th.scala.core.services.MachineDistributor;
import com.th.scala.core.services.MachineFactory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
	
	private MachineAdapter adapter;
	private MachineDistributor distributor;
	private MachineFactory factory;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Inicializa serviços
		distributor = new MachineDistributor();
		factory = new MachineFactory();
		
		// Configura ListView
		ListView listView = (ListView) findViewById(R.id.list_view);
		adapter = new MachineAdapter(this, factory.createDefaultMachines());
		listView.setAdapter(adapter);
		
		// Configura botão de distribuição
		Button distributeBtn = (Button) findViewById(R.id.distribute_button);
		distributeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				List<Operator> operators = createOperators();
				distributor.distributeMachines(adapter.getMachines(), operators);
				adapter.notifyDataSetChanged();
				Toast.makeText(MainActivity.this, "Máquinas distribuídas!", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private List<Operator> createOperators() {
		List<Operator> operators = new ArrayList<>();
		operators.add(new Operator("Operador 1", true, 3));
		operators.add(new Operator("Operador 2", true, 3));
		operators.add(new Operator("Operador 3", true, 3));
		operators.add(new Operator("Operador 4", true, 3));
		operators.add(new Operator("Operador 5", true, 3));
		operators.add(new Operator("Operador 6", true, 3)); 
		return operators;
	}
}