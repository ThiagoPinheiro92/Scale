package com.th.scala.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import com.th.scala.R;
import com.th.scala.core.adapters.MachineAdapter;
import com.th.scala.core.models.Operator;
import com.th.scala.core.models.Machine;
import com.th.scala.core.services.MachineDistributor;
import com.th.scala.core.services.MachineFactory;
import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private MachineDistributor distributor;
	private MachineFactory factory;
	private MachineAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Inicializa serviços
		distributor = new MachineDistributor();
		factory = new MachineFactory();
		
		// Configura ListView e Adapter
		ListView listView = findViewById(R.id.list_view);
		adapter = new MachineAdapter(this, factory.createDefaultMachines());
		listView.setAdapter(adapter);
		
		// Configura botão de distribuição
		Button distributeBtn = findViewById(R.id.distribute_button);
		distributeBtn.setOnClickListener(v -> {
			// Verifica se todas as máquinas têm dificuldade definida
			boolean allMachinesHaveDifficulty = true;
			for (Machine machine : adapter.getMachines()) {
				if (machine.getDifficulty() < 1 || machine.getDifficulty() > 3) {
					allMachinesHaveDifficulty = false;
					break;
				}
			}
			
			if (!allMachinesHaveDifficulty) {
				Toast.makeText(MainActivity.this,
				"Defina a dificuldade para todas as máquinas (Fácil, Médio ou Difícil)",
				Toast.LENGTH_LONG).show();
				return;
			}
			
			List<Operator> operators = createOperators();
			distributor.distributeMachines(adapter.getMachines(), operators);
			adapter.notifyDataSetChanged();
		});
		}
	
	private List<Operator> createOperators() {
		List<Operator> operators = new ArrayList<>();
		operators.add(new Operator("Operador 1", true, 4));
		operators.add(new Operator("Operador 2", false, 3));
		operators.add(new Operator("Operador 3", true, 4));
		operators.add(new Operator("Operador 4", false, 3));
		operators.add(new Operator("Operador 5", true, 4));
		return operators;
	}
}