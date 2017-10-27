package com.uuch.android_zxinglibrary.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.uuch.android_zxinglibrary.utils.FileUtils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.text.DecimalFormat;

/**
 * Created by ubuntu on 17-9-7.
 */

public abstract class BaseActivity extends FragmentActivity {


    private final int FILE_SELECT_CODE = 1;
    protected String xlsData;

    protected void showFileChooser(String hint) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.ms-excel");
        // intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult( Intent.createChooser(intent, hint), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.",  Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch(requestCode){
            case FILE_SELECT_CODE:
                // ResultActivity的返回数据
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    final String path = FileUtils.getPath(this, uri);
                    if (TextUtils.isEmpty(path)) {
                        try {
                            final FileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor();
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    xlsData = doRead(new FileInputStream(fileDescriptor));
                                    doDeal();
                                }
                            });
                        } catch (Exception e) {

                        }
                    } else {
                        Toast.makeText(BaseActivity.this, path, Toast.LENGTH_LONG).show();
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                final SharedPreferences sharedPreferences = getSharedPreferences("name", MODE_PRIVATE);
                                sharedPreferences.edit().putString("path", path).commit();
                                xlsData = doReadPath(path);
                                doDeal();
                            }
                        });
                    }
                    return;
                }
                postReadExcelFail();
        }
    }

    protected void doDeal() {
        BaseActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(xlsData)) {
                    postReadExcel();
                    return;
                } else {
                    postReadExcelFail();
                }
            }
        });
    }

    abstract void postReadExcel();

    abstract void postReadExcelFail();

    protected String doReadPath(String path) {
        try {
            return doRead(new FileInputStream(path));
        } catch (Exception e) {

        }
        return "";
    }

    private String doRead(FileInputStream is) {
        // 班组	姓名	性别	电话	全拼
        try {
            String result = "";
            HSSFWorkbook wb = new HSSFWorkbook(is);
            org.apache.poi.ss.usermodel.Sheet sheet1 = wb.getSheetAt(0);
            for (Row row : sheet1) {
                String r1 = "";
                for (int i = 0;  i < row.getLastCellNum(); i++ ) {
                    Cell cell = row.getCell(i);
                    String cellString = "";
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_STRING:
                                cellString = cell.getRichStringCellValue().getString();
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    cellString = String.valueOf(cell.getDateCellValue());
                                } else {
                                    double phone = cell.getNumericCellValue();
                                    cellString = new DecimalFormat("###").format(phone);
                                }
                                break;
                            default:
                                System.out.println();
                        }
                    }
                    r1 += " " + cellString.trim();
                }
                if (!TextUtils.isEmpty(r1)) {
                    result += r1.substring(1) + "\n";
                }
            }
            return result;
        } catch (Exception e) {
            Log.d("err", e.toString());
        }
        return "";
    }


}
