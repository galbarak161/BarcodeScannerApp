package com.example.barcodescanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

public class AndroidStoragePermission {
    final int RequestReadWriteExternalStorage = 2230;
    final int RequestForManageAllFiles = 2231;

    private final Activity activity;
    private final TaskCompletionSource<Boolean> requestPermissionResult = new TaskCompletionSource<>();

    public AndroidStoragePermission(Activity context) {
        this.activity = context;
    }

    public boolean hasStoragePermission() {
        boolean hasPermissions;

        int SDK = Build.VERSION.SDK_INT;

        if (SDK > Build.VERSION_CODES.Q) {
            // since sdk 30; stricter permissions requires special 'manage storage permission'
            // requires to go to system settings
            hasPermissions = Environment.isExternalStorageManager();
        } else if (SDK > Build.VERSION_CODES.M) {
            // since sdk 23-29 we request write/read external storage only
            hasPermissions =
                    (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED));
        } else {
            // sdk bellow 23 no permissions needed
            hasPermissions = true;
        }

        return hasPermissions;
    }

    public Task<Boolean> requestStoragePermission() {
        int SDK = Build.VERSION.SDK_INT;

        if (SDK <= Build.VERSION_CODES.M) {
            TaskCompletionSource<Boolean> task = new TaskCompletionSource<>();
            task.setResult(true);
            return task.getTask();
        } else if (SDK <= Build.VERSION_CODES.Q) {
            // handled by callback 'OnRequestPermissionsResult'
            activity.requestPermissions(new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    RequestReadWriteExternalStorage);

            return requestPermissionResult.getTask();
        } else {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(android.net.Uri.fromParts("package", activity.getPackageName(), null));

                // navigates to settings, when user dismisses them calls OnActivityResult with our constant
                activity.startActivityForResult(intent, RequestForManageAllFiles);

            } catch (Exception e) {
                // this bad! (probably outdated 'permission model' as android likes to change them every once in a while)
                TaskCompletionSource<Boolean> task = new TaskCompletionSource<>();
                task.setResult(false);
                return task.getTask();
            }

            return requestPermissionResult.getTask();
        }
    }

    /// <summary> Call this in activity OnActivityResult override </summary>
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestForManageAllFiles) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                if (Environment.isExternalStorageManager()) {
                    requestPermissionResult.trySetResult(true);
                } else {
                    requestPermissionResult.trySetResult(false);
                }
            }
        }
    }

    // <summary> Call this in activity OnRequestPermissionsResult override </summary>
    public void OnRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // make sure we are handling our request
        if (requestCode == RequestReadWriteExternalStorage) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    requestPermissionResult.trySetResult(false);
                    return;
                }
            }
            requestPermissionResult.trySetResult(true);
        }
    }
}
