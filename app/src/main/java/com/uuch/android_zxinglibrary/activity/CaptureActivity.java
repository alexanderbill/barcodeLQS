package com.uuch.android_zxinglibrary.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.ListPopupWindow;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.uuch.android_zxinglibrary.R;
import com.uuch.android_zxinglibrary.utils.Aes;
import com.uuch.android_zxinglibrary.utils.PinyinHelper;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Initial the camera
 * <p>
 * 默认的二维码扫描Activity
 */
public class CaptureActivity extends BaseActivity {

    CaptureFragment captureFragment;
    File file;
    File fileYigong;
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        Intent intent = getIntent();
        try {
            mUsername = intent.getExtras().getString("username");
        } catch (Exception e) {

        }
        captureFragment = new CaptureFragment();
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_zxing_container, captureFragment).commit();
        captureFragment.setCameraInitCallBack(new CaptureFragment.CameraInitCallBack() {
            @Override
            public void callBack(Exception e) {
                if (e == null) {

                } else {
                    Log.e("TAG", "callBack: ", e);
                }
            }
        });
        showAlertDialog();

        findViewById(R.id.generate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CaptureActivity.this, GenerateBarActivity.class);
                intent.putExtra("generateSingle", true);
                startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2:
                // ResultActivity的返回数据
                if (resultCode == RESULT_OK) {
                    //  Get the Uri of the selected file
                    String barcodeString = data.getStringExtra("barcodeString");
                    doRecord(barcodeString);
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private int male = 0;
    private int female = 0;

    private void showAlertDialog() {
        final SharedPreferences sharedPreferences = getSharedPreferences("name", MODE_PRIVATE);
        final String actionid = sharedPreferences.getString("name", "default");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();

        View view = View.inflate(this, com.uuch.android_zxinglibrary.R.layout.input_action, null);
        // dialog.setView(view);// 将自定义的布局文件设置给dialog
        dialog.setView(view, 0, 0, 0, 0);// 设置边距为0,保证在2.x的版本上运行没问题

        final EditText name = (EditText) view
                .findViewById(com.uuch.android_zxinglibrary.R.id.actionId);
        name.setText(actionid);
        final CheckBox checkBox = (CheckBox) view.findViewById(com.uuch.android_zxinglibrary.R.id.useNumber);
        checkBox.setChecked(Config.getInstance().useNumber);

        final CheckBox checkBox1 = (CheckBox) view.findViewById(R.id.includeYG);
        checkBox1.setChecked(Config.getInstance().includeYG);

        Button btnOK = (Button) view.findViewById(com.uuch.android_zxinglibrary.R.id.btn_ok);
        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String sname = name.getText().toString();
                if (!TextUtils.isEmpty(sname)) {
                    ((TextView) findViewById(R.id.checkTitle)).setText(sname + " 签到中");
                    try {
                        String append = mUsername;
                        if (append.equals("dev20170819")) {
                            append = "dev";
                        }
                        file = new File("/sdcard/namecard/" + sname + "-" + append + ".txt");
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        fileYigong = new File("/sdcard/namecard/" + sname + "-" + append + "-义工.txt");
                        if (!fileYigong.exists()) {
                            fileYigong.createNewFile();
                        }
                        sharedPreferences.edit().putString("name", sname).commit();
                        readPreRecord(file);
                        readPreRecord(fileYigong);
                        ((TextView) findViewById(R.id.checkinResultMan)).setText(String.valueOf(male));
                        ((TextView) findViewById(R.id.checkinResultWoman)).setText(String.valueOf(female));
                    } catch (Exception e) {

                    }

                    if (!actionid.equals(sname)) {
                        showFileChooser("选择预签到表");
                    } else {
                        final String path = sharedPreferences.getString("path", "");
                        if (!TextUtils.isEmpty(path)) {
                            xlsData = doReadPath(path.split(" ")[1]);
                            doDeal();
                        } else {
                            showFileChooser("选择预签到表");
                        }
                    }

                    Config.getInstance().useNumber = checkBox.isChecked();
                    Config.getInstance().includeYG = checkBox1.isChecked();

                    dialog.dismiss();
                } else {

                }
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void readPreRecord(File file) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String readline;
            while ((readline = br.readLine()) != null) {
                String[] s = readline.split("\t");
                vSignedId.add(s[0]);
                if (readline.charAt(0) == 'A')
                    male++;
                else
                    female++;
            }
            if (br != null) {
                br.close();
            }
        } catch (Exception e) {

        }
    }

    private ListPopupWindow listPopupWindow;
    private UserAdapter userAdapter;
    private void initSearch(String[] strings) {

        EditText et = (EditText)findViewById(R.id.search);
        listPopupWindow = new ListPopupWindow(CaptureActivity.this);
        userAdapter = new UserAdapter(this, R.layout.list_item);
        for (String p: strings) {
            String[] s = p.split(" ");
            // String name, String sex, String phone, String department, int checked, String nick, int rowId
            userAdapter.add(new User(s[1], PinyinHelper.getInstance().getFirstPinyins(s[1]), s[0]));
        }

        listPopupWindow.setAdapter(userAdapter);
        listPopupWindow.setAnchorView(et);

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    listPopupWindow.dismiss();
                } else {
                    listPopupWindow.show();
                    userAdapter.search(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void showAlertDialog(String text, final String key, final PreSign show, final String result, final int status) {
        if (Config.getInstance().useNumber || Config.getInstance().includeYG) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final AlertDialog dialog = builder.create();

            View view = View.inflate(this, R.layout.input_number, null);
            // dialog.setView(view);// 将自定义的布局文件设置给dialog
            dialog.setView(view, 0, 0, 0, 0);// 设置边距为0,保证在2.x的版本上运行没问题

            final TextView title = (TextView) view
                    .findViewById(R.id.title);
            if (!TextUtils.isEmpty(text)) {
                title.setText(text);
            }
            final EditText phone = (EditText) view
                    .findViewById(R.id.input_number);

            if (Config.getInstance().useNumber) {
                phone.setVisibility(View.VISIBLE);
            }

            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.yigong);
            if (Config.getInstance().includeYG) {
                checkBox.setVisibility(View.VISIBLE);
            }

            Button btnOK = (Button) view.findViewById(R.id.btn_ok);
            Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);

            btnOK.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String sphone = phone.getText().toString().trim();
                    if (!TextUtils.isEmpty(sphone)) {
                        forceDoRecord(key, show, result, status, sphone, checkBox.isChecked());
                        continueCap();
                        dialog.dismiss();
                    } else {

                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    forceDoRecord(key, show, result, status, "NULL", checkBox.isChecked());
                    continueCap();
                    dialog.dismiss();// 隐藏dialog
                }
            });

            dialog.show();
            return;
        }

        if (TextUtils.isEmpty(text)) {
            forceDoRecord(key, show, result, status);
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setPositiveButton(R.string.force_signin, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface view, int a) {
                view.dismiss();
                forceDoRecord(key, show, result, status);
                continueCap();
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface view, int i) {
                view.dismiss();
                continueCap();
            }
        }).setMessage(text).create();

        dialog.show();

    }

    private Vector<String> vSignedId = new Vector<>();

    private Vector<String> vId = new Vector<>();
    private Map<String, PreSign> vName = new HashMap<>();
    @Override
    void postReadExcel() {
        if (!TextUtils.isEmpty(xlsData)) {
            String strings[] = xlsData.split("\n");
            for (String p : strings) {
                String s[] = p.split(" ");
                if (s[0].startsWith("A") || s[0].startsWith("B")) {
                    vId.add(s[0]);
                    vName.put(s[0], new PreSign(Arrays.copyOfRange(s, 1, s.length)));
                }
            }
            initSearch(strings);
            return;
        }
    }

    class PreSign {
        final String name;
        String sex = "";
        String className = "";
        String classNick = "";
        PreSign(String[] s) {
            name = s[0];
            try {
                sex = s[1];
                className = s[3];
                classNick = s[2];
            } catch (Exception e) {

            }
        }

        @Override
        public String toString() {
            return name + " " + sex + " " + classNick + " " + className;
        }
    }

    void postReadExcelFail() {

    }

    private void continueCap() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (captureFragment != null && captureFragment.handler != null) {
                    captureFragment.handler.restartPreviewAndDecode();
                }
            }
        }, 2000);
    }
    private String raw = "";

    private void doRecord(String result) {
        try {
            // Toast.makeText(CaptureActivity.this, result, Toast.LENGTH_LONG).show();
            String[] s = result.split(" ");
            if (!Config.getInstance().seal.contains("ENTIRE") &&
                    (s.length >= 2 && !Config.getInstance().seal.contains(s[1]) ||
                            s.length ==1 && !Config.getInstance().seal.contains("NULL"))) {
                Toast.makeText(this, "此二维码不是本部组的", Toast.LENGTH_LONG).show();
                result = "";
                throw new Exception("");
            }
            PreSign toShow = vName.get(s[0]);
            if (vSignedId.contains(s[0])) {
                if (toShow == null) {
                    String pres[] = {s[0], "", "", ""};
                    toShow = new PreSign(pres);
                }
                showAlertDialog(toShow.name + "已经签到，请勿重复签到", s[0], toShow, result, 2);
                return;
            }
            if (vId.size() > 0 && !vId.contains(s[0])) {
                showAlertDialog(s[0] + "不在名单中", s[0], toShow, result, 1);
                return;
            }
            if (!raw.equals(result)) {
                showAlertDialog("", s[0], toShow, result, 0);
            }
        } catch (Exception e) {
            Log.d("err", e.toString());
        } finally {
            if (!TextUtils.isEmpty(result)) {
                raw = result;
            }
        }
        continueCap();
    }

    private void forceDoRecord(String key, PreSign toShow, String result, int status) {
        forceDoRecord(key, toShow, result, status, "", false);
    }

    private void forceDoRecord(String key, PreSign toShow, String result, int status, String number, boolean yigong) {
        try {
            if (status != 2) {
                vSignedId.add(key);
                if (key.startsWith("A")) {
                    male++;
                } else {
                    female++;
                }
            }

            ((TextView) findViewById(R.id.checkinResultMan)).setText(String.valueOf(male));
            ((TextView) findViewById(R.id.checkinResultWoman)).setText(String.valueOf(female));
            if (toShow != null) {
                String details = toShow.classNick + " " + toShow.className + " " + toShow.name;
                ((TextView) findViewById(R.id.checkTitle)).setText(details + " 签到中");
            }
            String toRecord = record(file, key, number, status, "", null);
            if (yigong) {
                record(fileYigong, key, number, status, result, toShow);
            }
            if (!TextUtils.isEmpty(toRecord)) {
                Log.d("leizhou", toRecord);
            }
        } catch (Exception e) {

        }
    }

    private String record(File file, String key, String number, int status, String result, PreSign to) {
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            String toRecord = key;
            long time = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd\tHH:mm:ss");
            Date d1 = new Date(time);
            String t1 = format.format(d1);
            toRecord += "\t" + t1;
            if (!TextUtils.isEmpty(number)) {
                toRecord += "\t" + number;
            }
            if (to != null) {
                toRecord += "\t" + to.toString();
            }
            if (mUsername.equals("dev20170819") || status != 0) {
                toRecord += "\t" + result;
                if (status == 1) {
                    toRecord += "\t" + "未报名";
                } else {
                    toRecord += "\t" + "重复签到";
                }
            }
            toRecord += "\n";
            raf.write(toRecord.getBytes());
            raf.close();
            return toRecord;
        } catch (Exception e) {

        }
        return "";
    }
    /**
     * 二维码解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            /*Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
            bundle.putString(CodeUtils.RESULT_STRING, result);
            resultIntent.putExtras(bundle);
            CaptureActivity.this.setResult(RESULT_OK, resultIntent);
            CaptureActivity.this.finish();*/
            try {
                byte[] bytes = Base64.decode(result, Base64.DEFAULT);
                result = new String(Aes.decrypt(bytes, "38297b2faee515715a13b708aef17758"));
                doRecord(result);
            } catch (Exception e) {

            } finally {
                continueCap();
            }
        }

        @Override
        public void onAnalyzeFailed() {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
            bundle.putString(CodeUtils.RESULT_STRING, "");
            resultIntent.putExtras(bundle);
            CaptureActivity.this.setResult(RESULT_OK, resultIntent);
            CaptureActivity.this.finish();
        }
    };

    class UserAdapter extends ArrayAdapter<User> {
        private int mResourceId;

        private ArrayList<User> sorted;

        private ArrayList<User> searched;

        public UserAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.mResourceId = textViewResourceId;
            sorted = new ArrayList<User>();
            searched = new ArrayList<User>();
        }

        @Override
        public void add(User user) {
            sorted.add(user);
        }

        public void sort() {
            Collections.sort(sorted, new SortByAge());
            searched.addAll(sorted);
        }

        public void search(String key) {
            if (TextUtils.isEmpty(key)) {
                searched.clear();
                searched.addAll(sorted);
            } else {
                searched.clear();
                for (int i = 0; i < sorted.size(); i++) {
                    if (sorted.get(i).mNick.startsWith(key)) {
                        searched.add(sorted.get(i));
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public User getItem(int position) {
            return searched.get(position);
        }

        @Override
        public int getCount() {
            return searched.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final User user = getItem(position);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(mResourceId, null);
            TextView nameText = (TextView) view.findViewById(R.id.name);
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbok);

            nameText.setText(user.mName);
            checkBox.setChecked(false);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    doRecord(user.mRowId + " " + Config.getInstance().seal.get(0));
                    EditText et = (EditText)findViewById(R.id.search);
                    et.setText("");
                }
            });

            return view;
        }
    }

    class User {
        public final String mName;
        public final String mNick;
        public final String mRowId;

        public User(String name, String nick, String rowId) {

            this.mName = name;
            mNick = nick;
            mRowId = rowId;
        }

    }

    class SortByAge implements Comparator {
        public int compare(Object o1, Object o2) {
            User s1 = (User) o1;
            User s2 = (User) o2;
            return s1.mNick.compareTo(s2.mNick);
        }
    }
}