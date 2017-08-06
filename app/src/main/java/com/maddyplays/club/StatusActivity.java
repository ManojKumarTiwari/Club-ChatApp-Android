package com.maddyplays.club;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    //Toolbar
    private Toolbar mToolbar;

    //Layout
    private TextInputLayout mStatusInput;
    private Button mSaveChangesBtn;

    //Firebase
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    //Progress dailog
    private ProgressDialog mstatusProgressDailog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mStatusInput = (TextInputLayout) findViewById(R.id.status_input);
        mSaveChangesBtn = (Button) findViewById(R.id.status_save_btn);

        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Club Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra("status_value");
        mStatusInput.getEditText().setText(status_value);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = currentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mSaveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mstatusProgressDailog = new ProgressDialog(StatusActivity.this);

                mstatusProgressDailog.setTitle("Saving changes");
                mstatusProgressDailog.setMessage("Please wait while we save your changes");
                mstatusProgressDailog.show();

                String status = mStatusInput.getEditText().getText().toString();
                mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            mstatusProgressDailog.dismiss();

                        } else {

                            Toast.makeText(StatusActivity.this, "There was some error while saving changes.", Toast.LENGTH_LONG).show();

                        }
                    }
                });

            }
        });

    }
}
