package com.example.visionnotes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText email, password, repassword;
    Button register, goLogin;

    FirebaseAuth auth;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.regUsername);
        password = findViewById(R.id.regPassword);
        repassword = findViewById(R.id.regRePassword);
        register = findViewById(R.id.btnRegister);
        goLogin = findViewById(R.id.btnGoLogin);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("users");

        register.setOnClickListener(v -> {

            String mail = email.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String repass = repassword.getText().toString().trim();

            if (mail.isEmpty() || pass.isEmpty() || repass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(repass)) {
                Toast.makeText(this, "Passwords not matching", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pass.length() < 6) {
                Toast.makeText(this, "Password min 6 chars", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            String uid = auth.getCurrentUser().getUid();

                            HashMap<String, Object> user = new HashMap<>();
                            user.put("email", mail);
                            user.put("createdAt", System.currentTimeMillis());

                            database.child(uid).setValue(user);

                            Toast.makeText(this, "Registered Successfully ✔", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(this, LoginActivity.class));
                            finish();

                        } else {
                            Toast.makeText(this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        goLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

}