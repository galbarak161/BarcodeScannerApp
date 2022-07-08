package com.example.barcodescanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    // Initialize variable
    Button btScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assign variable
        btScan = findViewById(R.id.bt_scan);
        btScan.setOnClickListener(view -> {
            // Initialize intent integrator
            IntentIntegrator intentIntegrator = new IntentIntegrator(
                    MainActivity.this
            );

            // Set prompt text and beep
            intentIntegrator.setPrompt("For flash use volume up key");
            intentIntegrator.setBeepEnabled(true);

            // Locked orientation
            intentIntegrator.setOrientationLocked(true);

            // Set capture activity and initialize scan
            intentIntegrator.setCaptureActivity(Capture.class);
            intentIntegrator.initiateScan();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Initialize intent results
        IntentResult result = IntentIntegrator.parseActivityResult(
                requestCode, resultCode, data
        );

        if (result.getContents() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    MainActivity.this
            );
            builder.setTitle("Results");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                // Dismiss dialog
                dialogInterface.dismiss();
            });

            builder.show();
        } else {
            Toast.makeText(getApplicationContext(), "Oops.. Try again", Toast.LENGTH_SHORT).show();
        }
    }
}