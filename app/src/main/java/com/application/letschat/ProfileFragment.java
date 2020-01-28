package com.application.letschat;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.net.Inet4Address;
import java.security.Key;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //Firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //Storage
    StorageReference storageReference;
    //Path where images of user profile and cover will be stored
    String storagePath =  "Users_Profile_Cover_Imgs/*";


    //Views from XML
    ImageView avatarIv , coverIv;
    TextView nameTv , emailTv , phoneTv ;
    FloatingActionButton fab;


    //Progress Dialog
    ProgressDialog pd;

    //Permission Constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //Arrays of permissions to be request
    String cameraPermission[];
    String storagePermissions[];

    //URI of picked image
    Uri image_uri;

    //For checking profile or cover photo
    String profileOrCoverPhoto;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //init Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference(); //Firebase Storage Reference

        //Init Arrays of Permissions
        cameraPermission = new String[]{Manifest.permission.CAMERA , Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init Views
        avatarIv = view.findViewById(R.id.avatarIv);
        coverIv = view.findViewById(R.id.coverIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        fab = view.findViewById(R.id.fab);

        //Init Progress Dialog
        pd = new ProgressDialog(getActivity());



        /*We have to get info of currently signed in user. We can only get it by using users's email or uid*/
        /*By using OrderByChild query we will show the detail from a node whose named email has value equal to currently signed in Email.
        it will search all the nodes, where the key matches. It will get its detail */

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Check until we get required data
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    //Get data
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String phone = ""+ ds.child("phone").getValue();
                    String image = ""+ ds.child("image").getValue();
                    String cover = ""+ ds.child("cover").getValue();

                    //Set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        //If image is received then set
                        Picasso.get().load(image).into(avatarIv);

                    }
                    catch (Exception e) {
                        //If there is any exception while getting any image then set default
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
                    }

                    try {
                        //If image is received then set
                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e) {
                        //If there is any exception while getting any image then set default

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //Fab Button Click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();

            }
        });


        return view;
    }

    private boolean checkStoragePermission(){
        //Check if storage permission is enabled or not
        //Return True if enabled
        //Return false if not
        boolean result = ContextCompat.checkSelfPermission(getActivity() , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){

        //Request runtme storage permission
        requestPermissions(storagePermissions ,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        //Check if storage permission is enabled or not
        //Return True if enabled
        //Return false if not
        boolean result = ContextCompat.checkSelfPermission(getActivity() , Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity() , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission(){

        //Request runtme storage permission
        requestPermissions(cameraPermission ,CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        /*Show Dialog Containing Option
        * 1)  Edit Profile Picture
        * 2) Edit Cover Photo
        * 3) Edit Name
        * 4) Edit Phone
         */

        //Options to Show in dialog
        String options[] = {"Edit Profile Picture" , "Edit Cover Photo" , "Edit Name" , "Edit Phone"};

        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set Title
        builder.setTitle("Choose Action");

        //Set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Handle dialog item clicks
                if (which == 0){
                    //Edit Profile Clicked
                    pd.setMessage("Updating Profile Picture...");
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();
                }
                else if (which == 1){
                    //Edit Cover Clicked
                    pd.setMessage("Updating Cover Photo...");
                    profileOrCoverPhoto = "cover";
                    showImagePicDialog();
                }
                else if (which == 2){
                    //Edit name Clicked
                    pd.setMessage("Updating Name...");
                    //Calling method and pass key "name" as parameter to update it's value in database
                    showNamePhoneUpdatedDialog("name");
                }
                else if (which == 3){
                    //Edit phone Clicked
                    pd.setMessage("Updating Phone...");
                    showNamePhoneUpdatedDialog("phone");
                }

            }
        });

        //Create and Show dialog
        builder.create().show();


    }

    private void showNamePhoneUpdatedDialog(final String key) {
        //Custom Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update"+ key); //Update name or phone
        //Set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10 ,10 ,10 ,10);

        //Add Edit Text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter"+ key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //Add buttons in dialog to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Input Text from Edit Text
                String value = editText.getText().toString().trim();
                //Validate if user has entered something or not
                if (!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String , Object> result = new HashMap<>();
                    result.put(key , value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Updated , dismiss Progress
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Failed , dismiss progress , get and show error message
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
                else {
                    Toast.makeText(getActivity(), "Please Enter"+ key, Toast.LENGTH_SHORT).show();

                }

            }
        });
        //Add buttons in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //Create and show dialog
        builder.create().show();

    }

    private void showImagePicDialog() {
        //Show options camera and Gallery to pick the image (Dialog Box)

        //Options to Show in dialog
        String options[] = {"Camera" , "Gallery"};

        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set Title
        builder.setTitle("Pick Image From");

        //Set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Handle dialog item clicks
                if (which == 0){
                    //Camera Clicked
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                else if (which == 1){
                    //Gallery Clicked
                    if (!checkStoragePermission()){

                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }

                }

            }
        });

        //Create and Show dialog
        builder.create().show();

        /*Picking image from
        *Camera(Camera and Storage Permission Required)
        * Gallery (Storage Permission Required) */


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*This method is called when user press Aloow or deny from permission request dialog
        *Handle permission case (Allowed or denied)
         */
        switch (requestCode) {

            case CAMERA_REQUEST_CODE:{
                //Picking from Camera first check if camera and storage permissions allowed or not
                if (grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        //Permissions enabled
                        pickFromCamera();

                    }
                    else {
                        //Permissions Denied
                        Toast.makeText(getActivity() , "Please enable camera & storage permission ", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{
                //Picking from gallery first check if camera and storage permissions allowed or not
                if (grantResults.length >0){
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        //Permissions enabled
                        pickFromGallery();
                    }
                    else {
                        //Permissions Denied
                        Toast.makeText(getActivity() , "Please enable Storage permission ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*This method will be called after picking of image from Camera or Gallery */
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //Image is picked from the gallery  , get uri of the image
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //Image is picked from the camera  , get uri of the image
                
                uploadProfileCoverPhoto(image_uri);

            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        //Show Progress
        pd.show();


        /*Instead of creating separate function for Profile Picture and Cover Photo(doing in same function)
        *Add a string variable and assign it value "image" when user clicks
        * "Edit Profile Pic" , and assign it value "cover" when user clicks "Edit Cover Photo"
        * Image is the key in each user containing uri of user's profile picture
        * cover is the key in each user containing uri of user's cover photo
         */

        /*The paramente "image_uri" conatins the uri of the image picked either from camera or gallery*/
        //Path and name of image to be stored in firebase storage
        String filePathAndName = storagePath+ ""+ profileOrCoverPhoto +"_"+ user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Image is uploaded to storage , now get its url and store in user's database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //Check if image is uploaded or not and url is received
                        if (uriTask.isSuccessful()){
                            //Image uploaded
                            //Add/update url in user's database
                            HashMap<String , Object> results = new HashMap<>();
                            results.put(profileOrCoverPhoto , downloadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //URL in database of user is added successfully
                                            //Dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error adding URL in databse of User
                                            //Dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error Updating Image ...", Toast.LENGTH_SHORT).show();


                                        }
                                    });

                        }
                        else {
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Some Error has occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //There were some error(s) , get and show error message , dismiss progress dialog
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void pickFromCamera() {
        //Intent of picking imgae from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE , "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION , "Temp Description");

        //Put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , values);

        //Intent to Start Camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT , image_uri);
        startActivityForResult(cameraIntent , IMAGE_PICK_CAMERA_CODE);

    }

    private void pickFromGallery() {
        //Pick from Gallery
        Intent galleryIntent =  new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent , IMAGE_PICK_GALLERY_CODE);

    }

}
