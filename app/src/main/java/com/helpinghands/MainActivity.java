package com.helpinghands;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.donor_button)
    Button mDonorButton;

    @BindView(R.id.receiver_button)
    Button mReceiverButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mDonorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate("donor");
            }
        });

        mReceiverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate("receiver");
            }
        });


    }

    public void navigate(String type) {
        Intent intent = new Intent(MainActivity.this, LoginSignupActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);
    }
}

