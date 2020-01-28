package com.application.letschat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //views
    EditText mEmailEt , mPasswordEt;
    Button mRegisterBtn;
    TextView mHaveAccountTv;

    //Progressing to display while registering user

    ProgressDialog progressDialog;

    //Declare an instance of Firebase Auth
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");

        //enable Back Button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mHaveAccountTv = findViewById(R.id.have_accountTv);

        //In the onCreate() method, initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");


        //handle Register Button Click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 //input Email , password
                String email = mEmailEt.getText().toString().trim();
                String password= mPasswordEt.getText().toString().trim();

                //Validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

                    //set error and focuses to email Edittext
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);

                }
                else if (password.length()<6) {
                    //set error and focuses to password Edittext
                    mPasswordEt.setError("Password should be atleast 6 Characters long");
                    mPasswordEt.setFocusable(true);
                }
                else{

                    registerUser(email , password); //Register the User
                }
            }
        });
        //Handle Login textview Click Listener
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this , LoginActivity.class));
                finish();
            }
        });


    }

    private void registerUser(String email, String password) {
        //Email and Password Pattern is valid , show progress dialog and start registering user
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog and start Register acitivity
                            progressDialog.dismiss();

                            FirebaseUser user = mAuth.getCurrentUser();
                            //Get User Email and Uid from Auth
                            String email = user.getEmail();
                            String uid = user.getUid();
                            //When user is registered store user info inin firebase realtime database too
                            //Using Hashmap
                            HashMap<Object , String> hashMap = new HashMap<>();
                            //Put info in HashMap
                            hashMap.put("email" , email);
                            hashMap.put("uid" , uid);
                            hashMap.put("name" , ""); //Will add later (e.g Edit Profile)
                            hashMap.put("phone" , ""); //Will add later (e.g Edit Profile)
                            hashMap.put("image" , ""); //Will add later (e.g Edit Profile)
                            hashMap.put("cover" , ""); //Will add later (e.g Edit Profile)

                            //Firebase Database Instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();

                            //Path to store User Data named "Users"
                            DatabaseReference reference = database.getReference("Users");
                            //Put Data within Hashmap in database
                            reference.child(uid).setValue(hashMap);


                            Toast.makeText(RegisterActivity.this, "Registeration Complete ... \n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this , DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error , dismiss progress dialog and get and show the error message
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go previous Activity
        return super.onSupportNavigateUp();
    }
}
