package com.yiwise.asr.demo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;

public final class PermissionUtil {
    public static boolean checkPermission(Context context) {
        return checkPermission(context, new String[]{
                RECORD_AUDIO,
                INTERNET,
        });
    }

    public static boolean checkPermission(Context context, String[] permissions) {
        ArrayList<String> permissionList = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(p);
            }
        }
        if (permissionList.isEmpty()) {
            return true;
        }
        String[] requestPermissions = new String[permissionList.size()];
        permissionList.toArray(requestPermissions);
        ActivityCompat.requestPermissions((Activity) context, requestPermissions, 0);
        return false;
    }
}
