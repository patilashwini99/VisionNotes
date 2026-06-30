package com.example.visionnotes;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    TextInputEditText email;
    MaterialButton resetBtn;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        email = findViewById(R.id.emailen);
        resetBtn = findViewById(R.id.resetbtn1);

        auth = FirebaseAuth.getInstance();

        resetBtn.setOnClickListener(v -> {

            String mail = email.getText() != null
                    ? email.getText().toString().trim()
                    : "";

            if (TextUtils.isEmpty(mail)) {
                email.setError("Enter email");
                return;
            }

            auth.sendPasswordResetEmail(mail)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Reset email sent ✔", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed ❌", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}