package playeazy.com.gurpreet;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;

import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;


import retrofit2.Call;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    Toolbar toolbar;
    private CheckBox signIncheckBox;
    private CheckBox newUserCheckBox;
    private CheckBox showPasswordCheckBox;
    private EditText email;
    private EditText password;
    private Button submitButton;
    private Button forgotPasswordButton;
    private Button faceBookButton;
    private Button twitterButton;
    private TextView toolbartitle;
    private EditText nameEntry;
    CallbackManager callbackManager;
    LoginManager loginManager;
    private TwitterAuthClient twitterAuthClient;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.signin_layout);

        toolbar = (Toolbar) findViewById(R.id.app_bar1);
        setSupportActionBar(toolbar);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        toolbartitle = (TextView) findViewById(R.id.toolbar_title);
        toolbartitle.setText("Sign In");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }

        signIncheckBox = (CheckBox) findViewById(R.id.checkBox2);
        newUserCheckBox = (CheckBox) findViewById(R.id.checkBox3);
        showPasswordCheckBox = (CheckBox) findViewById(R.id.checkBox4);
        email = (EditText) findViewById(R.id.editText);
        password = (EditText) findViewById(R.id.editText3);
        submitButton= (Button) findViewById(R.id.button15);
        forgotPasswordButton= (Button) findViewById(R.id.button11);
        faceBookButton = (Button) findViewById(R.id.button_facebook_1);
        twitterButton= (Button) findViewById(R.id.button_twitter_1);
        nameEntry = findViewById(R.id.editText5);
        email.setEnabled(false);
        password.setEnabled(false);
        nameEntry.setEnabled(false);

        callbackManager = CallbackManager.Factory.create();
        loginManager= com.facebook.login.LoginManager.getInstance();

        twitterAuthClient = new TwitterAuthClient();

        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithTwitter();
            }
        });

        faceBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginManager.logInWithReadPermissions(LoginActivity.this, Arrays.asList("email","public_profile"));
                loginWithFacebook();
            }
        });

        signIncheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((CheckBox)v).isChecked()){
                    nameEntry.setVisibility(View.GONE);
                    email.setEnabled(true);
                    password.setEnabled(true);
                    newUserCheckBox.setChecked(false);
                }else {
                    email.setEnabled(false);
                    password.setEnabled(false);
                }
            }
        });

        newUserCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(((CheckBox)v).isChecked()){
                    nameEntry.setVisibility(View.VISIBLE);
                    nameEntry.setEnabled(true);
                    email.setEnabled(true);
                    password.setEnabled(true);
                    signIncheckBox.setChecked(false);
                 }else{
                    nameEntry.setEnabled(false);
                    email.setEnabled(false);
                    password.setEnabled(false);
                }
            }
        });

        showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    }else {
                        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }
            });

        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this,ResetPasswordActivity.class);
                startActivity(i);
            }

        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result to the login button.
        twitterAuthClient.onActivityResult(requestCode, resultCode, data);
    }


    private void loginWithTwitter() {


       twitterAuthClient.authorize(LoginActivity.this,new Callback<TwitterSession>() {
           @Override
           public void success(Result<TwitterSession> result) {

               TwitterSession session = result.data;

               Call<User>  userCall= TwitterCore.getInstance().getApiClient().getAccountService().
                       verifyCredentials(true,false,true);

               userCall.enqueue(new Callback<User>() {
                   @Override
                   public void success(Result<User> result) {

                       String email = result.data.email;
                       String name = result.data.name;
                       int friendsCount=result.data.friendsCount;

                      Log.d(TAG,"yahoo   "+email+" "+name+ " "+friendsCount);
                   }

                   @Override
                   public void failure(TwitterException exception) {

                   }
               });
           }

           @Override
           public void failure(TwitterException exception) {

           }
       });

    }



    private void loginWithFacebook() {

        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

              String userId= loginResult.getAccessToken().getUserId();

              GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        getUserFaceBookInfo(object);
                    }
                });

              Bundle paramters = new Bundle();
              paramters.putString("fields"," first_name, last_name, email");
              graphRequest.setParameters(paramters);
              graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    private void getUserFaceBookInfo(JSONObject object) {

        String first_name, last_name, email;

        try {
            first_name = object.getString("first_name");
            last_name=object.getString("last_name");
            email = object.getString("email");

            Log.d(TAG, "Profile name "+ first_name+" "+last_name+" Email "+email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
