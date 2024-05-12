package com.example.languagetranslationapplication;

import static android.content.ContentValues.TAG;


import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener,TextToSpeech.OnInitListener {

    private Spinner fromLanguageSpinner;
    private Spinner toLanguageSpinner;
    private TextView translatedTextView;
    FirebaseTranslator LangTranslator;
    private Button translateLanguageBtn,VoiceBtn,Speaker;
    private EditText Textbox;
    private static final int SPEECH_REQUEST_CODE = 0;
    private int fromLangCode=-1, toLangCode =-1;
    private static HashMap<String, Integer> languageCodeMap = new HashMap<>();
    TextToSpeech tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromLanguageSpinner = findViewById(R.id.fromLanguageSpinner);
        toLanguageSpinner = findViewById(R.id.toLanguageSpinner);
        translatedTextView= findViewById(R.id.translated_text_view);
        translateLanguageBtn= findViewById(R.id.translateButton);
        Textbox = findViewById(R.id.textInput);
        VoiceBtn = findViewById(R.id.Voice);
        Speaker= findViewById(R.id.Speaker);

        // Language Code Mapper
        languageCodeMap.put("Select Language",-1);
        languageCodeMap.put("Arabic",FirebaseTranslateLanguage.AR);
        languageCodeMap.put("Bengali",FirebaseTranslateLanguage.BN);
        languageCodeMap.put("German",FirebaseTranslateLanguage.DE);
        languageCodeMap.put("English",FirebaseTranslateLanguage.EN);
        languageCodeMap.put("Spanish",FirebaseTranslateLanguage.ES);
        languageCodeMap.put("Persian",FirebaseTranslateLanguage.FA);
        languageCodeMap.put("French",FirebaseTranslateLanguage.FR);
        languageCodeMap.put("Gujarati",FirebaseTranslateLanguage.GU);
        languageCodeMap.put("Hindi",FirebaseTranslateLanguage.HI);
        languageCodeMap.put("Indonesian",FirebaseTranslateLanguage.ID);
        languageCodeMap.put("Italian",FirebaseTranslateLanguage.IT);
        languageCodeMap.put("Japanese",FirebaseTranslateLanguage.JA);
        languageCodeMap.put("Kannada",FirebaseTranslateLanguage.KN);
        languageCodeMap.put("Korean",FirebaseTranslateLanguage.KO);
        languageCodeMap.put("Marathi",FirebaseTranslateLanguage.MR);
        languageCodeMap.put("Dutch",FirebaseTranslateLanguage.NL);
        languageCodeMap.put("Polish",FirebaseTranslateLanguage.PL);
        languageCodeMap.put("Portuguese",FirebaseTranslateLanguage.PT);
        languageCodeMap.put("Russian",FirebaseTranslateLanguage.RU);
        languageCodeMap.put("Tamil",FirebaseTranslateLanguage.TA);
        languageCodeMap.put("Telugu",FirebaseTranslateLanguage.TE);
        languageCodeMap.put("Urdu",FirebaseTranslateLanguage.UR);
        languageCodeMap.put("Chinese",FirebaseTranslateLanguage.ZH);



        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.languages_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromLanguageSpinner.setAdapter(adapter);
        toLanguageSpinner.setAdapter(adapter);



        translateLanguageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fromLangCode == -1){
                    Toast.makeText(MainActivity.this, "Please Choose the language from which you want to translate", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(toLangCode ==-1){
                    Toast.makeText(MainActivity.this, "Please Choose the language to which you want to translate text", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(fromLangCode != -1 && toLangCode != -1){
                    FirebaseTranslatorOptions options =
                            new FirebaseTranslatorOptions.Builder()
                                    .setSourceLanguage(fromLangCode)
                                    .setTargetLanguage(toLangCode)
                                    .build();
                    LangTranslator =
                            FirebaseNaturalLanguage.getInstance().getTranslator(options);

                    String string = Textbox.getText().toString();
                    downloadModal(string);
                }
            }
        });

        VoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySpeechRecognizer();
            }
        });


        fromLanguageSpinner.setOnItemSelectedListener(this);
        toLanguageSpinner.setOnItemSelectedListener(this);

        tts = new TextToSpeech(this, this);

        Speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeak = translatedTextView.getText().toString();
                if (tts != null && !toSpeak.isEmpty()) {
                    speakText(toSpeak);
                }
                else{
                    Toast.makeText(MainActivity.this, "The translated text is empty or the text to speech engine is not initialised", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void downloadModal(String input) {
        // below line is use to download the modal which
        // we will require to translate in german language
        Toast.makeText(MainActivity.this, "Please wait language modal is being downloaded.", Toast.LENGTH_SHORT).show();

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().requireWifi().build();

        // below line is use to download our modal.
        LangTranslator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                // this method is called when modal is downloaded successfully.
                Toast.makeText(MainActivity.this, "Your Modal is downloaded. Translation is in progress...........", Toast.LENGTH_SHORT).show();

                // calling method to translate our entered text.
                translateLanguage(input);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Fail to download modal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void translateLanguage(String input) {
        LangTranslator.translate(input).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                translatedTextView.setText(s);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Fail to translate", Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            Textbox.setText(spokenText);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// This starts the activity and populates the intent with the speech text.
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        String selectedLanguage = (String) adapterView.getSelectedItem();
        if (adapterView == fromLanguageSpinner) {
            fromLangCode = languageCodeMap.get(selectedLanguage);
        } else if (adapterView == toLanguageSpinner) {
            toLangCode= languageCodeMap.get(selectedLanguage);
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
        } else {
            // Handle failure, like displaying an error message to the user
            Toast.makeText(this, "Text-to-Speech engine failed to initialize", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void speakText(String text) {
        String language= (String)toLanguageSpinner.getSelectedItem();
        if(languageCodeMap.get(language)==-1) return;
        String langCode="";
        switch(language){
            case "Arabic":
                langCode="AR";
                break;
            case "Bengali":
                langCode="BN";
                break;
            case "German":
                langCode="DE";
                break;
            case "English":
                langCode="EN";
                break;
            case "Spanish":
                langCode="ES";
                break;
            case "Persian":
                langCode="FA";
                break;
            case "French":
                langCode="FR";
                break;
            case "Gujarati":
                langCode="GU";
                break;
            case "Hindi":
                langCode="HI";
                break;
            case "Indonesian":
                langCode="ID";
                break;
            case "Italian":
                langCode="IT";
                break;
            case "Japanese":
                langCode="JA";
                break;
            case "Kannada":
                langCode="KN";
                break;
            case "Korean":
                langCode="KO";
                break;
            case "Marathi":
                langCode="MR";
                break;
            case "Dutch":
                langCode="NL";
                break;
            case "Polish":
                langCode="PL";
                break;
            case "Portuguese":
                langCode="PT";
                break;
            case "Russian":
                langCode="RU";
                break;
            case "Tamil":
                langCode="TA";
                break;
            case "Telugu":
                langCode="TE";
                break;
            case "Urdu":
                langCode="UR";
                break;
            case "Chinese":
                langCode="ZH";
                break;
            default: break;
        }
        Locale desiredLocale = new Locale(langCode); // Get system's default locale
        int result = tts.setLanguage(desiredLocale);

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Language not available, inform user and offer alternative (optional)
            Toast.makeText(this, "Selected language not supported. Using device default", Toast.LENGTH_SHORT).show();
            result = tts.setLanguage(Locale.getDefault()); // Fallback to default locale
        }

        if (result == TextToSpeech.SUCCESS) {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null);
        } else {
            // Handle general TTS engine failure
            Toast.makeText(this, "Text-to-Speech failed", Toast.LENGTH_SHORT).show();
        }
    }

}