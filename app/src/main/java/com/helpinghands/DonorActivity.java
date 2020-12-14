package com.helpinghands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.helpinghands.Adapters.RequestsAdapter;
import com.squareup.picasso.Picasso;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DonorActivity extends AppCompatActivity {


    @BindView(R.id.logout_button)
    Button mLogoutButton;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mUri = null;
    private ImageView mPreviewImage;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private ProgressBar mProgressBar;
    private String downloadUrl;
    AlertDialog dailog;


    @BindView(R.id.offer_button)
    Button mOfferButton;

    @BindView(R.id.request_list_recyclerView)
    RecyclerView mRequestListRecyclerView;

    ArrayList<DataSnapshot> mRequestList = new ArrayList<>();

    RequestsAdapter mRequestsAdapter;
    FirebaseAuth mFirebaseAuth;
    DatabaseReference mRequestsDatabaseReference,mOffersDatabaseReference;
    private String TAG = DonorActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor);
        ButterKnife.bind(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mRequestsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("requests");
        mOffersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("offers");

        mRequestListRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mRequestsAdapter = new RequestsAdapter(mRequestList);

        mRequestListRecyclerView.setAdapter(mRequestsAdapter);

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseAuth.signOut();
                startActivity(new Intent(DonorActivity.this, MainActivity.class));
                finish();
            }
        });


        mRequestsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> requestlist = snapshot.getChildren();

                mRequestList.clear();

                for (DataSnapshot eachRequest : requestlist) {
                    Log.d(TAG, "onDataChange: " + eachRequest.child("books"));
                    Log.d(TAG, "onDataChange: " + eachRequest.child("clothes"));
                    mRequestList.add(eachRequest);
                }

                mRequestsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Offer part

        mOfferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(DonorActivity.this);

                View mView = getLayoutInflater().inflate(R.layout.dailog_add_offer, null);

                final EditText mQuantityEditext = mView.findViewById(R.id.quantity_edittext);
                final EditText mMinAgeEditext = mView.findViewById(R.id.min_age_edittext);
                final EditText mMaxAgeEditext = mView.findViewById(R.id.max_age_edittext);
                mProgressBar = mView.findViewById(R.id.upload_progress);

                Button mOfferSubmitButton = mView.findViewById(R.id.submit_request_button);
                Button mBrowseButton = mView.findViewById(R.id.browse_button);
                mPreviewImage = mView.findViewById(R.id.preview_imageView);

                mBrowseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //openFileChooser();

                        PickSetup setup = new PickSetup()
                                .setTitle("Choose")
                                .setMaxSize(1)
                                .setProgressText("Processsing your image")
                                .setButtonOrientation(LinearLayout.HORIZONTAL)
                                .setSystemDialog(false)
                                .setGalleryIcon(R.drawable.ic_gallery_option)
                                .setCameraIcon(R.drawable.ic_camera_option);

                        PickImageDialog.build(setup)
                                .setOnPickResult(new IPickResult() {
                                    @Override
                                    public void onPickResult(PickResult r) {
                                        //TODO: do what you have to...
                                        if (r.getError() == null) {
                                            mUri = r.getUri();
                                            Log.d(TAG, "onPickResult: " + mUri);
                                            //mProductImageView.setImageURI(mUri);
                                            //isImageChanged = true;

                                            Picasso.get().load(mUri).into(mPreviewImage);
                                            mPreviewImage.setVisibility(View.VISIBLE);


                                        } else {
                                            //Handle possible errors
                                            //TODO: do what you have to do with r.getError();
                                            Toast.makeText(DonorActivity.this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
                                        }


                                    }
                                })
                                .setOnPickCancel(new IPickCancel() {
                                    @Override
                                    public void onCancelClick() {
                                        //TODO: do what you have to if user clicked cancel
                                    }
                                }).show(DonorActivity.this);

                    }
                });

                mOfferSubmitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!mQuantityEditext.getText().toString().isEmpty() &&
                                !mMinAgeEditext.getText().toString().isEmpty() &&
                                !mMaxAgeEditext.getText().toString().isEmpty() &&
                                mUri != null) {

                            uploadFile(mQuantityEditext.getText().toString(), mMinAgeEditext.getText().toString(), mMaxAgeEditext.getText().toString());
                            //submitBoth(mQuantityEditext.getText().toString(), mMinAgeEditext.getText().toString(), mMaxAgeEditext.getText().toString());

                        } else if (!mQuantityEditext.getText().toString().isEmpty() &&
                                (mMinAgeEditext.getText().toString().isEmpty() ||
                                        mMaxAgeEditext.getText().toString().isEmpty())) {
                            submitBooks(mQuantityEditext.getText().toString());

                            // no image required
                        } else if (mQuantityEditext.getText().toString().isEmpty() && !mMinAgeEditext.getText().toString().isEmpty() &&
                                !mMaxAgeEditext.getText().toString().isEmpty() && mUri != null) {
                            //submitClothes(mMinAgeEditext.getText().toString(), mMaxAgeEditext.getText().toString());
                            uploadFile("NaN",mMinAgeEditext.getText().toString(), mMaxAgeEditext.getText().toString());
                        } else {
                            Toast.makeText(DonorActivity.this, "Please enter offer details !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                mBuilder.setView(mView);
                dailog = mBuilder.create();
                dailog.setCancelable(true);
                dailog.show();

            }
        });

    }

    private void submitClothes(String minage, String maxage,Uri mDownloadUri) {


        String mUid = mRequestsDatabaseReference.push().getKey();

        mOffersDatabaseReference.child(mUid).child("clothes").setValue(minage + " to " + maxage + " years");
        mOffersDatabaseReference.child(mUid).child("email").setValue(mFirebaseAuth.getCurrentUser().getEmail());
        mOffersDatabaseReference.child(mUid).child("books").setValue("NaN");
        mOffersDatabaseReference.child(mUid).child("imageURL").setValue(mDownloadUri+"");

    }

    private void submitBooks(String qty) { //NO IMAGE
        String mUid = mRequestsDatabaseReference.push().getKey();
        mOffersDatabaseReference.child(mUid).child("books").setValue(qty);
        mOffersDatabaseReference.child(mUid).child("email").setValue(mFirebaseAuth.getCurrentUser().getEmail());
        mOffersDatabaseReference.child(mUid).child("clothes").setValue("NaN");
        mOffersDatabaseReference.child(mUid).child("imageURL").setValue("NaN");
    }

    private void submitBoth(String qty, String minage, String maxage,Uri mDownloadUri) {
        String mUid = mRequestsDatabaseReference.push().getKey();
        mOffersDatabaseReference.child(mUid).child("books").setValue(qty);
        mOffersDatabaseReference.child(mUid).child("email").setValue(mFirebaseAuth.getCurrentUser().getEmail());
        mOffersDatabaseReference.child(mUid).child("clothes").setValue(minage + " to " + maxage + " years");
        mOffersDatabaseReference.child(mUid).child("imageURL").setValue(mDownloadUri+"");

    }

