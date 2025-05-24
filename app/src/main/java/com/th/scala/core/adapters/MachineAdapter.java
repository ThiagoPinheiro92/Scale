package com.th.scala.core.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.th.scala.core.models.Machine;
import java.util.List;
import com.th.scala.R;
import android.widget.ArrayAdapter;

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
		bindMachineData(holder, machine, position);
		setupDifficultySpinner(holder.difficultySpinner, machine, position);
		
		return convertView;
	}
	
	private void bindMachineData(ViewHolder holder, Machine machine, int position) {
		String info = String.format("%s (%d)\nProduto: %s\nOperador: %s",
		machine.getName(),
		machine.getNumber(),
		machine.getProduct(),
		machine.getOperator().isEmpty() ? "Não atribuído" : machine.getOperator());
		
		holder.machineInfo.setText(info);
		holder.machinePosition.setText(String.valueOf(position + 1));
	}
	
	private void setupDifficultySpinner(Spinner spinner, Machine machine, int position) {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
		context,
		R.array.difficulty_levels,
		android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		// Define a seleção atual baseada na dificuldade da máquina
		spinner.setSelection(machine.getDifficulty() > 0 ? machine.getDifficulty() - 1 : 0);
		
		// Listener para salvar a dificuldade quando alterada
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// +1 porque os itens do Spinner começam em 0 (Fácil=1, Médio=2, Difícil=3)
				machine.setDifficulty(pos + 1);
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Não faz nada
			}
		});
	}
	
	public List<Machine> getMachines() {
		return machineList;
	}
	
	private static class ViewHolder {
		TextView machineInfo;
		TextView machinePosition;
		Spinner difficultySpinner;
	}
}