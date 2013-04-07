package com.techtest.jaxmppdemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.techtest.jaxmppdemo.GtalkService.GtalkBinder;
import com.techtest.jaxmppdemo.GtalkService.LoginDetails;
import com.techtest.jaxmppdemo.GtalkService.OnLoginComplete;

public class LoginActivity extends Activity implements OnLoginComplete {

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


        Intent serviceIntent = new Intent(this, GtalkService.class);
        startService(serviceIntent);

        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mBinder != null) {
            mBinder.cancelLogin(mCurrentSessionId);
        }

        unbindService(mServiceConn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    private GtalkBinder mBinder;
    private final ServiceConnectionCallback mServiceConn = new ServiceConnectionCallback();

    public class ServiceConnectionCallback implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mBinder = (GtalkBinder)rawBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
        }
    }

    @Override
    public void onLoginComplete(int sessionId) {
        setProgressBarIndeterminateVisibility(false);
        String toast = getResources().getString(R.string.login_successful);
        Toast.makeText(this, toast, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, ContactsListActivity.class);
        intent.putExtra(ContactsListActivity.BUNDLE_SESSION_ID, mCurrentSessionId);

        //Back should not show up login activity during live session
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    @Override
    public void onLoginFailure() {

        setProgressBarIndeterminateVisibility(false);
        String error = getResources().getString(R.string.login_failed);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }
}
