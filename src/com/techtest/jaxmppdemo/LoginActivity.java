package com.techtest.jaxmppdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.techtest.jaxmppdemo.GtalkService.LoginDetails;
import com.techtest.jaxmppdemo.GtalkService.OnLoginComplete;

public class LoginActivity extends ServiceActivity implements OnLoginComplete {

    private LoginDetails mCurrentLoginDetails;
    private int mCurrentSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText usernameEt = (EditText)findViewById(R.id.usernameEt);
        final EditText passwordEt = (EditText)findViewById(R.id.passwordEt);

        Button loginbutton = (Button)findViewById(R.id.loginButton);
        loginbutton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(mBinder != null) {

                    setProgressBarIndeterminateVisibility(true);
                    mCurrentLoginDetails = new LoginDetails(usernameEt.getText().toString(),
                            passwordEt.getText().toString(), LoginActivity.this);
                    mCurrentSessionId = mBinder.login(mCurrentLoginDetails);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(mBinder != null) {
            mBinder.cancelLogin(mCurrentSessionId);
        }

        super.onDestroy();
    }

    @Override
    public void onLoginComplete(int sessionId) {
        setProgressBarIndeterminateVisibility(false);
        String toast = getResources().getString(R.string.login_successful);
        Toast.makeText(this, toast, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, ContactsListActivity.class);
        intent.putExtra(ContactsListActivity.BUNDLE_SESSION_ID, mCurrentSessionId);
        startActivity(intent);
    }

    @Override
    public void onLoginFailure() {

        setProgressBarIndeterminateVisibility(false);
        String error = getResources().getString(R.string.login_failed);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
    }

    @Override
    protected void onServiceDisaconnected() {
    }
}
