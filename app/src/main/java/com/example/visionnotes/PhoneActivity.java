package com.example.visionnotes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneActivity extends AppCompatActivity {

    TextInputEditText phone;
    MaterialButton btnSendOtp;
    FirebaseAuth auth;

    String mobileNumber; // 🔥 important

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        phone = findViewById(R.id.phone);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        auth = FirebaseAuth.getInstance();

        btnSendOtp.setOnClickListener(v -> {

            mobileNumber = phone.getText().toString().trim();

            if (mobileNumber.isEmpty() || mobileNumber.length() < 10) {
                phone.setError("Enter valid number");
                return;
            }

            // 🔥 ADD HERE
            Toast.makeText(this, "Please wait... Sending OTP", Toast.LENGTH_SHORT).show();

            sendOtp(mobileNumber);
        });
    }

    private void sendOtp(String number) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber("+91" + number)
                        .setTimeout(3L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    Toast.makeText(PhoneActivity.this, "Auto verification done", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(PhoneActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(String verificationId,
                                       PhoneAuthProvider.ForceResendingToken token) {

                    Intent intent = new Intent(PhoneActivity.this, OtpActivity.class);
                    intent.putExtra("verificationId", verificationId);
                    intent.putExtra("phone", mobileNumber); // 🔥 pass number
                    intent.putExtra("resendToken", token);
                    startActivity(intent);
                }
            };

    private void signIn(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    }
                });
    }
}