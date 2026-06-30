package com.example.visionnotes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText email, password;
    private MaterialButton loginBtn, googleBtn, phoneBtn;
    private TextView goRegister, forgotPassword;

    private FirebaseAuth auth;

    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        // 🔥 AUTO LOGIN CHECK (NEW ADD)
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        // Views
        email = findViewById(R.id.username);
        password = findViewById(R.id.password);

        loginBtn = findViewById(R.id.login);
        googleBtn = findViewById(R.id.btnGoogle);
        phoneBtn = findViewById(R.id.btnPhone);

        goRegister = findViewById(R.id.goRegister);
        forgotPassword = findViewById(R.id.forgotPassword);

        // LOGIN
        loginBtn.setOnClickListener(v -> loginUser());

        // REGISTER
        goRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        // FORGOT PASSWORD
        forgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class))
        );

        // PHONE
        phoneBtn.setOnClickListener(v ->
                startActivity(new Intent(this, PhoneActivity.class))
        );

        // GOOGLE LOGIN
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleBtn.setOnClickListener(v -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
        });
    }

    private void loginUser() {
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();

        if (mail.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login Successful ✔", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this, HomeActivity.class));
                        finish();

                    } else {
                        Toast.makeText(this,
                                "Login Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.e("GOOGLE_LOGIN", "Error: " + e.getStatusCode());
                Toast.makeText(this, "Google SignIn Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential =
                GoogleAuthProvider.getCredential(account.getIdToken(), null);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = auth.getCurrentUser();

                        Toast.makeText(this,
                                "Welcome " + user.getDisplayName(),
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this, HomeActivity.class));
                        finish();

                    } else {
                        Toast.makeText(this,
                                "Google Auth Failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}