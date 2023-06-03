package com.example.stockbar.ui.login;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stockbar.MainActivity;

import com.example.stockbar.R;
import com.example.stockbar.services.DatabaseService;
import com.example.stockbar.services.users.UserInfo;
import com.example.stockbar.services.users.UsersManagerService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginFragment extends Fragment implements View.OnClickListener, UsersManagerService.UserListListener {

    private View root;
    private MainActivity activity;
    private SignInButton singInButton;
    private ProgressBar progressBar;

    private static final int SIGN_IN_REQUEST = 1;

    private UserInfo loggedUserInfo;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_login, container, false);

        activity = (MainActivity) getActivity();
        assert activity != null;

        singInButton = root.findViewById(R.id.sign_in_button);
        singInButton.setVisibility(View.GONE);
        progressBar = root.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        if (AuthUtils.getAuth().getCurrentUser() == null) {
            Log.i("SIGN", "Firebase not logged in. Requesting Google account");

            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
            if (account != null && account.isExpired()) {
                Log.i("SIGN", "Google account not expired");
                onGoogleAccount(account);
            } else {
                Log.i("SIGN", "Google account already expired");

                singInButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        } else {
            Log.i("SIGN", "Firebase user already logged in");

            onFirebaseUser();
        }

        activity.setDrawerVisibility(MainActivity.Visibility.HIDDEN);
        activity.setSearchBarVisibility(MainActivity.Visibility.HIDDEN);
        activity.setHomeButtonVisibility(MainActivity.Visibility.HIDDEN);

        singInButton.setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View v) {
        Intent signInIntent = AuthUtils.getSignInClient(activity).getSignInIntent();
        startActivityForResult(signInIntent, SIGN_IN_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                root.findViewById(R.id.no_access_text).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.profile_email).setVisibility(View.INVISIBLE);
                onGoogleAccount(account);
            } catch (ApiException ex) {
                String text = "Error accediendo a cuenta de Google";

                switch (ex.getStatusCode()) {
                    case 17: //  API_NOT_CONNECTED
                        text += ": no se puede conectar con la API.";
                        break;
                    case 16: //  CANCELED
                        text += ": proceso cancelado.";
                        break;
                    case 20: //  CONNECTION_SUSPENDED_DURING_CALL
                        text += ": pérdida de conexión.";
                        break;
                    case 7: //  NETWORK_ERROR
                        text += ": error de conexión.";
                        break;
                    case 15: //  TIMEOUT
                    case 22: //  RECONNECTION_TIMED_OUT
                    case 21: //  RECONNECTION_TIMED_OUT_DURING_UPDATE
                        text += ": timed out.";
                        break;
                    case 19: //  REMOTE_EXCEPTION
                        text += ": excepción remota.";
                        break;
                    case 10: //  DEVELOPER_ERROR
                        text += ": error de desarrollador.";
                        break;
                    case 13: //  ERROR
                        break;
                    case 8: //  INTERNAL_ERROR
                        text += ": error interno.";
                        break;
                    case 14: //  INTERRUPTED
                        break;
                    case 5: //  INVALID_ACCOUNT
                        text += ": cuenta inválida.";
                        break;
                }

                if (ex.getStatusCode() == 10) {
                    // Error de desarrollador. La hemos cagado.
                    // Sign out from Google and Firebase Auth y recrea el SignInClient
                    AuthUtils.getSignInClient(activity).signOut().addOnCompleteListener(singOutTask -> {
                        AuthUtils.getAuth().signOut();
                        AuthUtils.forceSignInClientRecreation(activity);
                    });
                }

                Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void onGoogleAccount(@NonNull GoogleSignInAccount account) {
        Log.i("SIGN", "onGoogleAccount: " + account.getIdToken());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        AuthUtils.getAuth().signInWithCredential(credential)
            .addOnCompleteListener(activity, task -> {
                if (task.isSuccessful()) {
                    singInButton.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);

                    onFirebaseUser();
                } else {
                    singInButton.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(activity, "Error confirmando cuenta de Google", Toast.LENGTH_LONG).show();

                    // Sign out so that we can sign in with other account.
                    singInButton.setClickable(false);
                    AuthUtils.getSignInClient(activity).signOut().addOnCompleteListener(singOutTask -> {
                        // Only let the user sign in after we signed out
                        singInButton.setClickable(true);
                    });
                }
            });
    }

    private void onFirebaseUser() {
        FirebaseUser loggedUser = AuthUtils.getAuth().getCurrentUser();
        assert loggedUser != null;

        UsersManagerService.getUserInfoOrCreate(loggedUser, userInfo -> {
            assert userInfo != null;

            this.loggedUserInfo = userInfo;

            root.findViewById(R.id.progress_bar).setVisibility(View.GONE);
            if (userInfo.hasAccess()) {
                Log.i("SIGN", "Firebase user has access to application");

                onUserWithAccess();
            } else {
                Log.i("SIGN", "Firebase user doesn't have access to application. Waiting..");

                root.findViewById(R.id.no_access_text).setVisibility(View.VISIBLE);

                root.findViewById(R.id.profile_email).setVisibility(View.VISIBLE);
                ((TextView) root.findViewById(R.id.profile_email)).setText(loggedUser.getEmail());

                singInButton.setVisibility(View.VISIBLE);
                ((TextView) singInButton.getChildAt(0)).setText("Cambiar Sesión");

                singInButton.setOnClickListener(v -> AuthUtils.getSignInClient(activity).signOut().addOnCompleteListener(task -> this.onClick(v)));

                UsersManagerService.setReference(this);
            }
        });
    }

    private void onUserWithAccess() {
        UsersManagerService.removeReference(this);
        activity.setUserInfo(loggedUserInfo);
        Navigation.findNavController(root).navigate(R.id.action_nav_login);
    }

    @Override
    public void onUserInfo(UserInfo newUserInfo) {
        onUserInfoChanged(newUserInfo);
    }

    @Override
    public void onUserInfoChanged(UserInfo newUserInfo) {
        if (newUserInfo.getUid().equals(this.loggedUserInfo.getUid()) && newUserInfo.hasAccess()) {
            this.loggedUserInfo = newUserInfo;
            onUserWithAccess();
        }
    }
}