package com.techtest.jaxmppdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

import com.techtest.jaxmppdemo.GtalkService.GtalkBinder;

public abstract class ServiceActivity extends FragmentActivity {

    protected GtalkBinder mBinder;
    private final ServiceConnectionCallback mServiceConn = new ServiceConnectionCallback();

    public class ServiceConnectionCallback implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mBinder = (GtalkBinder)rawBinder;
            ServiceActivity.this.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ServiceActivity.this.onServiceDisconnected();
            mBinder = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent serviceIntent = new Intent(this, GtalkService.class);
        startService(serviceIntent);

        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mServiceConn);
    }

    protected abstract void onServiceConnected();
    protected abstract void onServiceDisconnected();
}
