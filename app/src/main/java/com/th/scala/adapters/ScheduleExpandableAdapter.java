package com.th.scala.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.th.scala.R;
import com.th.scala.models.Employee;
import com.th.scala.models.Machine;
import com.th.scala.models.Schedule;

import java.util.List;
import java.util.Map;

public class ScheduleExpandableAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private final Schedule schedule;
    private final List<Employee> employees;
    private final Map<Employee, List<Machine>> assignments;

    public ScheduleExpandableAdapter(Context context, Schedule schedule) {
        this.context = context;
        this.schedule = schedule;
        this.employees = schedule.getEmployees();
        this.assignments = schedule.getAssignments();
    }

    @Override
    public int getGroupCount() {
        return employees.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return assignments.get(employees.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return employees.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return assignments.get(employees.get(groupPosition)).get(childPosition);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group_employee, null);
        }
        
        Employee employee = (Employee) getGroup(groupPosition);
        TextView employeeName = convertView.findViewById(R.id.employeeName);
        employeeName.setText(employee.getName());
        
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_machine, null);
        }
        
        Machine machine = (Machine) getChild(groupPosition, childPosition);
        TextView machineName = convertView.findViewById(R.id.machineName);
        TextView machineDifficulty = convertView.findViewById(R.id.machineDifficulty);
        
        machineName.setText(machine.getName());
        machineDifficulty.setText("Dificuldade: " + machine.getEffectiveDifficulty());
        
        return convertView;
    }

    // Implementar outros métodos necessários...
}