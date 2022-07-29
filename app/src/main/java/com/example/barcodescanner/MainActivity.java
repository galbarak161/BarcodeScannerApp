package com.example.barcodescanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    Button btImport, btScan, btExport;
    ArrayList<String> listItems = new ArrayList<>();
    RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeRecycleView();

        // Assign buttons
        btScan = findViewById(R.id.bt_scan);
        btScan.setOnClickListener(view -> ScannerButtonClickEvent());

        btExport = findViewById(R.id.bt_save);
        btExport.setEnabled(false);
        btExport.setOnClickListener(view -> exportButtonClickEvent());

        btImport = findViewById(R.id.bt_import);
        btImport.setOnClickListener(view -> importButtonClickEvent());

    }

    private void initializeRecycleView() {
        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.display_barcodes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter(this, listItems);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void importButtonClickEvent() {
        String title = "Import Excel file";
        String message = "Shibo... give me more time :)";

        AlertDialogBuilder(title, message, (dialogInterface, i) -> dialogInterface.dismiss(), false);
    }

    private void exportButtonClickEvent() {
        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose file name");
        builder.setView(input);
        builder.setPositiveButton("Save", (dialogInterface, i) ->
                saveListToFile(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        builder.show();
    }

    private void saveListToFile(String fileName) {
        boolean isExcelGenerated = ExcelUtils.exportDataIntoWorkbook(getApplication(),
                fileName, listItems);

        String message = isExcelGenerated ? String.format(Locale.getDefault(), "Created Excel file with %d elements", listItems.size()) : "Failed to generate file";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void ScannerButtonClickEvent() {
        //debug
        /*listItems.add("" + listItems.size() + "");
        adapter.notifyItemInserted(listItems.size() - 1);

        // after the first scan, update the view
        if (listItems.size() == 1) {
            findViewById(R.id.display_barcodes_recycler_view).setVisibility(View.VISIBLE);
            btExport.setEnabled(true);
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
            if (listItems.contains(scanResults)) {
                String title = "Scanner Results";
                String message = "This barcode is already exists in list";
                AlertDialogBuilder(title, message, (dialogInterface, i) -> dialogInterface.dismiss(), false);

                return;
            }

            listItems.add(0, scanResults);
            adapter.notifyItemInserted(0);

            // after the first scan, update the view
            if (listItems.size() == 1) {
                findViewById(R.id.display_barcodes_recycler_view).setVisibility(View.VISIBLE);
                btExport.setEnabled(true);
                btScan.setText(R.string.button_secondClick);
            }
        } else {
            String title = "Scanner Results";
            String message = "Oops.. Try again";
            AlertDialogBuilder(title, message, (dialogInterface, i) -> dialogInterface.dismiss(), false);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        String title = "Delete selected barcode";
        String message = "Are you sure that you want to delete the item: " + adapter.getItem(position) + "?";
        AlertDialogBuilder(title, message, (dialogInterface, i) -> {
            listItems.remove(position);
            adapter.notifyItemRemoved(position);
            dialogInterface.dismiss();
            Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show();
        }, true);
    }

    private boolean IsScanValidate(String scanResults) {
        return true;/*scanResults != null &&
                scanResults.matches("[0-9]+") &&
                scanResults.length() == 7 &&
                scanResults.charAt(0) == '3';*/
    }

    private void AlertDialogBuilder(String title, String message, final DialogInterface.OnClickListener positiveClickEvent, boolean showNegativeButton) {
        // Create an alert for the user
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.this
        );

        builder.setTitle(title);
        builder.setMessage(message);

        if (showNegativeButton) {
            builder.setPositiveButton("Yes", positiveClickEvent);
            builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        } else {
            builder.setPositiveButton("OK", positiveClickEvent);
        }

        builder.show();
    }
}