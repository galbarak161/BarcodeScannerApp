package com.example.barcodescanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener {

    // Initialize variable
    Button btImport, btScan, btCopy;
    ArrayList<String> listItems = new ArrayList<>();
    RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitializeRecycleView();

        // Assign buttons
        btScan = findViewById(R.id.bt_scan);
        btScan.setOnClickListener(view -> ScannerButtonClickEvent());

        btCopy = findViewById(R.id.bt_save);
        btCopy.setEnabled(false);
        btCopy.setOnClickListener(view -> SaveButtonClickEvent());

        btImport = findViewById(R.id.bt_import);
        btImport.setOnClickListener(view -> ImportButtonClickEvent());

    }

    private void ImportButtonClickEvent() {
        // Create an alert for the user
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.this
        );

        builder.setTitle("Import Excel file");
        builder.setMessage("Shibo... give me more time :)");
        builder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());

        builder.show();
    }

    private void InitializeRecycleView() {
        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.display_barcodes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter(this, listItems);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void SaveButtonClickEvent() {
        boolean isExcelGenerated = ExcelUtils.exportDataIntoWorkbook(getApplication(),
                Constants.EXCEL_FILE_NAME, listItems);

        // Create an alert for the user
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.this
        );

        String msg = isExcelGenerated ? String.format(Locale.getDefault(), "Created Excel file with %d elements", listItems.size()) : "Failed to generate file";

        builder.setTitle("Export to excel");
        builder.setMessage(msg);
        builder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());

        builder.show();
    }

    private void ScannerButtonClickEvent() {
        //debug
        /*listItems.add("" + listItems.size() + "");
        adapter.notifyItemInserted(listItems.size() -1 );

        // after the first scan, update the view
        if(listItems.size() == 1){
            findViewById(R.id.display_barcodes_recycler_view).setVisibility(View.VISIBLE);
            btCopy.setEnabled(true);
            btScan.setText(R.string.button_secondClick);
        }*/

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

        String scanResults = result.getContents();
        if (IsScanValidate(scanResults)) {
            listItems.add(0, result.getContents());
            adapter.notifyItemInserted(0);

            // after the first scan, update the view
            if (listItems.size() == 1) {
                findViewById(R.id.display_barcodes_recycler_view).setVisibility(View.VISIBLE);
                btCopy.setEnabled(true);
                btScan.setText(R.string.button_secondClick);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Oops.. Try again", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean IsScanValidate(String scanResults) {
        return scanResults != null &&
                scanResults.matches("[0-9]+") &&
                scanResults.length() == 7 &&
                scanResults.charAt(0) == '3';
    }

    @Override
    public void onItemClick(View view, int position) {
        // Create an alert for the user
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.this
        );

        builder.setTitle("Delete selected barcode");
        builder.setMessage("Are you sure that you want to delete this item?");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            listItems.remove(position);
            adapter.notifyItemRemoved(position);
            dialogInterface.dismiss();
            Toast.makeText(this, "Removed " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();
    }
}