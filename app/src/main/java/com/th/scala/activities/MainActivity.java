package com.th.scala.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.th.scala.R;
import com.th.scala.database.DatabaseHelper;

public class MainActivity extends AppCompatActivity {
	
	private DatabaseHelper dbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Inicializa o banco de dados
		dbHelper = new DatabaseHelper(this);
		
		// Configura os botões/cards do menu principal
		setupMenuCards();
	}
	
	private void setupMenuCards() {
		CardView cardMachines = findViewById(R.id.card_machines);
		CardView cardEmployees = findViewById(R.id.card_employees);
		CardView cardSchedule = findViewById(R.id.card_schedule);
		CardView cardSettings = findViewById(R.id.card_settings);
		
		// Navegação para gerenciamento de máquinas
		cardMachines.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, MachineManagementActivity.class);
			startActivity(intent);
		});
		
		// Navegação para gerenciamento de funcionários
		cardEmployees.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, EmployeeManagementActivity.class);
			startActivity(intent);
		});
		
		// Navegação para geração de escalas
		cardSchedule.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);
			startActivity(intent);
		});
		
		// Navegação para configurações (opcional)
		cardSettings.setOnClickListener(v -> {
			// Intent para tela de configurações (se necessário)
			// startActivity(new Intent(MainActivity.this, SettingsActivity.class));
		});
	}
	
	@Override
	protected void onDestroy() {
		dbHelper.close();
		super.onDestroy();
	}
}