package com.srinivasan.photovault;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CyclerAdapter.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private String result;

    private TextView currentUserName, hidden_txt_cycle;
    private Button btnLogOut;
    private EditText mEditTextFileName;
    private ProgressBar progressBar;
    private ImageView mReplaceImageView;

    private LinearLayout ll_upload, ll_replace,ll_details;
    private Uri mImageUrl;

    private String uid;
    private String uniqueId;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private StorageReference storageReference;//For photo upload
    private StorageTask mUploadTask;//For photo upload

    private FirebaseStorage firebaseStorage;//For deleting images
    private ValueEventListener mDBlistener;

    private RecyclerView recyclerView;
    private CyclerAdapter cyclerAdapter;
    private List<PhotoInfoModel> photoInfoModelList;

    private String currentUserPhone;
    private String currentUserPasswrd;
    private String currentUser;
    private String currentUserFname; //For profile Window

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); // For transparent statusbar

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        currentUserName = findViewById(R.id.name);
        hidden_txt_cycle = findViewById(R.id.hidden_txt_cycle);
        btnLogOut = findViewById(R.id.btn_logout);
        ll_replace = findViewById(R.id.ll_replace);
        ll_upload = findViewById(R.id.upload_file);
        ll_details = findViewById(R.id.ll_details);
        mEditTextFileName = findViewById(R.id.file_name_editTxt);
        progressBar = findViewById(R.id.progress_bar_file);
        mReplaceImageView = findViewById(R.id.replace_img_vw);

        //For deleting images from db
        firebaseStorage = FirebaseStorage.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        photoInfoModelList = new ArrayList<PhotoInfoModel>();

        currentUser = mail; //Data for profile window

        getUserDetailsFromDb();

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Successfully logged out", Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LogInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ll_replace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        ll_upload.setVisibility(View.GONE);

        ll_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(MainActivity.this, "Uploading...", Toast.LENGTH_SHORT).show();
                } else {
                    uploadPhotoToFirebase();
                }
            }
        });

        ll_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callProfileWindow();
            }
        });

    }

    public void callProfileWindow(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialog);

        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.profile_window,null);

        de.hdodenhof.circleimageview.CircleImageView circleImageView = dialogLayout.findViewById(R.id.dialog_profile_img);
        TextInputLayout dialog_mail = dialogLayout.findViewById(R.id.dialog_mail);
        TextInputLayout dialog_fname = dialogLayout.findViewById(R.id.dialog_fname);
        TextInputLayout dialog_phone = dialogLayout.findViewById(R.id.dialog_phone);
        TextInputLayout dialog_passwrd = dialogLayout.findViewById(R.id.dialog_passwrd);
        Button closeBtn = dialogLayout.findViewById(R.id.btn_close);

        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        circleImageView.setImageResource(R.drawable.ic_launcher_mdpi);
        dialog_mail.getEditText().setText(currentUser);
        dialog_fname.getEditText().setText(currentUserFname);
        dialog_phone.getEditText().setText(currentUserPhone);
        dialog_passwrd.getEditText().setText(currentUserPasswrd);

    }

    private void getRecyclerViewData() {
        databaseReference = FirebaseDatabase.getInstance().getReference("UserDetails");
        Log.d(TAG, "UID : " + uid);
        mDBlistener = databaseReference.child(uid).child("ImageInfo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                hidden_txt_cycle.setVisibility(View.GONE);
                photoInfoModelList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "Snapshot : " + postSnapshot);
                    PhotoInfoModel photoInfoModel = postSnapshot.getValue(PhotoInfoModel.class);
                    photoInfoModel.setPhotoKey(postSnapshot.getKey());
                    photoInfoModelList.add(photoInfoModel);
                }

                cyclerAdapter = new CyclerAdapter(MainActivity.this, photoInfoModelList);
                recyclerView.setAdapter(cyclerAdapter);
                cyclerAdapter.setOnItemClickListener(MainActivity.this);

                cyclerAdapter.notifyDataSetChanged();

                if (photoInfoModelList.isEmpty()) {
                    Log.d(TAG,"Empty List.No photos to display.");
                    hidden_txt_cycle.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(MainActivity.this, "Data accessing error : " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public void setInfoToFirebase(String nameCustom, String urlCustom) {
        PhotoInfoModel photoInfoModel = new PhotoInfoModel(nameCustom, urlCustom);
        Log.d(TAG, "Photo Name : " + photoInfoModel.getPhotoName() + "Photo URL : " + photoInfoModel.getPhotoUrl());
        databaseReference.child(uid).child("ImageInfo").child(uniqueId).setValue(photoInfoModel);

    }

    private void uploadPhotoToFirebase() {
        Log.d(TAG, "User UID : " + uid);
        uniqueId = String.valueOf(System.currentTimeMillis());
        storageReference = FirebaseStorage.getInstance().getReference().child(uid).child(uniqueId);
        databaseReference = FirebaseDatabase.getInstance().getReference("UserDetails");
        final String NAME = mEditTextFileName.getText().toString().trim();

        if (mImageUrl != null) {
            final StorageReference fileRef = storageReference.child(mEditTextFileName.getText().toString().trim() + "." + getFileExtension(mImageUrl));
            mUploadTask = fileRef.putFile(mImageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                    //mReplaceImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String URL = uri.toString();
                            setInfoToFirebase(NAME, URL);
                        }
                    });

                    mReplaceImageView.setImageResource(R.drawable.ic_file_plus);
                    mEditTextFileName.setText(null);
                    ll_upload.setVisibility(View.GONE);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    //double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }

    }

    //To display image into choose btn
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            mImageUrl = data.getData();

            result = null;
            if (mImageUrl.getScheme().equals("content")) {
                Cursor cursor = getContentResolver().query(mImageUrl, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            }
            if (result == null) {
                result = mImageUrl.getPath();
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
            Picasso.get().load(mImageUrl).into(mReplaceImageView);
            Log.d(TAG, "File Name : " + result);
            mEditTextFileName.setText(result);
            ll_upload.setVisibility(View.VISIBLE);

        }

    }

    private void getUserDetailsFromDb() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        Log.d(TAG, "UID : " + uid);

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        databaseReference = rootNode.getReference();

        databaseReference.child("UserDetails").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                SignupDataModel signupDataModel = dataSnapshot.getValue(SignupDataModel.class);
                Log.d(TAG, "Data Snapshot : " + dataSnapshot);

                String userName = signupDataModel.getFull_name();
                String userPhone = signupDataModel.getPhone();
                String userPasswrd = signupDataModel.getPassword();

                currentUserName.setText(userName);
                currentUserFname = userName;
                currentUserPhone = userPhone;
                currentUserPasswrd = userPasswrd;
                getRecyclerViewData();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.d(TAG, "Failed to read values. : " + databaseError.toException());

            }
        });

    }

    @Override
    public void onItemClick(int position) {
        //Toast.makeText(this, "Normal click : "+position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {

        final int posi = position;

        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialog);

        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.confirm_window,null);

        Button cancelBtn = dialogLayout.findViewById(R.id.btn_cancel);
        Button deleteBtn = dialogLayout.findViewById(R.id.btn_delete);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PhotoInfoModel selectedItem = photoInfoModelList.get(posi);
                final String selectedKey = selectedItem.getPhotoKey();

                StorageReference imageref = firebaseStorage.getReferenceFromUrl(selectedItem.getPhotoUrl());
                imageref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        databaseReference.child(uid).child("ImageInfo").child(selectedKey).removeValue();
                        Toast.makeText(MainActivity.this, "Item deleted.", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();


    }

    @Override
    public void onOpenClick(int position) {
        //Toast.makeText(this, "Open :"+position, Toast.LENGTH_SHORT).show();
        final PhotoInfoModel selectedImage = photoInfoModelList.get(position);
        final String selectedURL = selectedImage.getPhotoUrl();
        Log.d(TAG,"Popup url : "+selectedURL);

        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialog);

        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.popup_window,null);

        ImageView imageView = (ImageView) dialogLayout.findViewById(R.id.full_img);
        Button closeBtn = dialogLayout.findViewById(R.id.btn_close);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (selectedURL != null && !selectedURL.isEmpty()){
            Log.d(TAG,"URL inside dialog : "+selectedURL);
            Picasso.get().load(selectedURL)
                    .into(imageView);
        }else {
            imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_upload_cloud));
        }

        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference = FirebaseDatabase.getInstance().getReference("UserDetails");
        databaseReference.child(uid).child("ImageInfo").removeEventListener(mDBlistener);
    }
}