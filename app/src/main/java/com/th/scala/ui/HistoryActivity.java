package com.th.scala.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout defined in activity_history.xml
        setContentView(R.layout.activity_history);

        // Initialize the TextView
        historyTextView = findViewById(R.id.history_text_view);

        // Load and display the history
        loadHistory();

        // Optional: Add a title to the action bar for this activity
        if (getActionBar() != null) {
            getActionBar().setTitle("Histórico de Distribuições");
            // Enable the Up button (optional, needs parent activity declared in Manifest)
            // getActionBar().setDisplayHomeAsUpEnabled(true);
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
            }

        } catch (IOException e) {
            Log.e(TAG, "Erro ao ler arquivo de histórico", e);
            historyTextView.setText("Erro ao carregar histórico: " + e.getMessage());
            Toast.makeText(this, "Erro ao carregar histórico", Toast.LENGTH_LONG).show();
        }
    }

    /* Optional: Handle Up button press if enabled
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to the parent activity (MainActivity)
            finish(); // or NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */
}