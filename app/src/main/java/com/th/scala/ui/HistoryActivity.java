package com.th.scala.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.th.scala.R; // Assuming R class is in this package

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class HistoryActivity extends Activity {
	
	private static final String HISTORY_FILENAME = "distribution_history.txt";
	private static final String TAG = "HistoryActivity";
	
	private TextView historyTextView;
	private ScrollView scrollView; // Add reference to the ScrollView
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set the layout defined in activity_history.xml
		setContentView(R.layout.activity_history);
		
		// Initialize the TextView and ScrollView
		historyTextView = findViewById(R.id.history_text_view);
		// Assuming the ScrollView in activity_history.xml has the ID 'scrollView'
		// If not, add android:id="@+id/scrollView" to your ScrollView in the XML
		// For now, let's assume the root element IS the ScrollView
		View rootView = findViewById(android.R.id.content).getRootView();
		if (rootView instanceof ScrollView) {
			scrollView = (ScrollView) rootView;
			} else {
			// If the ScrollView is nested, find it by its ID
			// Example: scrollView = findViewById(R.id.scrollView);
			// Make sure to add an ID to your ScrollView in activity_history.xml
			// For this example, we'll try finding it assuming it's the direct parent of the TextView
			if (historyTextView.getParent() instanceof ScrollView) {
				scrollView = (ScrollView) historyTextView.getParent();
				} else {
				Log.w(TAG, "ScrollView not found or not the direct parent. Auto-scroll might not work.");
			}
		}
		
		
		// Load and display the history
		loadHistory();
		
		// Optional: Add a title to the action bar for this activity
		if (getActionBar() != null) {
			getActionBar().setTitle("Histórico de Distribuições");
		}
	}
	
	private void loadHistory() {
		StringBuilder historyContent = new StringBuilder();
		File historyFile = new File(getExternalFilesDir(null), HISTORY_FILENAME);
		
		if (!historyFile.exists()) {
			historyTextView.setText("Nenhum histórico encontrado.");
			return;
		}
		
		try {
			FileInputStream fis = new FileInputStream(historyFile);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader bufferedReader = new BufferedReader(isr);
			
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				historyContent.append(line).append("\n");
			}
			
			bufferedReader.close();
			isr.close();
			fis.close();
			
			if (historyContent.length() == 0) {
				historyTextView.setText("Histórico vazio.");
				} else {
				historyTextView.setText(historyContent.toString());
				// Scroll to bottom AFTER setting the text
				scrollToBottom();
			}
			
			} catch (IOException e) {
			Log.e(TAG, "Erro ao ler arquivo de histórico", e);
			historyTextView.setText("Erro ao carregar histórico: " + e.getMessage());
			Toast.makeText(this, "Erro ao carregar histórico", Toast.LENGTH_LONG).show();
		}
	}
	
	private void scrollToBottom() {
		if (scrollView != null) {
			// Use post to ensure the layout is complete before scrolling
			scrollView.post(new Runnable() {
				@Override
				public void run() {
					scrollView.fullScroll(View.FOCUS_DOWN);
				}
			});
		}
	}
	
	/* Optional: Handle Up button press if enabled
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	*/
}