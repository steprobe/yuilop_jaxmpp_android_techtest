package com.techtest.jaxmppdemo;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

public class GtalkService extends Service {

    public interface OnLoginComplete {
        public void onLoginComplete();
        public void onLoginFailure();
    }

    public class LoginDetails {
        public String username;
        public String password;
        public OnLoginComplete callback;

        public LoginDetails(String username, String password, OnLoginComplete callback) {
            this.username = username;
            this.password = password;
            this.callback = callback;
        }
    }

    public interface GtalkBinder {
        public void login(LoginDetails loginDetails);
        public void cancelLogin(LoginDetails loginDetails);
    }

    private final List<LoginDetails> mLogins = new ArrayList<LoginDetails>();

    private class GtalkBinderImpl extends Binder implements GtalkBinder {

        @Override
        public void login(LoginDetails loginDetails) {
            GtalkService.this.login(loginDetails);
        }

        @Override
        public void cancelLogin(LoginDetails loginDetails) {
            GtalkService.this.cancelLogin(LoginDetails loginDetails);
        }
    }

    private final Handler mHandler = new Handler();
    private final GtalkBinderImpl mBinder = new GtalkBinderImpl();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void login(LoginDetails loginDetails) {
        // TODO Auto-generated method stub

    }
}

