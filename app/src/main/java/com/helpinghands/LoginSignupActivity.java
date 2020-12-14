package com.helpinghands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginSignupActivity extends AppCompatActivity {

    private static final String TAG = LoginSignupActivity.class.getSimpleName();

    @BindView(R.id.Handler)
    TextView mHandlerTextView;

    @BindView(R.id.name_edittext)
    EditText mNameEditText;
    @BindView(R.id.email_edittext)
    EditText mEmailEditText;

    @BindView(R.id.password_edittext)
    EditText mPasswordEditText;

    @BindView(R.id.login_button)
    Button mLoginButton;

    @BindView(R.id.signup_button)
    Button mSignupButton;

    boolean isLogin = false;
    String userType;
    private FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser()!= null){
            navigate();
        }
    }

    @Override
    protected void onRestart() {


        super.onRestart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        userType = getIntent().getStringExtra("type");


        Log.d(TAG, "onCreate: " + userType);


        databaseReference = FirebaseDatabase.getInstance().getReference(userType);


        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isLogin)
                    login(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString());
                else
                    signup(mNameEditText.getText().toString(), mEmailEditText.getText().toString(), mPasswordEditText.getText().toString());
            }
        });

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLogin) {

                    mHandlerTextView.setText("Register");
                    mSignupButton.setText("Already registered ? Login");
                    mLoginButton.setText("Register");
                    mNameEditText.setVisibility(View.VISIBLE);
                    isLogin = false;
                } else {

                    mHandlerTextView.setText("Login");
                    mSignupButton.setText("New user ? Register");
                    mLoginButton.setText("Login");
                    mNameEditText.setVisibility(View.GONE);
                    isLogin = true;
                }
            }
        });


    }

    void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginSignupActivity.this, "Success",
                                    Toast.LENGTH_SHORT).show();
                            navigate();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginSignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }


    void signup(final String name, final String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginSignupActivity.this, "Success",
                                    Toast.LENGTH_SHORT).show();

                            String mUid = databaseReference.push().getKey();
                            databaseReference.child(mUid).child("name").setValue(name);
                            databaseReference.child(mUid).child("email").setValue(email);
                            navigate();
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginSignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }

    private void navigate() {
        if (userType.equals("donor")) {
            startActivity(new Intent(LoginSignupActivity.this, DonorActivity.class));
            finish();

        } else {
            startActivity(new Intent(LoginSignupActivity.this, ReceiverActivity.class));
            finish();
        }
    }


}
