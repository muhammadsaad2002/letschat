package com.application.letschat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    //Firebase Auth
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //Actionbar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        //Bottom Navigation
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //home Fragment Transaction (default , On start )
        actionBar.setTitle("Home");  //Change actionbar Title
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content , fragment1 , "");
        ft1.commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    //Handle item Clicks
                    switch (menuItem.getItemId()) {

                        case R.id.nav_home:
                            //home Fragment Transaction
                            actionBar.setTitle("Home");  //Change actionbar Title
                            HomeFragment fragment1 = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content , fragment1 , "");
                            ft1.commit();
                            return true;

                        case R.id.nav_profile:
                            //Profile Fragment Transaction
                            actionBar.setTitle("Profile");  //Change actionbar Title
                            ProfileFragment fragment2 = new ProfileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content , fragment2 , "");
                            ft2.commit();
                            return true;

                        case R.id.nav_users:
                            //Users Fragment Transaction
                            actionBar.setTitle("Users");  //Change actionbar Title
                            UsersFragment fragment3 = new UsersFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content , fragment3 , "");
                            ft3.commit();
                            return true;

                    }

                    return false;
                }
            };

    private void checkUserStatus() {
        //get Current User
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user !=null) {
            //user is signed in stay here
            //Set Email of Logged in User
            //mProfileTv.setText(user.getEmail());

        }
        else {
            //User not signed in ,  go to main acitivity
            startActivity(new Intent(DashboardActivity.this , MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //Check on start of app
        checkUserStatus();
        super.onStart();
    }

    /*inflate Options menu*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflating Menu
        getMenuInflater().inflate(R.menu.menu_main ,  menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*handle Menu item Clicks*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Get item Id
        int id = item.getItemId();
        if (id == R.id.action_logout) {

            firebaseAuth.signOut();
            checkUserStatus();

        }

        return super.onOptionsItemSelected(item);
    }
}
