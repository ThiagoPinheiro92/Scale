package com.th.scala.core.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

// Removed androidx imports

import com.th.scala.R; // Assuming R class is in this package
import com.th.scala.core.models.Machine; // Use the updated Machine class

import java.util.List;

// Adapter modified to include the toggle button and handle machine state (without androidx annotations)
public class MachineAdapter extends ArrayAdapter<Machine> {
	
	private Context context;
	private List<Machine> machines;
	
	// Removed @NonNull annotations
	public MachineAdapter(Context context, List<Machine> machines) {
		super(context, 0, machines); // Use 0 for resource ID as we inflate manually
		this.context = context;
		this.machines = machines;
	}
	
	// Removed @NonNull and @Nullable annotations
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View listItemView = convertView;
		if (listItemView == null) {
			// Inflate the new layout with the toggle button
			listItemView = LayoutInflater.from(context).inflate(R.layout.list_item_machine_with_toggle, parent, false);
		}
		
		// Get the current machine
		Machine currentMachine = machines.get(position);
		
		// Find views in the layout
		TextView nameNumberTextView = listItemView.findViewById(R.id.machine_name_number);
		TextView operatorTextView = listItemView.findViewById(R.id.machine_operator);
		TextView detailsTextView = listItemView.findViewById(R.id.machine_details);
		Button toggleButton = listItemView.findViewById(R.id.toggle_button);
		
		// Populate the views
		nameNumberTextView.setText(currentMachine.getName() + " (" + currentMachine.getNumber() + ")");
		operatorTextView.setText("Operador: " + (currentMachine.getOperator() != null && !currentMachine.getOperator().isEmpty() ? currentMachine.getOperator() : "(não atribuído)"));
		detailsTextView.setText("Produto: " + currentMachine.getProduct() + " | Dif: " + currentMachine.getDifficulty());
		
		// Set button color based on machine state
		updateButtonColor(toggleButton, currentMachine.isOn());
		
		// Set click listener for the toggle button
		toggleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Toggle the machine state
				currentMachine.setOn(!currentMachine.isOn());
				
				// Update the button color immediately
				updateButtonColor(toggleButton, currentMachine.isOn());
				
				// Optional: Notify that data might have changed if other parts depend on isOn
				// notifyDataSetChanged(); // This might be too broad, consider item-specific update if needed
				
				// Clear operator if machine is turned off
				if (!currentMachine.isOn()) {
					currentMachine.setOperator(""); // Clear operator assignment
					// Update the operator TextView immediately
					operatorTextView.setText("Operador: (não atribuído)");
				}
			}
		});
		
		return listItemView;
	}
	
	// Helper method to set button background color
	private void updateButtonColor(Button button, boolean isOn) {
		if (isOn) {
			// Use green color
			button.setBackgroundColor(Color.parseColor("#4CAF50")); // Standard Green
			} else {
			// Use red color
			button.setBackgroundColor(Color.parseColor("#F44336")); // Standard Red
		}
	}
	
	// Method to get the current list of machines (including their states)
	public List<Machine> getMachines() {
		return this.machines;
	}
}