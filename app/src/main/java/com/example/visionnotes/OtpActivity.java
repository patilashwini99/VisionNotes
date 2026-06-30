package com.example.visionnotes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneAuthOptions;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    TextInputEditText etOtp;
    MaterialButton btnVerify;
    TextView tvResend;

    String verificationId;
    String phoneNumber;

    FirebaseAuth mAuth;
    PhoneAuthProvider.ForceResendingToken resendToken;

    // ✅ IMPORTANT: callbacks CLASS LEVEL ला ठेव
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    mAuth.signInWithCredential(credential);
                }

                @Override
                public void onVerificationFailed(com.google.firebase.FirebaseException e) {
                    Toast.makeText(OtpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(String verificationId,
                                       PhoneAuthProvider.ForceResendingToken token) {

                    // 🔥 update new values
                    OtpActivity.this.verificationId = verificationId;
                    resendToken = token;

                    Toast.makeText(OtpActivity.this, "OTP Sent Again ✅", Toast.LENGTH_SHORT).show();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        etOtp = findViewById(R.id.etOtp);
        btnVerify = findViewById(R.id.btnVerify);
        tvResend = findViewById(R.id.tvResend);

        mAuth = FirebaseAuth.getInstance();

        // 🔥 data get from PhoneActivity
        verificationId = getIntent().getStringExtra("verificationId");
        phoneNumber = getIntent().getStringExtra("phone");
        resendToken = getIntent().getParcelableExtra("resendToken");

        // ✅ RESEND
        tvResend.setOnClickListener(v -> {
            resendOtp(phoneNumber);
        });

        // ✅ VERIFY
        btnVerify.setOnClickListener(v -> {

            String otp = etOtp.getText().toString().trim();

            if (otp.isEmpty() || otp.length() < 6) {
                etOtp.setError("Enter valid OTP");
                return;
            }

            PhoneAuthCredential credential =
                    PhoneAuthProvider.getCredential(verificationId, otp);

            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            Toast.makeText(this, "Login Successful ✅", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(this, HomeActivity.class));
                            finish();

                        } else {
                            Toast.makeText(this, "Invalid OTP ❌", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // ✅ RESEND FUNCTION
    private void resendOtp(String phoneNumber) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91" + phoneNumber)
                        .setTimeout(3L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(resendToken)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}