package com.yiwise.asr.demo.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yiwise.asr.demo.R;
import com.yiwise.asr.demo.utils.PermissionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends AppCompatActivity {
    private static Logger logger = LoggerFactory.getLogger(MainActivity.class);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean permission = PermissionUtil.checkPermission(this);
        logger.debug("permission check " + (permission ? "success" : "failure"));
    }

    /**
     * 复查权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "请授予必要的权限", Toast.LENGTH_LONG).show();
                    break;
                }
            }
        }
    }

    public void onFileRealTime(View view) {
        Intent intent = new Intent(this, FileRealTimeActivity.class);
        startActivity(intent);
    }

    public void onRecordRealTime(View view) {
        Intent intent = new Intent(this, RecordRealTimeActivity.class);
        startActivity(intent);
    }
}
