package com.mkchaudh.nnataraj.orangeftp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mkchaudh.nnataraj.orangeftp.data.FirebaseHelper;

public class LoginActivity extends AppCompatActivity {

    private static final String CREATE_ACCOUNT_ERROR = "Unable to create account";
    private static final String SIGN_IN_ERROR = "Unable to sign in";

    private void proceed() {
        FirebaseHelper.set(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            proceed();

        findViewById(R.id.createAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                String email = ((TextView) findViewById(R.id.editTextEmail)).getText().toString();
                String password = ((TextView) findViewById(R.id.editTextPassword)).getText().toString();

                if (email == null || password == null || email == "" || password == "")
                    return;


                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                            proceed();
                        else
                            Snackbar.make(v, CREATE_ACCOUNT_ERROR, Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });

        findViewById(R.id.signIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                String email = ((TextView) findViewById(R.id.editTextEmail)).getText().toString();
                String password = ((TextView) findViewById(R.id.editTextPassword)).getText().toString();

                if (email != null && !(email == "") && password != null && !(password == ""))
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                                proceed();
                            else
                                Snackbar.make(v, SIGN_IN_ERROR, Snackbar.LENGTH_LONG).show();
                        }
                    });
            }
        });


    }

}
