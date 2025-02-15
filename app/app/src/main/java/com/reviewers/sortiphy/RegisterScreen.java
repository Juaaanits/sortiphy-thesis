package com.reviewers.sortiphy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailEditText, passwordEditText, confirmPasswordEditText, positionEditText, usernameEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.email_field);
        usernameEditText = findViewById(R.id.username_field);
        positionEditText = findViewById(R.id.position_field);
        passwordEditText = findViewById(R.id.password_field);
        confirmPasswordEditText = findViewById(R.id.password_field2);
        registerButton = findViewById(R.id.login_confirm_button);

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String position = positionEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || username.isEmpty() || position.isEmpty()) {
            Toast.makeText(RegisterScreen.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), username, email, position);
                        }
                    } else {
                        Toast.makeText(RegisterScreen.this, "Registration Failed!", Toast.LENGTH_SHORT).show();
                        Log.e("FirebaseAuth", "Error: ", task.getException());
                    }
                });


    }

    private void saveUserToFirestore(String userId, String username, String email, String position) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("createdAt", System.currentTimeMillis());
        user.put("position", position);

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterScreen.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterScreen.this, LoginScreen.class));
                    finish();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error writing user", e));
    }
}
