package com.uuch.android_zxinglibrary.activity;

import android.text.TextUtils;

import com.uuch.android_zxinglibrary.utils.PinyinHelper;

/**
 * Created by u1 on 18-2-9.
 */

class User {
    public final String mName;
    public final String mSex;
    public String mPhone;
    public final String mDepart;
    public final String mNick;
    public String mDesp;
    public String mKey;
    public String mSeal;

    public User(String name, String sex, String phone, String depart, String nick, String desp) {
        mName = name;
        mSex = sex;
        mPhone = phone;
        mDepart = depart;
        mNick = nick;
        mDesp = desp;
    }

    public User(String s) {
        String[] sv = s.trim().split(" ");
        mDepart = sv[0];
        mName = sv[1];
        mSex = sv[2];
        mPhone = sv[3];

        if (sv.length > 4 && !TextUtils.isEmpty(sv[4])) {
            mNick = sv[4];
        } else {
            mNick = PinyinHelper.getInstance().getPinyins(mName, "");
        }
        genEnc();

        mDesp = " ";
        for (int i = 5; i < sv.length; i++) {
            mDesp += sv[i] + " ";
        }
    }

    private void genEnc() {
        String toEnc = "A";
        if ("女".equals(mSex)) {
            toEnc = "B";
        }
        toEnc += mPhone;
        toEnc += mNick;
        mKey = toEnc;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public void setkey(String key) {
        mKey = key;
    }

    public void setSeal(String seal) {
        mSeal = seal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mKey);
        if (!TextUtils.isEmpty(mSeal)) {
            sb.append(" " + mSeal);
        }
        if (!TextUtils.isEmpty(mName)) {
            sb.append(" " + mName);
        }
        if (!TextUtils.isEmpty(mDesp)) {
            sb.append(" " + mDesp);
        }
        return sb.toString();
    }

    public void main() {
        User user = new User("aa 周磊 17710275730  20180101  十大 挨打的");
        System.out.print(user);
    }

}