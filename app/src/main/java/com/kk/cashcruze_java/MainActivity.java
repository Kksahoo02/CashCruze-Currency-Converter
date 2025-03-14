package com.kk.cashcruze_java;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText etAmount;
    private Spinner spinnerFrom, spinnerTo;
    private Button btnConvert, btnSwapCurrencies;
    private TextView tvResult;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_DARK_MODE = "darkMode";


    // Replace with your actual API key
    private static final String API_KEY = "bcfc153da8c7c5a9f394feeb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        etAmount = findViewById(R.id.etAmount);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        btnConvert = findViewById(R.id.btnConvert);
        tvResult = findViewById(R.id.tvResult);
        btnSwapCurrencies = findViewById(R.id.btnSwapCurrencies); // Add this line


        // Setup currency spinners
        setupCurrencySpinners();

        // Setup convert button
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performConversion();
            }
        });

        // Setup swap button
        btnSwapCurrencies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapCurrencies();
            }
        });
    }

    private void setupCurrencySpinners() {
        // Create adapters for spinners
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.currencies,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        // Set default selections
        spinnerFrom.setSelection(0); // First currency
        spinnerTo.setSelection(1);   // Second currency
    }

    private void performConversion() {
        // Get amount from input
        String amountStr = etAmount.getText().toString();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            // Get selected currencies
            String fromCurrency = getSelectedCurrencyCode(spinnerFrom);
            String toCurrency = getSelectedCurrencyCode(spinnerTo);

            // Make API request
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        double result = fetchExchangeRate(fromCurrency, toCurrency);
                        displayResult(amount, fromCurrency, toCurrency, result);
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).start();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    private String getSelectedCurrencyCode(Spinner spinner) {
        return getResources().getStringArray(R.array.currency_codes)[spinner.getSelectedItemPosition()];
    }

    private double fetchExchangeRate(String fromCurrency, String toCurrency) throws Exception {
        // Construct API URL
        String urlStr = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/pair/" + fromCurrency + "/" + toCurrency;

        // Make HTTP request
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        // Read response
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder json = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            json.append(line);
        }
        reader.close();

        // Parse JSON response
        JsonParser parser = new JsonParser();
        JsonObject jsonResponse = parser.parse(json.toString()).getAsJsonObject();

        if (jsonResponse.get("result").getAsString().equals("success")) {
            return jsonResponse.get("conversion_rate").getAsDouble();
        } else {
            throw new Exception("API request failed: " + jsonResponse.get("error-type").getAsString());
        }
    }

    private void displayResult(final double amount, final String fromCurrency, final String toCurrency, final double rate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                double convertedAmount = amount * rate;
                tvResult.setText(String.format("%.2f %s = %.2f %s", amount, fromCurrency, convertedAmount, toCurrency));
            }
        });
    }

    // Add this method to swap currencies
    private void swapCurrencies() {
        int fromPosition = spinnerFrom.getSelectedItemPosition();
        int toPosition = spinnerTo.getSelectedItemPosition();

        spinnerFrom.setSelection(toPosition);
        spinnerTo.setSelection(fromPosition);
    }
}