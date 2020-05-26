package edu.sjsu.android.design;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;


public class MainActivity extends AppCompatActivity
{
    Button trackWorkoutButton, findActivityButton;
    Toolbar toolbar;
    private static final String TAG = "SignIn";
    SignInButton sign_in_button;
    Button sign_out_button;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        account = GoogleSignIn.getLastSignedInAccount(this);
        sign_in_button = findViewById(R.id.sign_in_button);
        sign_out_button = findViewById(R.id.sign_out_button);
        trackWorkoutButton = findViewById(R.id.button);//get id of button 1
        findActivityButton = findViewById(R.id.button2);//get id of button 2
        trackWorkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(account == null){
                    signIn();
                }
                else{
                    Intent intent = new Intent(view.getContext(), ActivityLog.class);
                    view.getContext().startActivity(intent);
                }
            }
        });

        findActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), FindActivity.class);
                view.getContext().startActivity(intent);
            }
        });

        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                signIn();
            }
        });

        sign_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });





        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Fitness.SCOPE_ACTIVITY_READ_WRITE)
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id))
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        updateUI(GoogleSignIn.getLastSignedInAccount(this));
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try
        {
            account = completedTask.getResult(ApiException.class);
            updateUI(account);
            if(account != null)
            {
                String idToken = account.getIdToken();
                Log.d(TAG, "Token value: " + idToken);
            }
        }
        catch (ApiException e)
        {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }

    }



    private void signOut() {
        account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null)
        {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            account = null;
                            // ...
                        }
                    });
            Toast toast = Toast.makeText(this, account.getEmail() + "is signed out", Toast.LENGTH_SHORT);
            toast.show();
            updateUI(null);
        }

    }

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null)
        {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
        } else
        {
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
    }
}

