package com.example.yummyrestaurant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.yummyrestaurant.activities.DashboardActivity;
import com.example.yummyrestaurant.activities.LoginActivity;
import com.example.yummyrestaurant.activities.ProductListActivity;
import com.example.yummyrestaurant.utils.RoleManager;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查并请求 POST_NOTIFICATIONS 权限（适用于 Android 9.0 及以上版本）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            } else {
                // 权限已授予，继续处理用户登录逻辑
                handleUserLogin();
            }
        } else {
            // 对于 Android 9.0 以下版本，直接处理用户登录逻辑
            handleUserLogin();
        }
    }

    private void handleUserLogin() {
        // 直接从 RoleManager 取得用户信息（假设已在登录时保存）
        String userRole = RoleManager.getUserRole();

        if (userRole != null) {
            if ("staff".equals(userRole)) {
                startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, ProductListActivity.class));
            }
            finish(); // 关闭 MainActivity 以便于重定向
        } else {
            // 尚未登录，导向登录页
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // 关闭 MainActivity 以便于重定向
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予，继续处理用户登录逻辑
                handleUserLogin();
            } else {
                // 权限未授予，显示消息
                Toast.makeText(this, "通知权限被拒绝", Toast.LENGTH_SHORT).show();
                // 继续处理用户登录逻辑即使权限被拒绝
                handleUserLogin();
            }
        }
    }
}
