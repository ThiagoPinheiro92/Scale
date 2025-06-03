package com.th.scala.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class MainActivity extends Activity {
	
	private static final String HISTORY_FILENAME = "distribution_history.txt";
	private static final String TAG = "MainActivityToggle";
	private static final String PREFS_NAME = "OperatorOrderPrefs";
	private static final String PREF_KEY_OPERATOR_ORDER = "lastOperatorOrder";
	// Optional: Add prefs for machine states if needed across app restarts
	// private static final String PREF_KEY_MACHINE_STATES = "lastMachineStates";
	
	private MachineAdapter adapter;
	private MachineDistributor distributor;
	private MachineFactory factory;
	
	// Lista que será rotacionada
	private List<Operator> rotatedOperatorList;
	// Lista original (não rotacionar esta!)
	private List<Operator> originalOperatorList;
	// Lista de máquinas
	private List<Machine> machineList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		distributor = new MachineDistributor();
		factory = new MachineFactory();
		
		// 1. Cria a lista original e guarda
		originalOperatorList = createInitialOperators();
		
		// 2. Carrega a ordem salva ou usa uma CÓPIA da original para a lista rotacionada
		rotatedOperatorList = loadOperatorOrder(this, originalOperatorList);
		if (rotatedOperatorList == null || rotatedOperatorList.isEmpty()) {
			rotatedOperatorList = new ArrayList<>(originalOperatorList); // Usa cópia da original se não houver salva
			Log.i(TAG, "Nenhuma ordem salva encontrada, usando ordem inicial para rotação.");
			} else {
			Log.i(TAG, "Ordem dos operadores carregada para rotação: " + getOperatorNames(rotatedOperatorList));
		}
		
		// 3. Cria ou carrega a lista de máquinas
		machineList = factory.createDefaultMachines();
		// loadMachineStates(this, machineList); // Descomente se implementar persistência de estado da máquina
		
		// 4. Configura ListView e Adapter
		ListView listView = findViewById(R.id.list_view);
		adapter = new MachineAdapter(this, machineList);
		listView.setAdapter(adapter);
		
		// 5. Configura botão de distribuição
		Button distributeBtn = findViewById(R.id.distribute_button);
		distributeBtn.setOnClickListener(v -> {
			// Rotaciona a lista ROTATED
			if (rotatedOperatorList != null && !rotatedOperatorList.isEmpty()) {
				Collections.rotate(rotatedOperatorList, -1);
				Log.i(TAG, "Ordem rotacionada: " + getOperatorNames(rotatedOperatorList));
			}
			
			// Pega a lista atual de máquinas do adapter (com estados atualizados)
			List<Machine> currentMachines = adapter.getMachines();
			
			// Chama a distribuição passando AMBAS as listas de operadores!
			distributor.distributeMachines(currentMachines, rotatedOperatorList, originalOperatorList);
			
			// Salva a ordem da lista ROTATED
			saveOperatorOrder(MainActivity.this, rotatedOperatorList);
			
			// Salva o histórico
			saveDistributionHistory(MainActivity.this, currentMachines, rotatedOperatorList);
			
			// Atualiza a UI
			adapter.notifyDataSetChanged();
			Toast.makeText(MainActivity.this, "Máquinas distribuídas e histórico salvo!", Toast.LENGTH_SHORT).show();
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
		if (operators == null || operators.isEmpty()) { return; }
		String orderString = operators.stream().map(Operator::getName).collect(Collectors.joining(","));
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREF_KEY_OPERATOR_ORDER, orderString);
		editor.apply();
		Log.i(TAG, "Ordem dos operadores salva: " + orderString);
	}
	
	// Ajustado para receber a lista original como referência
	private List<Operator> loadOperatorOrder(Context context, List<Operator> referenceOriginalList) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		String savedOrder = prefs.getString(PREF_KEY_OPERATOR_ORDER, null);
		if (savedOrder == null || savedOrder.isEmpty()) { return null; }
		
		List<String> operatorNames = Arrays.asList(savedOrder.split(","));
		List<Operator> loadedOperators = new ArrayList<>();
		
		// Use referenceOriginalList para encontrar os operadores pelos nomes salvos
		for (String name : operatorNames) {
			Operator foundOp = referenceOriginalList.stream()
			.filter(op -> op.getName().equals(name))
			.findFirst()
			.orElse(null); // Retorna null se não encontrar
			if (foundOp != null) {
				loadedOperators.add(foundOp);
				} else {
				Log.w(TAG, "Operador salvo \"" + name + "\" não encontrado na lista original de referência.");
				// Se um operador salvo não existe mais na lista original, a ordem salva é inválida
				return null; // Força recriar a partir da original
			}
		}
		
		// Verifica se todos os operadores originais estão presentes na ordem salva
		if (loadedOperators.size() != referenceOriginalList.size()) {
			Log.w(TAG, "Discrepância entre operadores salvos e originais. Usando ordem padrão.");
			return null; // Força o uso da ordem original se a salva estiver inconsistente
		}
		
		return loadedOperators;
	}
	
	// --- Initial Operator Creation ---
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
	
	// --- History Saving ---
private void saveDistributionHistory(Context context, List<Machine> machines, List<Operator> operatorsUsedForRotation) {
    StringBuilder historyEntry = new StringBuilder();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    String timestamp = sdf.format(new Date());
    
    // Cabeçalho
    historyEntry.append("\n--- Histórico de Distribuição [").append(timestamp).append("] ---\n\n");
    historyEntry.append("Ordem dos Operadores (Rotação Atual): ").append(getOperatorNames(operatorsUsedForRotation)).append("\n\n");
    historyEntry.append("Máquinas Distribuídas (Considerando Estado Ligado/Desligado):\n");
    
    if (machines == null || machines.isEmpty()) {
        historyEntry.append("  (Nenhuma máquina na lista)\n");
    } else {
        long activeCount = machines.stream().filter(Machine::isOn).count();
        historyEntry.append("  (Máquinas Ativas para Distribuição: ").append(activeCount).append(")\n\n");
        
        for (Machine machine : machines) {
            historyEntry.append("-").append(machine.getName())
                .append(" (").append(machine.getNumber()).append(")")
                .append(" [").append(machine.isOn() ? "🟢 LIG" : "🔴 DESL}").append("]")
                .append(" -> Operador: ")
                .append(machine.getOperator() != null && !machine.getOperator().isEmpty() ? machine.getOperator() : "(não atribuído)")
                .append("\n");
        }
    }
    
    // Rodapé
    historyEntry.append("\n--- Fim da Entrada [").append(timestamp).append("] ---\n");
    
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

// Helper para obter nomes dos operadores
private String getOperatorNames(List<Operator> operators) {
    if (operators == null || operators.isEmpty()) { return "[]"; }
    return "[" + operators.stream().map(Operator::getName).collect(Collectors.joining(", ")) + "]";
}
}