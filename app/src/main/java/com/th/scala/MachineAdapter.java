package com.th.scala;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import java.util.List;

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
		ViewHolder holder;
		
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_machine, parent, false);
			holder = new ViewHolder();
			holder.machineInfo = convertView.findViewById(R.id.machine_info);
			holder.machinePosition = convertView.findViewById(R.id.machine_position);
			holder.difficultySpinner = convertView.findViewById(R.id.difficulty_spinner);
			convertView.setTag(holder);
			} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Machine machine = machineList.get(position);
		updateMachineInfo(holder, machine);
		setupDifficultySpinner(holder, machine, position);
		
		return convertView;
	}
	
	private void updateMachineInfo(ViewHolder holder, Machine machine) {
		String info = String.format("%s (%d)\nProd: %s\nDif: %s\nOper: %s",
		machine.getName(),
		machine.getNumber(),
		machine.getProduct(),
		getDifficultyLetter(machine.getDifficulty()),
		machine.getOperator());
		
		holder.machineInfo.setText(info);
		holder.machinePosition.setText(String.valueOf(machine.getPosition()));
	}
	
	private void setupDifficultySpinner(ViewHolder holder, Machine machine, int position) {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
		context,
		R.array.difficulty_levels,
		android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		holder.difficultySpinner.setAdapter(adapter);
		holder.difficultySpinner.setSelection(machine.getDifficulty() - 1);
		
		holder.difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				machine.setDifficulty(pos + 1);
				updateMachineInfo(holder, machine);
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}
	
	private String getDifficultyLetter(int difficulty) {
		switch (difficulty) {
			case 3: return "D (Difícil)";
			case 2: return "M (Médio)";
			case 1: return "F (Fácil)";
			default: return "?";
		}
	}
	
	private static class ViewHolder {
		TextView machineInfo;
		TextView machinePosition;
		Spinner difficultySpinner;
	}
}