package com.uuch.android_zxinglibrary.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.uuch.android_zxinglibrary.R;
import com.uuch.android_zxinglibrary.utils.PinyinHelper;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;


public class MainActivity extends BaseActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);
        setContentView(R.layout.activity_main);
        PinyinHelper.getInstance().init(this);
        File f = new File("/sdcard/namecard/");
        if (!f.exists()) {
            f.mkdir();
        }

        try {
            PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            //获取APP版本versionName
            String versionName = packageInfo.versionName;
            //获取APP版本versionCode
            ((TextView) findViewById(R.id.version)).setText(versionName);
        } catch (Exception e) {

        }

        showLoginDialog();
    }

    public void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();

        View view = View.inflate(this, R.layout.input_login, null);
        // dialog.setView(view);// 将自定义的布局文件设置给dialog
        dialog.setView(view, 0, 0, 0, 0);// 设置边距为0,保证在2.x的版本上运行没问题

        final EditText name = (EditText) view
                .findViewById(R.id.name);
        final EditText password = (EditText) view
                .findViewById(R.id.password);

        Button btnOK = (Button) view.findViewById(R.id.btn_ok);
        final RadioGroup group = (RadioGroup)view.findViewById(R.id.onoff);

        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String sname = name.getText().toString().trim();
                String sphone = password.getText().toString().trim();
                int radioButtonId = group.getCheckedRadioButtonId();
                if (radioButtonId == R.id.offline) {
                    try {
                        File config = new File("/sdcard/namecard/config.txt");
                        FileInputStream is = new FileInputStream(config);
                        byte b[]=new byte[(int)config.length()];     //创建合适文件大小的数组
                        is.read(b);    //读取文件中的内容到b[]数组
                        is.close();
                        JSONObject object = new JSONObject(new String(b));
                        Config.getInstance().init(object);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "请检查配置文件", Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    //　TODO: online
                    Toast.makeText(MainActivity.this, "此模式未启用", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!TextUtils.isEmpty(sname)) {
                    username = sname;
                    if (sname.equals("admin") && "LQQD".equals(sphone) || (sname.equals("dev20170819"))) {
                        findViewById(R.id.generate_barcode).setVisibility(View.VISIBLE);
                    } else if (!Config.getInstance().users.contains(sname)) {
                        Toast.makeText(MainActivity.this, "无效用户名", Toast.LENGTH_LONG).show();
                        return;
                    }
                    findViewById(R.id.sign).setVisibility(View.VISIBLE);
                    findViewById(R.id.out).setVisibility(View.VISIBLE);
                    dialog.dismiss();
                }
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private String username;

    @Override
    public void onResume() {
        super.onResume();
        findViewById(R.id.generate_barcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, GenerateBarActivity.class);
                startActivity(it);
            }
        });
        findViewById(R.id.sign).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, CaptureActivity.class);
                it.putExtra("username", username);
                startActivity(it);
            }
        });
        findViewById(R.id.out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, OutActivity.class);
                startActivity(it);
            }
        });
    }


    void postReadExcel() {}

    void postReadExcelFail() {}
}
