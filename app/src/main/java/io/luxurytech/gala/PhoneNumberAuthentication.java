package io.luxurytech.gala;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

/** Verifies if user is already signed in with phone. If not, let's them sign up. */
public class PhoneNumberAuthentication extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            // already signed in - check if other fields are complete
            DocumentReference drUser;
            FirebaseFirestore db;
            String uid = null;
            FirebaseUser authUser;

            db = FirebaseFirestore.getInstance();
            authUser = auth.getCurrentUser();
            if(authUser != null) {
                uid = authUser.getUid();
            }
            drUser = db.collection("Users").document(uid);

            // Check if user already has full user information
            // If so, skip to home
            drUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot doc = task.getResult();
                        if(doc.get("desiredGender") != ""  && doc.get("desiredGender") != null &&
                                doc.get("desiredMaxAge") != ""  && doc.get("desiredMaxAge") != null &&
                                doc.get("desiredMinAge") != ""  && doc.get("desiredMinAge") != null &&
                                doc.get("recoveryEmail") != ""  && doc.get("recoveryEmail") != null &&
                                doc.get("screenName") != ""  && doc.get("screenName") != null &&
                                doc.get("userAge") != ""  && doc.get("userAge") != null &&
                                doc.get("userGender") != ""  && doc.get("userGender") != null){
                            startActivity(new Intent(PhoneNumberAuthentication.this, HomeActivity.class));
                            finish();
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });



            // If not, go thru process
            startActivity(new Intent(PhoneNumberAuthentication.this, RecoveryEmail.class));
            finish();
        } else {
            // not signed in
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(
                                    Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()
                                    ))
                            .build(),
                    RC_SIGN_IN);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                startActivity(new Intent(PhoneNumberAuthentication.this, RecoveryEmail.class));
                finish();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Log.e("Login","Login canceled by User");
                    return;
                }
                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Log.e("Login","No Internet Connection");
                    return;
                }
                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Log.e("Login","Unknown Error");
                    return;
                }
            }
            Log.e("Login","Unknown sign in response");
        }
    }
}
