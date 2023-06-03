package com.example.stockbar.ui.login;

import android.content.Context;

import com.example.stockbar.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class AuthUtils {

    private static GoogleSignInClient googleSignInClient;

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static GoogleSignInClient getSignInClient(Context context) {
        if (googleSignInClient == null)
            forceSignInClientRecreation(context);

        return googleSignInClient;
    }

    public static void forceSignInClientRecreation(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("631408971606-4avohpij34ql96oma3hu7qqamikc5no1.apps.googleusercontent.com")
                .requestServerAuthCode("631408971606-4avohpij34ql96oma3hu7qqamikc5no1.apps.googleusercontent.com")
                //.requestIdToken(context.getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public static FirebaseAuth getAuth() {
        return auth;
    }
}
