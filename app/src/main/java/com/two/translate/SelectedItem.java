package com.two.translate;


import android.app.Activity; 
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.View;


public class SelectedItem {

    public static void setupSpinners(Activity activity, Spinner englishSpinner, Spinner turkishSpinner, TextView englishTranslationView, TextView turkishTranslationView, EditText sourceTextView) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, Language.getAllLanguages());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Setzen Sie den Adapter für beide Spinner
        englishSpinner.setAdapter(adapter);
        turkishSpinner.setAdapter(adapter);

        // Setzen Sie einen Listener, um auf Änderungen der Auswahl zu reagieren
        englishSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Holen Sie sich die ausgewählte Sprache
                String selectedLanguage = (String) parent.getItemAtPosition(position);
                // Holen Sie sich den zu übersetzenden Text
                String textToTranslate = sourceTextView.getText().toString();
                // Führen Sie die Übersetzung durch (zum Beispiel durch Aufrufen einer Methode)
                translateText(textToTranslate, selectedLanguage, englishTranslationView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nichts zu tun
            }
        });

        turkishSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Holen Sie sich die ausgewählte Sprache
                String selectedLanguage = (String) parent.getItemAtPosition(position);
                // Holen Sie sich den zu übersetzenden Text
                String textToTranslate = sourceTextView.getText().toString();
                // Führen Sie die Übersetzung durch (zum Beispiel durch Aufrufen einer Methode)
                translateText(textToTranslate, selectedLanguage, turkishTranslationView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nichts zu tun
            }
        });
    }

    private static void translateText(String textToTranslate, String targetLanguage, TextView translationView) {
        // Hier müssen Sie Ihre Übersetzungslogik implementieren
        // Beispielsweise könnten Sie eine API wie Google Translate verwenden
        String translatedText = "Übersetzung von '" + textToTranslate + "' in '" + targetLanguage + "'";
        translationView.setText(translatedText);
    }
}
