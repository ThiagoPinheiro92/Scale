package com.th.scala.activities;

import android.os.Bundle;
import android.widget.ExpandableListView;


import com.th.scala.R;
import com.th.scala.adapters.ScheduleExpandableAdapter;
import com.th.scala.database.DatabaseHelper;
import com.th.scala.database.EmployeeDAO;
import com.th.scala.database.MachineDAO;
import com.th.scala.models.Schedule;
import com.th.scala.utils.ScheduleGenerator;

import java.util.List;

public class ScheduleActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        dbHelper = new DatabaseHelper(this);
        
        // Carregar dados
        List<Employee> employees = new EmployeeDAO(dbHelper.getReadableDatabase()).getAllEmployees();
        List<Machine> machines = new MachineDAO(dbHelper.getReadableDatabase()).getAllMachines();
        
        // Gerar escala
        Schedule schedule = ScheduleGenerator.generateWeeklySchedule(employees, machines);
        
        // Configurar a lista expansível
        ExpandableListView expandableListView = findViewById(R.id.scheduleExpandableListView);
        ScheduleExpandableAdapter adapter = new ScheduleExpandableAdapter(this, schedule);
        expandableListView.setAdapter(adapter);
    }
}