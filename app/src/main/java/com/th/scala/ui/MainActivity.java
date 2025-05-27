package com.th.scala.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
// import android.os.Environment; // Not strictly needed for getExternalFilesDir
import android.text.TextUtils; // Needed for TextUtils.join potentially if not using streams
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

// Assuming R class is generated correctly in this package or imported
import com.th.scala.R;
// Import the UPDATED classes
import com.th.scala.core.adapters.MachineAdapter; // Use the adapter with toggle
import com.th.scala.core.models.Machine;          // Use the machine with state
import com.th.scala.core.models.Operator;
import com.th.scala.core.services.MachineDistributor; // Use the distributor that filters
import com.th.scala.core.services.MachineFactory; // Assuming this still creates Machines (now with default isOn=true)

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
import java.util.stream.Collectors; // Keep for operator names and potentially machine filtering if needed here

public class MainActivity extends Activity {
	
	private static final String HISTORY_FILENAME = "distribution_history.txt";
	private static final String TAG = "MainActivityToggle"; // Updated TAG for clarity
	private static final String PREFS_NAME = "OperatorOrderPrefs";
	private static final String PREF_KEY_OPERATOR_ORDER = "lastOperatorOrder";
	// Optional: Add prefs for machine states if needed across app restarts
	// private static final String PREF_KEY_MACHINE_STATES = "lastMachineStates";
	
	private MachineAdapter adapter; // Use the updated adapter
	private MachineDistributor distributor; // Use the updated distributor
	private MachineFactory factory;
	private List<Operator> operatorList;
	private List<Machine> machineList; // Keep a reference to the list used by the adapter
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Ensure this layout contains R.id.list_view and R.id.distribute_button
		setContentView(R.layout.activity_main);
		
		// Initialize services
		distributor = new MachineDistributor(); // Instantiate the filtered distributor
		factory = new MachineFactory(); // Assuming this creates Machines with isOn=true by default
		
		// Load the last operator order or create the initial one
		operatorList = loadOperatorOrder(this);
		if (operatorList == null || operatorList.isEmpty()) {
			operatorList = createInitialOperators();
			Log.i(TAG, "Nenhuma ordem salva encontrada, usando ordem inicial.");
			} else {
			Log.i(TAG, "Ordem dos operadores carregada: " + getOperatorNames(operatorList));
		}
		
		// Create or load machine list (including their states)
		// For simplicity, we create default machines here.
		// If you need persistence for machine ON/OFF states across app restarts,
		// you would load them here, similar to how operator order is loaded.
		machineList = factory.createDefaultMachines();
		// loadMachineStates(this, machineList); // Example call if persistence was added
		
		// Configura ListView
		ListView listView = (ListView) findViewById(R.id.list_view);
		// Use the updated adapter and the machine list
		adapter = new MachineAdapter(this, machineList);
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
				
				// 2. Get the current machine list FROM THE ADAPTER
				// This list now reflects the ON/OFF states set by the user via the buttons
				List<Machine> currentMachines = adapter.getMachines();
				
				// 3. Distribute machines using the rotated list and filtered distributor
				// The distributor will internally filter for active machines
				distributor.distributeMachines(currentMachines, operatorList);
				
				// 4. Save the distribution result to history file
				// History will show operator assignments only for machines that were ON
				// and processed by the distributor. OFF machines will show as "(não atribuído)".
				saveDistributionHistory(MainActivity.this, currentMachines, operatorList);
				
				// 5. Save the current operator order for persistence
				saveOperatorOrder(MainActivity.this, operatorList);
				
				// Optional: Save machine states if persistence is needed
				// saveMachineStates(MainActivity.this, currentMachines);
				
				// 6. Update the UI - IMPORTANT to refresh operator assignments shown in the list
				adapter.notifyDataSetChanged();
				Toast.makeText(MainActivity.this, "Máquinas distribuídas e histórico salvo!", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	// --- Menu Handling (No changes needed here) ---
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu); // Use the corrected menu_main.xml
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Use if-else if if more items are added
		if (item.getItemId() == R.id.action_view_history) {
			Intent intent = new Intent(this, HistoryActivity.class); // Use HistoryActivity_autoscroll if renamed
			startActivity(intent);
			return true;
			} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	// --- Operator Order Persistence (No changes needed here) ---
	
