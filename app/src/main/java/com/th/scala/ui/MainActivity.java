package com.th.scala.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Renaming the final version incorporating history and menu
public class MainActivity extends Activity {
	
	private static final String HISTORY_FILENAME = "distribution_history.txt";
	private static final String TAG = "MainActivity";
	
	private MachineAdapter adapter;
	private MachineDistributor distributor;
	private MachineFactory factory;
	private List<Operator> operatorList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Assuming you have a layout file named activity_main.xml
		// Make sure it includes a Toolbar if you want the menu there, or it will use the default ActionBar
		setContentView(R.layout.activity_main);
		
		// Initialize services
		distributor = new MachineDistributor();
		factory = new MachineFactory();
		
		// Initialize the operator list once
		operatorList = createInitialOperators();
		
		// Configura ListView
		// Ensure R.id.list_view exists in your activity_main.xml
		ListView listView = (ListView) findViewById(R.id.list_view);
		adapter = new MachineAdapter(this, factory.createDefaultMachines());
		listView.setAdapter(adapter);
		
		// Configura botão de distribuição
		// Ensure R.id.distribute_button exists in your activity_main.xml
		Button distributeBtn = (Button) findViewById(R.id.distribute_button);
		distributeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 1. Rotate the operator list before distribution
				if (operatorList != null && !operatorList.isEmpty()) {
					Collections.rotate(operatorList, -1);
				}
				
				List<Machine> currentMachines = adapter.getMachines();
				
				// 2. Distribute machines using the rotated list
				distributor.distributeMachines(currentMachines, operatorList);
				
				// 3. Save the distribution result to history file
				saveDistributionHistory(MainActivity.this, currentMachines, operatorList);
				
				// 4. Update the UI
				adapter.notifyDataSetChanged();
				Toast.makeText(MainActivity.this, "Máquinas distribuídas e histórico salvo!", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	// --- Menu Handling ---
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		// Assuming you saved the menu file as res/menu/menu_main.xml
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		// Use if-else if structure if you add more menu items
		if (item.getItemId() == R.id.action_view_history) {
			// Create Intent to launch HistoryActivity
			Intent intent = new Intent(this, HistoryActivity.class);
			startActivity(intent);
			return true;
			} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	// --- Operator and History Methods ---
	
	private List<Operator> createInitialOperators() {
		List<Operator> operators = new ArrayList<>();
		operators.add(new Operator("Operador 1", true, 3));
		operators.add(new Operator("Operador 2", true, 3));
		operators.add(new Operator("Operador 3", true, 3));
		operators.add(new Operator("Operador 4", true, 3));
		operators.add(new Operator("Operador 5", true, 3));
		operators.add(new Operator("Operador 6", true, 3));
		return operators;
	}
	
	private void saveDistributionHistory(Context context, List<Machine> machines, List<Operator> operatorsUsed) {
		StringBuilder historyEntry = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		String timestamp = sdf.format(new Date());
		
		historyEntry.append("--- Histórico de Distribuição [" + timestamp + "] ---\n");
		historyEntry.append("Ordem dos Operadores Utilizada: " + getOperatorNames(operatorsUsed) + "\n");
		historyEntry.append("Máquinas Distribuídas:\n");
		
		if (machines == null || machines.isEmpty()) {
			historyEntry.append("  (Nenhuma máquina para distribuir)\n");
			} else {
			for (Machine machine : machines) {
				historyEntry.append("  - Máquina: " + machine.getName() + " (" + machine.getNumber() + ")" +
				" -> Operador: " + (machine.getOperator() != null && !machine.getOperator().isEmpty() ? machine.getOperator() : "(não atribuído)") + "\n");
			}
		}
		historyEntry.append("--- Fim da Entrada [" + timestamp + "] ---\n\n");
		
		try {
			File historyFile = new File(context.getExternalFilesDir(null), HISTORY_FILENAME);
			FileOutputStream fos = new FileOutputStream(historyFile, true);
			OutputStreamWriter writer = new OutputStreamWriter(fos);
			writer.append(historyEntry.toString());
			writer.close();
			fos.close();
			Log.i(TAG, "Histórico de distribuição salvo em: " + historyFile.getAbsolutePath());
			} catch (IOException e) {
			Log.e(TAG, "Erro ao salvar histórico de distribuição", e);
			Toast.makeText(context, "Erro ao salvar histórico: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
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