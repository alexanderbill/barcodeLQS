package com.uuch.android_zxinglibrary.activity;

import android.text.TextUtils;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ubuntu on 17-10-12.
 */

public class Config {

    public String version;
    public String color;
    public List<String> users;
    public String seal;
    public String APP_title;
    public String ID_title;
    public String footprint;

    public static final Config instance = new Config();

    private Config() {

    }

    public static Config getInstance() {
        return instance;
    }

    public void init(JSONObject object) {
        version = object.optString("v", "1.0.3");
        color = object.optString("color", "#FFFFFF");
        seal = object.optString("seal", "");
        APP_title = object.optString("APP_title", "");
        ID_title = object.optString("ID_title", "");
        footprint = object.optString("footprint", "");
        String userstring = object.optString("users", "");
        if (!TextUtils.isEmpty(userstring)) {
            userstring += "/admin";
        } else {
            userstring = "admin";
        }
        users = Arrays.asList(userstring.split("/"));
    }
}
