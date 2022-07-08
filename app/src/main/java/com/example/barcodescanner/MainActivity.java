package com.example.barcodescanner;

import android.app.ListActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends ListActivity {

    // Initialize variable
    Button btScan, btCopy;
    ArrayList<String> listItems = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assign variable
        btScan = findViewById(R.id.bt_scan);
        btScan.setOnClickListener(view -> ScannerButtonClickEvent());

        btCopy = findViewById(R.id.bt_copyList);
        btCopy.setEnabled(false);
        btCopy.setOnClickListener(view -> CopyButtonClickEvent());

        // Link adapter to ListView
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        setListAdapter(adapter);
    }

    private void CopyButtonClickEvent() {
        String result = String.join(" , ", listItems);

        // Save the list content to clipboard
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", result);
        clipboard.setPrimaryClip(clip);

        // Create alert for the user
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.this
        );
        builder.setTitle("Copy to clipboard");
        builder.setMessage(String.format(Locale.getDefault(), "Copied %d elements to the clipboard", listItems.size()));
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            // Dismiss dialog
            dialogInterface.dismiss();
        });

        builder.show();
    }

    private void ScannerButtonClickEvent() {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Initialize intent results
        IntentResult result = IntentIntegrator.parseActivityResult(
                requestCode, resultCode, data
        );

        if (result.getContents() != null) {
            adapter.add(result.getContents());
            btCopy.setEnabled(true);

            // Update the button content
            btScan.setText(R.string.button_secondClick);

        } else {
            Toast.makeText(getApplicationContext(), "Oops.. Try again", Toast.LENGTH_SHORT).show();
        }
    }
}