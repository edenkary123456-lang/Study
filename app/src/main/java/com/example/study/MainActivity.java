package com.example.study;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText Name, Email, Password;
    private TextView Status;
    private Button Login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Name = findViewById(R.id.editTextText);
        Email = findViewById(R.id.editTextTextEmailAddress);
        Password = findViewById(R.id.editTextNumberPassword);
        Status = findViewById(R.id.textView);
        Login = findViewById(R.id.button);

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });
    }

    public void createUser() {
        String emailStr = Email.getText().toString().trim();
        String passwordStr = Password.getText().toString().trim();
        String nameStr = Name.getText().toString().trim();

        if (emailStr.isEmpty() || passwordStr.isEmpty() || nameStr.isEmpty()) {
            Status.setText(" fill all fields");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Creating user");
        pd.show();
        FBReF.auth.createUserWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                            Log.i("MainActivity", "createUser:success");
                            FirebaseUser user = FBReF.auth.getCurrentUser();
                            Status.setText("Success User ID " + user.getUid());
                        } else {
                            Log.e("MainActivity", "createUser:failure", task.getException());
                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthWeakPasswordException) {
                                Status.setText("Password weak you need min 6 cher");
                            } else if (e instanceof FirebaseAuthUserCollisionException) {
                                Status.setText("Email already in ues");
                            } else if (e instanceof FirebaseNetworkException) {
                                Status.setText("No internet connection");
                            } else {
                                Status.setText("Error" + e.getMessage());
                            }
                        }
                    }
                });
    }
}