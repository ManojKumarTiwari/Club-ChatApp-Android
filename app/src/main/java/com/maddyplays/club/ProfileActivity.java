package com.maddyplays.club;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendRequestBtn,mProfileDeclineBtn;

    private String mCurrentState;

    private DatabaseReference usersDatabase;
    private DatabaseReference friendReqDatabase;
    private DatabaseReference friendDatabase;
    private DatabaseReference notificationDatabase;

    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_diplay_name);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_total_friends);
        mProfileSendRequestBtn = (Button) findViewById(R.id.profile_send_request_btn);
        mProfileDeclineBtn = (Button) findViewById(R.id.profile_decline_request_btn);

        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
        mProfileDeclineBtn.setEnabled(false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentState = "not_friends";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Profile");
        mProgressDialog.setMessage("Please wait! while we load User Profile");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mRootRef = FirebaseDatabase.getInstance().getReference();

        final String user_id = getIntent().getStringExtra("user_id");
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        friendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friends_req");
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");

        usersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.photo).into(mProfileImage);

                //_ ______________________________________________FRIENDS LIST/ REQUEST FEATURE_________________________________________________________________
                friendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){
                            String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(request_type.equals("received")){

                                mCurrentState = "request_received";
                                mProfileSendRequestBtn.setText("Accept Friend Request");

                                mProfileDeclineBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineBtn.setEnabled(true);

                            }else if(request_type.equals("sent")){

                                mCurrentState = "request_sent";
                                mProfileSendRequestBtn.setText("Cancel Friend Request");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);

                            }

                            mProgressDialog.dismiss();

                        } else {

                            friendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){

                                        mCurrentState = "friends";
                                        mProfileSendRequestBtn.setText("Unfriend this User");

                                        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineBtn.setEnabled(false);

                                    }

                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendRequestBtn.setEnabled(false);

                // _ _____________________________________________NOT FRIENDS STATE____________________________________________________________________________________

                if(mCurrentState.equals("not_friends")){

                    DatabaseReference newNotificationref = mRootRef.child("Notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friends_req/"+ mCurrentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friends_req/"+ user_id + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_LONG).show();
                            }
                            mProfileSendRequestBtn.setEnabled(true);
                            mCurrentState = "request_sent";
                            mProfileSendRequestBtn.setText("Cancel Friend Request");
                        }
                    });

                }

                // _ _____________________________________________CANCEL REQUEST STATE____________________________________________________________________________________

                if(mCurrentState.equals("request_sent")){

                    friendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendRequestBtn.setEnabled(true);
                                    mCurrentState = "not_friends";
                                    mProfileSendRequestBtn.setText("Send Friend Request");

                                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineBtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }

                //_ ________________________________________________REQUEST RECEIVED STATE_____________________________________________________________________________________

                if(mCurrentState.equals("request_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date", currentDate);

                    friendsMap.put("Friends_req/" + mCurrentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friends_req/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mProfileSendRequestBtn.setEnabled(true);
                                mCurrentState = "friends";
                                mProfileSendRequestBtn.setText("Unfriend this User");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                                
                            } else {
                                
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }

                //_____________________________________________________UNFRIEND THIS USER_______________________________________________________________________

                if(mCurrentState.equals("friends")) {

                    Map unFriendMap = new HashMap();
                    unFriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id, null);
                    unFriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(unFriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mCurrentState = "not_friends";
                                mProfileSendRequestBtn.setText("Send Friend Request");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                            mProfileSendRequestBtn.setEnabled(true);

                        }
                    });
                }
            }

        });
    }
}
