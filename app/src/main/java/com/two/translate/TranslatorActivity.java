package com.two.translate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TranslatorActivity extends AppCompatActivity {
    private EditText sourceText;
    private TextView englishTranslationView;
    private TextView turkishTranslationView;

    private Button englishLanguageButton;
    private Button turkishLanguageButton;

    private String englishLanguage = "EN";
    private String turkishLanguage = "TR";

    private Map<String, TranslationResult> translations = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_editor);
        // setTitle("");

        // Initialize views
        sourceText = findViewById(R.id.source_text);
        englishTranslationView = findViewById(R.id.english_translation);
        turkishTranslationView = findViewById(R.id.turkish_translation);

        englishLanguageButton = findViewById(R.id.english_language_button);
        turkishLanguageButton = findViewById(R.id.turkish_language_button);

        englishLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageSelectionDialog(true);
            }
        });

        turkishLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageSelectionDialog(false);
            }
        });

        sourceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                translateText(s.toString());
            }
        });

        // Set window properties
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = WindowManager.LayoutParams.MATCH_PARENT;
            int height = displayMetrics.heightPixels * 3 / 5;
            window.setLayout(width, height);
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.TOP;
            window.setAttributes(layoutParams);

            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            layoutParams.dimAmount = 0.3f;
        }

        String selectedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (selectedText != null && !selectedText.isEmpty()) {
            sourceText.setText(selectedText);
        } else {
            Toast.makeText(this, "No text to translate.", Toast.LENGTH_SHORT).show();
        }

        updateLanguageButtons();
    }

    public void onTranslateButtonClick(View view) {
    // Updated PayPal donation link with 1 Euro as the amount
    String paypalUrl = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=halilkocoglu.de@gmail.com&currency_code=EUR&amount=1.00";
    
    // Create an intent to open a browser with the PayPal donation link
    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paypalUrl));
    startActivity(browserIntent);
}



    private void showLanguageSelectionDialog(final boolean isEnglish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Language");

        final String[] languages = Language.getAllLanguages().toArray(new String[0]);

        builder.setItems(languages, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedLanguage = languages[which];
                if (isEnglish) {
                    englishLanguage = getLanguageCodeForLanguage(selectedLanguage);
                } else {
                    turkishLanguage = getLanguageCodeForLanguage(selectedLanguage);
                }
                translateText(sourceText.getText().toString());
                updateLanguageButtons();
            }
        });

        builder.show();
    }

    private void translateText(String text) {
        translateToLanguage(text, englishLanguage, englishTranslationView);
        translateToLanguage(text, turkishLanguage, turkishTranslationView);
    }

    private void translateToLanguage(String text, String targetLang, TextView targetTextView) {
        // Ensure the targetLang is already a two-letter language code
        if (targetLang != null && !targetLang.isEmpty()) {
            String baseUrl = "https://translate.googleapis.com/translate_a/single";
            HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                    .addQueryParameter("client", "gtx")
                    .addQueryParameter("sl", "auto")
                    .addQueryParameter("tl", targetLang.toLowerCase())
                    .addQueryParameter("dt", "t")
                    .addQueryParameter("q", text)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    synchronized (translations) {
                        translations.put(targetLang, new TranslationResult("", true, e.getMessage()));
                    }
                    updateTargetTextView(translations, targetLang, targetTextView);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    StringBuilder resultTextBuilder = new StringBuilder();
                    boolean isError = false;
                    String errorMessage = "";

                    try {
                        String jsonData = response.body().string();
                        JSONArray jsonArray = new JSONArray(jsonData);
                        JSONArray translatedTextArray = jsonArray.getJSONArray(0);

                        for (int i = 0; i < translatedTextArray.length(); i++) {
                            JSONArray translatedTextInfo = translatedTextArray.getJSONArray(i);
                            resultTextBuilder.append(translatedTextInfo.getString(0));
                        }
                    } catch (JSONException e) {
                        isError = true;
                        errorMessage = "Failed to parse translation: " + e.getMessage();
                    }

                    String resultText = resultTextBuilder.toString();

                    synchronized (translations) {
                        translations.put(targetLang, new TranslationResult(resultText, isError, errorMessage));
                    }
                    updateTargetTextView(translations, targetLang, targetTextView);
                }
            });

        } else {
            targetTextView.setText("Unsupported language");
        }
    }

    private void updateTargetTextView(Map<String, TranslationResult> translations, String targetLang, TextView targetTextView) {
        runOnUiThread(() -> {
            TranslationResult result = translations.get(targetLang);
            if (result != null) {
                if (result.isError) {
                    targetTextView.setText(result.errorMessage);
                } else {
                    targetTextView.setText(result.translatedText);
                }
            } else {
                targetTextView.setText("No translation available");
            }
        });
    }

    private String getLanguageCodeForLanguage(String language) {
        switch (language.toLowerCase()) {
        
        case "english":
            return "en";
        case "turkish":
            return "tr";
	    case "german":
            return "de";
        case "french":
            return "fr";
        case "spanish":
            return "es";
        case "afrikaans":
            return "af";
        case "albanian":
            return "sq";
        case "arabic":
            return "ar";
        case "armenian":
            return "hy";
        case "azerbaijani":
            return "az";
        case "basque":
            return "eu";
        case "belarusian":
            return "be";
        case "bengali":
            return "bn";
        case "bulgarian":
            return "bg";
        case "catalan":
            return "ca";
        case "chinese":
            return "zh";
        case "croatian":
            return "hr";
        case "czech":
            return "cs";
        case "danish":
            return "da";
        case "dutch":
            return "nl";
        case "estonian":
            return "et";
        case "filipino":
            return "tl";
        case "finnish":
            return "fi";
        case "galician":
            return "gl";
        case "georgian":
            return "ka";
        case "greek":
            return "el";
        case "gujarati":
            return "gu";
        case "haitian_creole":
            return "ht";
        case "hebrew":
            return "iw";
        case "hindi":
            return "hi";
        case "hungarian":
            return "hu";
        case "icelandic":
            return "is";
        case "indonesian":
            return "id";
        case "irish":
            return "ga";
        case "italian":
            return "it";
        case "japanese":
            return "ja";
        case "kannada":
            return "kn";
        case "korean":
            return "ko";
        case "latin":
            return "la";
        case "latvian":
            return "lv";
        case "lithuanian":
            return "lt";
        case "macedonian":
            return "mk";
        case "malay":
            return "ms";
        case "maltese":
            return "mt";
        case "norwegian":
            return "no";
        case "persian":
            return "fa";
        case "polish":
            return "pl";
        case "portuguese":
            return "pt";
        case "romanian":
            return "ro";
        case "russian":
            return "ru";
        case "serbian":
            return "sr";
        case "slovak":
            return "sk";
        case "slovenian":
            return "sl";
        case "swahili":
            return "sw";
        case "swedish":
            return "sv";
        case "tamil":
            return "ta";
        case "telugu":
            return "te";
        case "thai":
            return "th";
        case "ukrainian":
            return "uk";
        case "urdu":
            return "ur";
        case "vietnamese":
            return "vi";
        case "welsh":
            return "cy";
        case "yiddish":
            return "yi";
        case "chinese_simplified":
            return "zh";
        case "chinese_traditional":
            return "zh";
       
        default:
                return null;
        }

    }

    private void updateLanguageButtons() {
        englishLanguageButton.setText(englishLanguage.toUpperCase());
        turkishLanguageButton.setText(turkishLanguage.toUpperCase());
    }

    private static class TranslationResult {
        String translatedText;
        boolean isError;
        String errorMessage;

        TranslationResult(String translatedText, boolean isError, String errorMessage) {
            this.translatedText = translatedText;
            this.isError = isError;
            this.errorMessage = errorMessage;
        }
    }
}
