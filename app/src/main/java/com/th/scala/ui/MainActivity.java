package com.th.scala.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

// Renaming the final version incorporating history, menu, and persistent order
public class MainActivity extends Activity {
	
	private static final String HISTORY_FILENAME = "distribution_history.txt";
	private static final String TAG = "MainActivity";
	private static final String PREFS_NAME = "OperatorOrderPrefs";
	private static final String PREF_KEY_OPERATOR_ORDER = "lastOperatorOrder";
	
	private MachineAdapter adapter;
	private MachineDistributor distributor;
	private MachineFactory factory;
	private List<Operator> operatorList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Initialize services
		distributor = new MachineDistributor();
		factory = new MachineFactory();
		
		// Load the last operator order or create the initial one
		operatorList = loadOperatorOrder(this);
		if (operatorList == null || operatorList.isEmpty()) {
			operatorList = createInitialOperators();
			Log.i(TAG, "Nenhuma ordem salva encontrada, usando ordem inicial.");
			} else {
			Log.i(TAG, "Ordem dos operadores carregada: " + getOperatorNames(operatorList));
		}
		
		// Configura ListView
		ListView listView = (ListView) findViewById(R.id.list_view);
		adapter = new MachineAdapter(this, factory.createDefaultMachines());
		listView.setAdapter(adapter);
		
		// Configura botão de distribuição
		Button distributeBtn = (Button) findViewById(R.id.distribute_button);
		distributeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 1. Rotate the operator list before distribution
				if (operatorList != null && !operatorList.isEmpty()) {
					Collections.rotate(operatorList, -1);
					Log.i(TAG, "Ordem rotacionada: " + getOperatorNames(operatorList));
				}
				
				List<Machine> currentMachines = adapter.getMachines();
				
				// 2. Distribute machines using the rotated list
				distributor.distributeMachines(currentMachines, operatorList);
				
				// 3. Save the distribution result to history file
				saveDistributionHistory(MainActivity.this, currentMachines, operatorList);
				
				// 4. Save the current operator order for persistence
				saveOperatorOrder(MainActivity.this, operatorList);
				
				// 5. Update the UI
				adapter.notifyDataSetChanged();
				Toast.makeText(MainActivity.this, "Máquinas distribuídas e histórico salvo!", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	// --- Menu Handling ---
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_view_history) {
			Intent intent = new Intent(this, HistoryActivity.class);
			startActivity(intent);
			return true;
			} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	// --- Operator Order Persistence ---
	
	private void saveOperatorOrder(Context context, List<Operator> operators) {
		if (operators == null || operators.isEmpty()) {
			return;
		}
		// Convert list of operator names to a single comma-separated string
		String orderString = operators.stream()
		.map(Operator::getName)
		.collect(Collectors.joining(","));
		
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREF_KEY_OPERATOR_ORDER, orderString);
		editor.apply(); // Use apply() for asynchronous saving
		Log.i(TAG, "Ordem dos operadores salva: " + orderString);
	}
	
	private List<Operator> loadOperatorOrder(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		String savedOrder = prefs.getString(PREF_KEY_OPERATOR_ORDER, null);
		
		if (savedOrder == null || savedOrder.isEmpty()) {
			return null; // No saved order found
		}
		
		// Split the string back into names
		List<String> operatorNames = Arrays.asList(savedOrder.split(","));
		
		// Reconstruct the Operator list based on the saved order of names
		// This assumes the Operator objects themselves don't need saving, only their order.
		// We create new Operator objects based on the names in the saved order.
		List<Operator> loadedOperators = new ArrayList<>();
		for (String name : operatorNames) {
			// Find the corresponding operator details (availability, capacity) if needed
			// For simplicity, we create new ones with default values matching createInitialOperators
			// A more robust approach might involve saving/loading operator details too, or having a master list.
			loadedOperators.add(new Operator(name, true, 3)); // Assuming default capacity 3
		}
		return loadedOperators;
	}
	
	// --- Initial Operator Creation and History Methods ---
	
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
		// Use stream for cleaner joining
		return "[" + operators.stream().map(Operator::getName).collect(Collectors.joining(", ")) + "]";
	}
}