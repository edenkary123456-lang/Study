package com.example.study;
import static com.example.study.FBReF.refAuth;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import android.widget.TextView;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    private EditText name;
    private EditText password;
    private String  email;
    public TextView textview;
    public Button login;
    public void createUser(View view) {
        String email=((EditText)findViewById(R.id.Email)).getText().toString();
        String password=((EditText)findViewById(R.id.Password)).getText().toString();
        String name=((EditText)findViewById(R.id.name)).getText().toString();
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            textview.setText("Please fill all fields");
        } else {
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Please wait");
            pd.show();
            refAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    pd.dismiss();
                    if (task.isSuccessful()) {
                        Log.i("MainActivity", "createUserWithEmail:success");
                        FirebaseUser user = refAuth.getCurrentUser();
                        textview.setText("User created"+ user.getUid());
                    }else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthInvalidUserException){
                            textview.setText("Invalid email");
                        }else if (e instanceof FirebaseAuthWeakPasswordException){
                            textview.setText("Weak password");
                        }else if (e instanceof FirebaseAuthUserCollisionException) {
                            textview.setText("User already exists");
                        }else if (e instanceof FirebaseAuthInvalidCredentialsException){
                            textview.setText("Invalid credentials");
                        }else if (e instanceof FirebaseNetworkException){
                            textview.setText("No internet connection");
                        }
                        else {
                            textview.setText("Authentication failed");

                        }
                    }
                }

            }
        ;}
    }
}
