package com.srinivasan.photovault;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private Button btnLogIn;
    private Button btnSignUp;

    private LinearLayout llMain;
    private RelativeLayout containerMain;
    private TextView titleTxt;
    private TextInputLayout mailEditText;
    private TextInputLayout passwedEditText;
    private TextInputLayout nameEditText;
    private TextInputLayout phoneEditText;

    private ImageView signup_img;

    private String uid;

    private FirebaseAuth firebaseAuth;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); // For transparent statusbar

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        firebaseAuth = FirebaseAuth.getInstance();

        btnLogIn = findViewById(R.id.btn_login);
        btnSignUp = findViewById(R.id.btn_signup);

        llMain = findViewById(R.id.ll_main);
        containerMain = findViewById(R.id.container_main);
        titleTxt = findViewById(R.id.title_txt);
        mailEditText = findViewById(R.id.signup_email);
        passwedEditText = findViewById(R.id.signup_passwrd);
        nameEditText = findViewById(R.id.signup_fname);
        phoneEditText = findViewById(R.id.signup_phone);
        signup_img = findViewById(R.id.img_signup);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean emailvalid = isEmailValid();
                boolean passwordvalid = isPasswordValid();
                boolean phonevalid = isPhoneValid();
                boolean namevalid = isNameValid();

                Log.d(TAG, "Eamil : " + emailvalid + " Password : " + passwordvalid);
                Log.d(TAG, "Phone : " + phonevalid + " Name : " + namevalid);

                if (emailvalid && passwordvalid && phonevalid && namevalid) {
                    registerUser();
                }
            }
        });

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);

                Pair[] pairs = new Pair[9];
                pairs[0] = new Pair<View, String>(llMain, "ll_main_trans");
                pairs[1] = new Pair<View, String>(titleTxt, "title_trans");
                pairs[2] = new Pair<View, String>(mailEditText, "email_trans");
                pairs[3] = new Pair<View, String>(passwedEditText, "passwrd_trans");
                pairs[4] = new Pair<View, String>(nameEditText, "fname_trans");
                pairs[5] = new Pair<View, String>(phoneEditText, "phone_trans");
                pairs[6] = new Pair<View, String>(containerMain, "container_trans");
                pairs[7] = new Pair<View, String>(btnSignUp, "btn_main_trans");
                pairs[8] = new Pair<View,String>(signup_img,"img_trans");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SignUpActivity.this, pairs);
                startActivity(intent, options.toBundle());
                finish();
            }
        });

    }

    private void registerUser() {

        String signupMail = mailEditText.getEditText().getText().toString().trim();
        Log.d(TAG, "Signup Mail : " + signupMail);
        String signupPassword = passwedEditText.getEditText().getText().toString().trim();
        Log.d(TAG, "Signup Password : " + signupPassword);

        firebaseAuth.createUserWithEmailAndPassword(signupMail, signupPassword)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d(TAG, "New user registration : " + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            if (String.valueOf(task.getException()).contains("The email address is already in use")){
                                mailEditText.setError("Email address already in use");
                                mailEditText.setErrorIconDrawable(0);
                            }
                            //Toast.makeText(SignUpActivity.this, "Auth failed." + task.getException(), Toast.LENGTH_SHORT).show();
                        } else {

                            uid = task.getResult().getUser().getUid();
                            Log.d(TAG, "User uid : " + uid);
                            savetoFirebase();

                        }

                    }
                });

    }

    public void savetoFirebase() {
        String modelName = nameEditText.getEditText().getText().toString().trim();
        String modelMail = mailEditText.getEditText().getText().toString().trim();
        String modelPassword = passwedEditText.getEditText().getText().toString().trim();
        String modelPhone = phoneEditText.getEditText().getText().toString().trim();

        Log.d(TAG, "M_Name : " + modelName + " M_Mail : " + modelMail);
        Log.d(TAG, "M_Password : " + modelPassword + " M_Phone : " + modelPhone);

        //photo-vault-349c7-default-rtdb
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        databaseReference = rootNode.getReference();

        //Passing data to Model Class
        SignupDataModel signupDataModel = new SignupDataModel(uid, modelName, modelMail, modelPassword, modelPhone);

        databaseReference.child("UserDetails").child(uid).setValue(signupDataModel);

        //Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        //startActivity(intent);
        finish();
        startActivity(new Intent(SignUpActivity.this, MainActivity.class));

    }

    public boolean isPasswordValid() {

        passwedEditText.setErrorEnabled(false);

        passwedEditText.setErrorIconDrawable(0);

        String passwrd = passwedEditText.getEditText().getText().toString().trim();

        boolean b = true;

        if (passwrd.isEmpty() || !passwrd.isEmpty()) {

            if (passwrd.isEmpty()) {
                passwedEditText.setError("Field can't be empty");
                passwedEditText.setErrorIconDrawable(0);
                b = false;
            } else if (!passwrd.isEmpty()) {
                if (passwrd.contains(" ")) {
                    passwedEditText.setError("White spaces aren't allowed");
                    passwedEditText.setErrorIconDrawable(0);
                    b = false;
                } else if (!passwrd.contains(" ")) {
                    if (passwrd.length() <= 5) {
                        passwedEditText.setError("Password should contains atleast 5 letters");
                        passwedEditText.setErrorIconDrawable(0);
                        b = false;
                    }
                }
            } else {
                b = true;
            }
        }
        return b;
    }

    public boolean isEmailValid() {

        mailEditText.setErrorEnabled(false);

        mailEditText.setErrorIconDrawable(0);

        String mail = mailEditText.getEditText().getText().toString().trim();

        boolean b = true;

        if (mail.isEmpty() || !mail.isEmpty()) {

            if (mail.isEmpty()) {
                mailEditText.setError("Field can't be empty");
                mailEditText.setErrorIconDrawable(0);
                b = false;
            } else if (!mail.isEmpty()) {
                if (mail.contains(" ")) {
                    mailEditText.setError("White spaces aren't allowed");
                    mailEditText.setErrorIconDrawable(0);
                    b = false;
                } else if (!mail.contains(" ")) {
                    if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                        mailEditText.setError("Invalid mail");
                        mailEditText.setErrorIconDrawable(0);
                        b = false;
                    }
                }
            } else {

                b = true;
            }

        }
        return b;
    }

    public boolean isNameValid() {

        nameEditText.setErrorEnabled(false);

        nameEditText.setErrorIconDrawable(0);

        String name = nameEditText.getEditText().getText().toString().trim();

        boolean b = true;

        if (name.isEmpty() || !name.isEmpty()) {

            if (name.isEmpty()) {
                nameEditText.setError("Field can't be empty");
                nameEditText.setErrorIconDrawable(0);
                b = false;
            } else if (!name.isEmpty()) {
                if (name.contains(" ")) {
                    nameEditText.setError("White spaces aren't allowed");
                    nameEditText.setErrorIconDrawable(0);
                    b = false;
                } else if (!name.contains(" ")) {
                    if (name.matches(".*\\d.*")) {
                        nameEditText.setError("Name doesn't have numbers");
                        nameEditText.setErrorIconDrawable(0);
                        b = false;
                    } else if (!name.matches(".*\\d.*")) {
                        if (Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE).matcher(name).matches()) {//Pattern for checking spl character in a string
                            nameEditText.setError("Name doesn't have spl characters");
                            nameEditText.setErrorIconDrawable(0);
                            b = false;
                        }
                    }
                }
            } else {

                b = true;
            }

        }
        return b;

    }

    public boolean isPhoneValid() {

        phoneEditText.setErrorEnabled(false);

        phoneEditText.setErrorIconDrawable(0);

        String phone = phoneEditText.getEditText().getText().toString().trim();

        boolean b = true;

        if (phone.isEmpty() || !phone.isEmpty()) {

            if (phone.isEmpty()) {
                phoneEditText.setError("Field can't be empty");
                phoneEditText.setErrorIconDrawable(0);
                b = false;
            } else if (!phone.isEmpty()) {
                if (phone.contains(" ")) {
                    phoneEditText.setError("White spaces aren't allowed");
                    phoneEditText.setErrorIconDrawable(0);
                    b = false;
                } else if (!phone.contains(" ")) {
                    if (!Pattern.compile("^[+]?[0-9]{10,13}$").matcher(phone).matches()) {
                        phoneEditText.setError("Invalid phone number");
                        phoneEditText.setErrorIconDrawable(0);
                        b = false;
                    }
                }
            } else {

                b = true;
            }

        }
        return b;
    }

}