package chinna.sandyz.com.facebookandgooglelogin;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;


public class MainFragment extends Fragment implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{

    LoginButton loginButton;
    CallbackManager callbackManager;
    ProgressDialog progressDialog;
    TextView first_name, last_name, id, email, pro_pic_path, gender;
    GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;
    private static final int RC_SIGN_IN = 9001;


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment, container, false);
        first_name=(TextView)view.findViewById(R.id.first_name);
        last_name= (TextView)view.findViewById(R.id.last_name);
        id= (TextView)view.findViewById(R.id.id);
        email=(TextView)view.findViewById(R.id.email);
        pro_pic_path=(TextView)view.findViewById(R.id.profil_pic_path);
        gender= (TextView)view.findViewById(R.id.gender);
        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        // If using in a fragment
        loginButton.setFragment(this);
        // Other app specific specialization
        loginButton.setOnClickListener(this);

        //read permisiom
        loginButton.setReadPermissions(Arrays.asList("user_status"));
        // Callback registration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
//                .enableAutoManage( (FragmentActivity) getActivity()/* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        signInButton = (SignInButton) view.findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(this);

        return view;
    }

   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
    }

    private Bundle getFacebookData(JSONObject object) {

        Bundle bundle = null;
        try {
            progressDialog.dismiss();
            bundle = new Bundle();
            String id = object.getString("id");

            try {
                URL profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?width=200&height=150");
                Log.i("profile_pic", profile_pic + "");
                pro_pic_path.setText("Profile Pic Path: "+ profile_pic);
                bundle.putString("profile_pic", profile_pic.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            bundle.putString("idFacebook", id);
            if (object.has("first_name"))
                bundle.putString("first_name", object.getString("first_name"));
            if (object.has("last_name"))
                bundle.putString("last_name", object.getString("last_name"));
            if (object.has("email"))
                bundle.putString("email", object.getString("email"));
            if (object.has("gender"))
                bundle.putString("gender", object.getString("gender"));
            if (object.has("birthday"))
                bundle.putString("birthday", object.getString("birthday"));
            if (object.has("location"))
                bundle.putString("location", object.getJSONObject("location").getString("name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bundle;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("Social Login", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            first_name.setText("Display Name: "+ acct.getDisplayName());
            last_name.setText("Given Name: "+acct.getGivenName());
            email.setText("Email: "+acct.getEmail());
            pro_pic_path.setText("Profile Pic Path: "+acct.getPhotoUrl()+"");
            id.setText("Id: "+acct.getId());
            gender.setText("Family Name: "+acct.getFamilyName());
        } else {
            // Signed out, show unauthenticated UI.
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.login_button:
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email","public_profile"));
                loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Log.e("success","onSuccess");
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage("Procesando datos...");
                        progressDialog.show();
                        String accessToken = loginResult.getAccessToken().getToken();
                        Log.i("accessToken", accessToken);

                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.i("LoginActivity", response.toString());
                                // Get facebook data from login
                                Bundle bFacebookData = getFacebookData(object);
                                first_name.setText("First Name: "+ bFacebookData.getString("first_name"));
                                last_name.setText("Last Name: "+ bFacebookData.getString("last_name"));
                                email.setText("Email: "+ bFacebookData.getString("email"));
                                id.setText("Id: "+ bFacebookData.getString("id"));
                                gender.setText("Gender: "+ bFacebookData.getString("gender"));
                            }
                        });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location"); // Parámetros que pedimos a facebook
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        System.out.println("onCancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        System.out.println("onError");
                        Log.v("LoginActivity", exception.getCause().toString());
                    }
                });
            break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
