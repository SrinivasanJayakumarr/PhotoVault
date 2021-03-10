package com.srinivasan.photovault;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
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

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private Button btnSignUp;
    private Button btnLogIn;

    private LinearLayout llMain;
    private RelativeLayout containerMain;
    private TextView titleTxt;
    private TextInputLayout mailEditText;
    private TextInputLayout passwedEditText;
    private TextInputLayout nameEditText;
    private TextInputLayout phoneEditText;

    private ImageView login_img;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            Intent intent = new Intent(LogInActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_log_in);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); // For transparent statusbar

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        firebaseAuth = FirebaseAuth.getInstance();

        btnSignUp = findViewById(R.id.btn_signup);
        btnLogIn = findViewById(R.id.btn_login);

        llMain = findViewById(R.id.ll_main);
        containerMain = findViewById(R.id.container_main);
        titleTxt = findViewById(R.id.title_txt);
        mailEditText = findViewById(R.id.login_mail);
        passwedEditText = findViewById(R.id.login_passwrd);
        nameEditText = findViewById(R.id.login_fname);
        phoneEditText = findViewById(R.id.login_phone);
        login_img = findViewById(R.id.img_login);

        nameEditText.setVisibility(View.GONE);
        phoneEditText.setVisibility(View.GONE);

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean emailvalid = isEmailValid();
                boolean passwordvalid = isPasswordValid();

                Log.d(TAG, "Eamil : " + emailvalid + " Password : " + passwordvalid);

                if (emailvalid && passwordvalid) {
                    loginUser();
                }

            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);

                Pair[] pairs = new Pair[9];
                pairs[0] = new Pair<View, String>(llMain, "ll_main_trans");
                pairs[1] = new Pair<View, String>(titleTxt, "title_trans");
                pairs[2] = new Pair<View, String>(mailEditText, "email_trans");
                pairs[3] = new Pair<View, String>(passwedEditText, "passwrd_trans");
                pairs[4] = new Pair<View, String>(nameEditText, "fname_trans");
                pairs[5] = new Pair<View, String>(phoneEditText, "phone_trans");
                pairs[6] = new Pair<View, String>(containerMain, "container_trans");
                pairs[7] = new Pair<View, String>(btnLogIn, "btn_main_trans");
                pairs[8] = new Pair<View, String>(login_img, "img_trans");
                //pairs[8] = new Pair<View,String>(btnSignUp,"btn_txt_trans");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(LogInActivity.this, pairs);
                startActivity(intent, options.toBundle());
                finish();
            }
        });

    }

    private void loginUser() {

        String loginMail = mailEditText.getEditText().getText().toString().trim();
        Log.d(TAG,"Login Mail : "+loginMail);
        String loginPassword = passwedEditText.getEditText().getText().toString().trim();
        Log.d(TAG,"Login Password : "+loginPassword);

        mailEditText.setErrorEnabled(false);
        passwedEditText.setErrorEnabled(false);
        mailEditText.setErrorIconDrawable(0);
        passwedEditText.setErrorIconDrawable(0);

        firebaseAuth.signInWithEmailAndPassword(loginMail,loginPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d(TAG,"New user registration : "+task.isSuccessful());
                        if (!task.isSuccessful()){
                            if (String.valueOf(task.getException()).contains("There is no user record")){
                                mailEditText.setError("User doesn't exist");
                                mailEditText.setErrorIconDrawable(0);
                            }else {
                                passwedEditText.setError("Enter a correct password");
                                passwedEditText.setErrorIconDrawable(0);
                            }
                            //Toast.makeText(LogInActivity.this, "Auth failed."+task.getException(), Toast.LENGTH_SHORT).show();
                        }else {
                            Intent intent = new Intent(LogInActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    }
                });

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
}