	private void saveOperatorOrder(Context context, List<Operator> operators) {
		if (operators == null || operators.isEmpty()) { return; }
		String orderString = operators.stream().map(Operator::getName).collect(Collectors.joining(","));
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREF_KEY_OPERATOR_ORDER, orderString);
		editor.apply();
		Log.i(TAG, "Ordem dos operadores salva: " + orderString);
	}
	
	private List<Operator> loadOperatorOrder(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		String savedOrder = prefs.getString(PREF_KEY_OPERATOR_ORDER, null);
		if (savedOrder == null || savedOrder.isEmpty()) { return null; }
		List<String> operatorNames = Arrays.asList(savedOrder.split(","));
		List<Operator> loadedOperators = new ArrayList<>();
		List<Operator> initialOperators = createInitialOperators(); // Get defaults to find matching details
		for (String name : operatorNames) {
			// Find the original operator by name to keep its properties (like capacity)
			Operator foundOp = initialOperators.stream()
			.filter(op -> op.getName().equals(name))
			.findFirst()
			.orElse(new Operator(name, true, 3)); // Fallback if name not found
			loadedOperators.add(foundOp);
		}
		return loadedOperators;
	}
	
	// --- Machine State Persistence (Optional - Example) ---
	/*
	private void saveMachineStates(Context context, List<Machine> machines) {
		if (machines == null || machines.isEmpty()) { return; }
		// Example: Save as "machineNumber1:state1,machineNumber2:state2,..."
		String stateString = machines.stream()
		.map(m -> m.getNumber() + ":" + m.isOn())
		.collect(Collectors.joining(","));
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREF_KEY_MACHINE_STATES, stateString);
		editor.apply();
		Log.i(TAG, "Estados das máquinas salvos: " + stateString);
	}
	
	private void loadMachineStates(Context context, List<Machine> machineList) {
		if (machineList == null || machineList.isEmpty()) { return; }
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		String savedStates = prefs.getString(PREF_KEY_MACHINE_STATES, null);
		if (savedStates == null || savedStates.isEmpty()) {
			Log.i(TAG, "Nenhum estado de máquina salvo encontrado.");
			return; // Keep default states (usually ON)
		}
		Log.i(TAG, "Carregando estados das máquinas: " + savedStates);
		String[] statePairs = savedStates.split(",");
		for (String pair : statePairs) {
			String[] parts = pair.split(":");
			if (parts.length == 2) {
				try {
					int number = Integer.parseInt(parts[0]);
					boolean isOn = Boolean.parseBoolean(parts[1]);
					// Find machine by number and update its state
					machineList.stream()
					.filter(m -> m.getNumber() == number)
					.findFirst()
					.ifPresent(m -> m.setOn(isOn));
					} catch (NumberFormatException e) {
					Log.w(TAG, "Erro ao parsear estado da máquina: " + pair);
				}
			}
		}
	}
	*/
	
	// --- Initial Operator Creation and History Methods (No changes needed here) ---
	
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
	
	// History saving now reflects the state AFTER distribution, including inactive machines
	private void saveDistributionHistory(Context context, List<Machine> machines, List<Operator> operatorsUsed) {
		StringBuilder historyEntry = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		String timestamp = sdf.format(new Date());
		
		historyEntry.append("--- Histórico de Distribuição [").append(timestamp).append("] ---\n");
		historyEntry.append("Ordem dos Operadores Utilizada: ").append(getOperatorNames(operatorsUsed)).append("\n");
		historyEntry.append("Máquinas Distribuídas (Considerando Estado Ligado/Desligado):\n");
		
		if (machines == null || machines.isEmpty()) {
			historyEntry.append("  (Nenhuma máquina na lista)\n");
			} else {
			long activeCount = machines.stream().filter(Machine::isOn).count();
			historyEntry.append("  (Máquinas Ativas para Distribuição: ").append(activeCount).append(")\n");
			for (Machine machine : machines) {
				historyEntry.append("  - Máquina: ").append(machine.getName())
				.append(" (").append(machine.getNumber()).append(")")
				.append(" [").append(machine.isOn() ? "LIGADA" : "DESLIGADA").append("]") // Show state
				.append(" -> Operador: ")
				.append(machine.getOperator() != null && !machine.getOperator().isEmpty() ? machine.getOperator() : "(não atribuído)")
				.append("\n");
			}
		}
		historyEntry.append("--- Fim da Entrada [").append(timestamp).append("] ---\n\n");
		
		// File saving logic remains the same
		try {
			File historyFile = new File(context.getExternalFilesDir(null), HISTORY_FILENAME);
			// Ensure the directory exists (getExternalFilesDir should handle this, but good practice)
			// File parentDir = historyFile.getParentFile();
			// if (parentDir != null && !parentDir.exists()) {
			//     parentDir.mkdirs();
			// }
			FileOutputStream fos = new FileOutputStream(historyFile, true); // Append mode
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
		if (operators == null || operators.isEmpty()) { return "[]"; }
		return "[" + operators.stream().map(Operator::getName).collect(Collectors.joining(", ")) + "]";
	}
}