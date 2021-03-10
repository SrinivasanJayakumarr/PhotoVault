package com.srinivasan.photovault;

import com.google.firebase.database.Exclude;

public class PhotoInfoModel {

    private String photoName;
    private String photoUrl;
    private String photoKey;

    public PhotoInfoModel(){
        //Empty constructor for firebase
    }

    public PhotoInfoModel(String photoName, String photoUrl){

        this.photoName = photoName;
        this.photoUrl = photoUrl;

    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Exclude
    public String getPhotoKey() {
        return photoKey;
    }

    @Exclude
    public void setPhotoKey(String photoKey) {
        this.photoKey = photoKey;
    }
}
