package com.th.scala.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Configura o Fragment de Preferências
		getSupportFragmentManager()
		.beginTransaction()
		.replace(android.R.id.content, new SettingsFragment())
		.commit();
	}
	
	public static class SettingsFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			// Crie um arquivo XML em res/xml/settings_preferences.xml primeiro!
			setPreferencesFromResource(R.xml.settings_preferences, rootKey);
		}
	}
}