/*
    //not used
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    //not used
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            mUri = data.getData();
            Picasso.get().load(mUri).into(mPreviewImage);
            mPreviewImage.setVisibility(View.VISIBLE);
        }

    }*/

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadFile(final String qty, final String minage, final String maxage) {

        String fileName = System.currentTimeMillis() + "." + getFileExtension(mUri);
        final String path = "uploads/" + fileName;

        final StorageReference storageRef = storage.getReference();

        final StorageReference productImage = storageRef.child(path);
        final UploadTask uploadTask = productImage.putFile(mUri);


        Task<Uri> urlTask = uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                mProgressBar.setProgress((int) progress);
            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return productImage.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    //if image upload then store offer

                    if (qty.equals("NaN")){
                        submitClothes(minage,maxage,downloadUri);
                    }else {
                        submitBoth(qty,minage,maxage,downloadUri);
                    }
                    //AddProduct(downloadUri.toString());

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setProgress(0);
                            Toast.makeText(DonorActivity.this, "Offer Posted", Toast.LENGTH_SHORT).show();
                            if (dailog.isShowing()) {
                                dailog.dismiss();
                                mUri = null;
                            }
                        }
                    }, 500);

                } else {
                    Toast.makeText(DonorActivity.this, "Error ! Could not add offer.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}

