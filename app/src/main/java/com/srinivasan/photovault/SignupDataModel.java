package com.srinivasan.photovault;

import android.content.Context;
import android.widget.Toast;

public class SignupDataModel {

    private String uid;
    private String full_name;
    private String mail;
    private String password;
    private String phone;
    //private Context context;

    public SignupDataModel(){
        //Empty constructor
    }

    public SignupDataModel (String uid, String full_name, String mail, String password, String phone){

        this.uid = uid;
        this.full_name = full_name;
        this.mail = mail;
        this.password = password;
        this.phone = phone;
        //this.context = context;

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
