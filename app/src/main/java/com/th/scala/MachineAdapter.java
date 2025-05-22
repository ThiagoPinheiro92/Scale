package com.th.scala;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.List;
import android.widget.AdapterView;

public class MachineAdapter extends BaseAdapter {
	private Context context;
	private List<Machine> machineList;
	
	public MachineAdapter(Context context, List<Machine> machineList) {
		this.context = context;
		this.machineList = machineList;
	}
	
	@Override
	public int getCount() {
		return machineList.size();
	}
	
	@Override
	public Object getItem(int position) {
		return machineList.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context)
			.inflate(R.layout.item_machine, parent, false);
		}
		
		Machine machine = machineList.get(position);
		
		// Vincule os componentes do layout
		TextView machineName = convertView.findViewById(R.id.machine_name);
		EditText productName = convertView.findViewById(R.id.product_name);
		Spinner difficultySpinner = convertView.findViewById(R.id.difficulty_spinner);
		EditText operatorName = convertView.findViewById(R.id.operator_name);
		
		// Configure os valores iniciais
		machineName.setText(machine.getName() + " (" + machine.getNumber() + ")");
		productName.setText(machine.getProduct());
		operatorName.setText(machine.getOperator());
		
		// Configure o Spinner de dificuldade
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
		context,
		R.array.difficulty_levels,
		android.R.layout.simple_spinner_item
		);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		difficultySpinner.setAdapter(adapter);
		difficultySpinner.setSelection(machine.getDifficulty() - 1);
		
		// Listeners para atualização
		productName.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus) {
				machine.setProduct(productName.getText().toString());
			}
		});
		
		difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				machine.setDifficulty(pos + 1);
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		
		operatorName.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus) {
				machine.setOperator(operatorName.getText().toString());
			}
		});
		
		return convertView;
	}
}