package com.helpinghands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.helpinghands.Adapters.OffersAdapter;
import com.helpinghands.Adapters.RequestsAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReceiverActivity extends AppCompatActivity {

    @BindView(R.id.logout_button)
    Button mLogoutButton;

    @BindView(R.id.offer_list_recyclerView)
    RecyclerView mOfferRecyclerView;

    ArrayList<DataSnapshot> mOfferList = new ArrayList<>();

    OffersAdapter mOffersAdapter;
    DatabaseReference mRequestsDatabaseReference,mOffersDatabaseReference;



    @BindView(R.id.request_button)
    Button mRequestButton;

    AlertDialog dailog;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        ButterKnife.bind(this);
        mRequestsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("requests");
        mOffersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("offers");

        mAuth = FirebaseAuth.getInstance();


        mOfferRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mOffersAdapter = new OffersAdapter(mOfferList);

        mOfferRecyclerView.setAdapter(mOffersAdapter);

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(ReceiverActivity.this, MainActivity.class));
                finish();
            }
        });
        mOffersDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> offerlist = snapshot.getChildren();
                mOfferList.clear();
                for(DataSnapshot eachoffer: offerlist){
                    mOfferList.add(eachoffer);
                }
                mOffersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(ReceiverActivity.this);

                View mView = getLayoutInflater().inflate(R.layout.dailog_add_request, null);
                final EditText mQuantityEditext = mView.findViewById(R.id.quantity_edittext);
                final EditText mMinAgeEditext = mView.findViewById(R.id.min_age_edittext);
                final EditText mMaxAgeEditext = mView.findViewById(R.id.max_age_edittext);

                Button mRequestSubmitButton = mView.findViewById(R.id.submit_request_button);

                mRequestSubmitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!mQuantityEditext.getText().toString().isEmpty() &&
                                !mMinAgeEditext.getText().toString().isEmpty() &&
                                !mMaxAgeEditext.getText().toString().isEmpty()) {

                            submitBoth(mQuantityEditext.getText().toString(), mMinAgeEditext.getText().toString(), mMaxAgeEditext.getText().toString());

                        } else if (!mQuantityEditext.getText().toString().isEmpty()) {
                            submitBooks(mQuantityEditext.getText().toString());
                        } else if (!mMinAgeEditext.getText().toString().isEmpty() &&
                                !mMaxAgeEditext.getText().toString().isEmpty()) {
                            submitClothes(mMinAgeEditext.getText().toString(), mMaxAgeEditext.getText().toString());
                        } else {
                            Toast.makeText(ReceiverActivity.this, "Please enter request details !", Toast.LENGTH_SHORT).show();
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

    private void submitClothes(String minage, String maxage) {
        String mUid = mRequestsDatabaseReference.push().getKey();

        mRequestsDatabaseReference.child(mUid).child("clothes").setValue(minage + " to " + maxage + " years");
        mRequestsDatabaseReference.child(mUid).child("email").setValue(mAuth.getCurrentUser().getEmail());
        mRequestsDatabaseReference.child(mUid).child("books").setValue("NaN");
    }

    private void submitBooks(String qty) {
        String mUid = mRequestsDatabaseReference.push().getKey();
        mRequestsDatabaseReference.child(mUid).child("books").setValue(qty);
        mRequestsDatabaseReference.child(mUid).child("email").setValue(mAuth.getCurrentUser().getEmail());
        mRequestsDatabaseReference.child(mUid).child("clothes").setValue("NaN");

    }

    private void submitBoth(String qty, String minage, String maxage) {
        String mUid = mRequestsDatabaseReference.push().getKey();
        mRequestsDatabaseReference.child(mUid).child("books").setValue(qty);
        mRequestsDatabaseReference.child(mUid).child("email").setValue(mAuth.getCurrentUser().getEmail());
        mRequestsDatabaseReference.child(mUid).child("clothes").setValue(minage + " to " + maxage + " years");

    }
}
