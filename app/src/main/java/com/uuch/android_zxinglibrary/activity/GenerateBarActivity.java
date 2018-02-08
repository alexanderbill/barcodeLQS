package com.uuch.android_zxinglibrary.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.uuch.android_zxinglibrary.R;
import com.uuch.android_zxinglibrary.utils.Aes;
import com.uuch.android_zxinglibrary.utils.PinyinHelper;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class GenerateBarActivity extends BaseActivity {

    private final static String TAG = GenerateBarActivity.class.getName();
    private View all;
    private TextView name;
    private TextView sex;
    private TextView department;
    private ImageView barcode;
    private ImageView avator;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gen);
        all = findViewById(R.id.all);
        all.setDrawingCacheEnabled(true);
        name = (TextView) findViewById(R.id.name);
        name.setTextColor(Color.parseColor(Config.getInstance().color));
        sex = (TextView) findViewById(R.id.sex);
        sex.setTextColor(Color.parseColor(Config.getInstance().color));
        department = (TextView) findViewById(R.id.department);
        department.setTextColor(Color.parseColor(Config.getInstance().color));
        barcode = (ImageView) findViewById(R.id.barcode);
        TextView ID_title = (TextView) findViewById(R.id.ID_title);
        ID_title.setText(Config.getInstance().ID_title);
        TextView footprint = (TextView) findViewById(R.id.footprint);
        footprint.setText(Config.getInstance().footprint);
        TextView v = (TextView) findViewById(R.id.version);
        v.setText(Config.getInstance().version);
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("generateSingle") == true) {
            generateBitmapForSingle();
        } else {
            showFileChooser("选择文件生成二维码");
            all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    generateBitmapForSingle();
                }
            });
        }

        avator = (ImageView) findViewById(R.id.avator);
        setViewWidthByHeight(avator);

        try {
            file = new File("/sdcard/namecard/generate.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {

        }
    }


    public static void setViewWidthByHeight(View view) {
        final View mv = view;
        final ViewTreeObserver vto = mv.getViewTreeObserver();
        final ViewTreeObserver.OnPreDrawListener preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {

				int width = mv.getMeasuredWidth();

                android.view.ViewGroup.LayoutParams lp = mv.getLayoutParams();
                lp.height = width * 7 / 5;
                mv.setLayoutParams(lp);
                return true;
            }
        };
        vto.addOnPreDrawListener(preDrawListener);
    }


    public void generateBitmapForSingle() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();

        View view = View.inflate(this, R.layout.input_single, null);
        // dialog.setView(view);// 将自定义的布局文件设置给dialog
        dialog.setView(view, 0, 0, 0, 0);// 设置边距为0,保证在2.x的版本上运行没问题

        final EditText name = (EditText) view
                .findViewById(R.id.name);
        final EditText phone = (EditText) view
                .findViewById(R.id.phone);
        final EditText department = (EditText) view
                .findViewById(R.id.department);

        Button btnOK = (Button) view.findViewById(R.id.btn_ok);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        final RadioGroup group = (RadioGroup)view.findViewById(R.id.radioGroup);

        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String sname = name.getText().toString().trim();
                String sphone = phone.getText().toString().trim();
                String sdepartment = department.getText().toString().trim();
                sdepartment = TextUtils.isEmpty(sdepartment) ? "无" : sdepartment.trim();
                int radioButtonId = group.getCheckedRadioButtonId();
                boolean male = true;
                if (radioButtonId == R.id.male) {
                    male = true;
                } else {
                    male = false;
                }
                if (!TextUtils.isEmpty(sname) && !TextUtils.isEmpty(sphone)) {
                    generateBitmap(new User(sdepartment + " " + sname + " " + (male? "男":"女") + " " + sphone));
                    dialog.dismiss();
                } else {

                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();// 隐藏dialog
            }
        });

        dialog.show();
    }

    @Override
    void postReadExcel() {
        if (!TextUtils.isEmpty(xlsData)) {
            String strings[] = xlsData.split("\n");
            for (String p : strings) {
                generateBitmap(new User(p));
            }
            return;
        }
    }

    private String barcodeString = "";
    @Override
    public void onBackPressed() {
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("generateSingle") == true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AlertDialog dialog = builder.setPositiveButton(com.uuzuche.lib_zxing.R.string.signin, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface view, int a) {
                    Intent intent = new Intent();
                    intent.putExtra("barcodeString", barcodeString);
                    setResult(RESULT_OK, intent);
                    view.dismiss();
                    finish();
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    finish();
                }
            }).setMessage("签到么？").create();

            dialog.show();
            return;
        }
        super.onBackPressed();
    }

    @Override
    void postReadExcelFail() {
        generateBitmapForSingle();
    }



    // param：部组+姓名+性别+电话+简拼(可选)
    // A17710275730zl
    private String generateBitmap(User user) {
        try {
            name.setText(user.mName);
            sex.setText(user.mSex);
            department.setText(user.mDepart);
            barcodeString = user.mKey + user.mDesp;
            byte[] bytes= Aes.encrypt(barcodeString.getBytes(), "38297b2faee515715a13b708aef17758");
            Log.d("leizhou", barcodeString);
            String contentE = new String(Base64.encode(bytes, Base64.DEFAULT));

            Bitmap b = CodeUtils.createImage(contentE, 400, 400, null);

            barcode.setImageBitmap(b);

            saveBitmap(user.mSex + "-" + user.mDepart + "-" + user.mName + ".png", barcodeString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap getImage(File f) {
        return BitmapFactory.decodeFile(f.getPath());
    }

    public void saveBitmap(String picName, String sgen) {
        Log.e(TAG, "保存图片");
        File f = new File("/sdcard/namecard/", picName);
        if (f.exists()) {
            f.delete();
        }
        try {
            Bitmap bimage = null;
            File image = new File("/sdcard/namecard-P/", picName.replace(".png", "-P.png"));
            if (!image.exists()) {
                image = new File("/sdcard/namecard-P/", picName.replace(".png", "-P.jpg"));
                if (image.exists()) {
                    bimage = getImage(image);
                }
            } else {
                bimage = getImage(image);
            }
            if (bimage != null) {
                ((ImageView)findViewById(R.id.avator)).setImageBitmap(bimage);
            }

            FileOutputStream out = new FileOutputStream(f);
            all.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");

            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            sgen += "\n";
            raf.write(sgen.getBytes());
            raf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
