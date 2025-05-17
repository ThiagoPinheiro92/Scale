package com.th.scala.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.th.scala.R;
import com.th.scala.database.DatabaseHelper;
import com.th.scala.database.MachineDAO;
import com.th.scala.models.Machine;

import java.util.List;

public class MachineManagementActivity extends AppCompatActivity {
	private DatabaseHelper dbHelper;
	private MachineDAO machineDAO;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_machine_management);
		
		dbHelper = new DatabaseHelper(this);
		machineDAO = new MachineDAO(dbHelper.getWritableDatabase());
		
		loadMachines();
	}
	
	private void loadMachines() {
		List<Machine> machines = machineDAO.getAllMachines();
		ListView listView = findViewById(R.id.machinesListView);
		
		ArrayAdapter<Machine> adapter = new ArrayAdapter<>(
		this,
		android.R.layout.simple_list_item_1,
		machines
		);
		
		listView.setAdapter(adapter);
	}
}