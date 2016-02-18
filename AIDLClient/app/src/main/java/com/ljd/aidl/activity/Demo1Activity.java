package com.ljd.aidl.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ljd.aidl.ICalculate;
import com.ljd.aidl.client.R;

public class Demo1Activity extends AppCompatActivity {

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ICalculate calculate = ICalculate.Stub.asInterface(service);
            try {
                int result = calculate.add(2,4);
                Log.e("result",String.valueOf(result));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bindService();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo1);
        bindService();
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    private void bindService(){
        Intent intent = new Intent();
        intent.setAction("com.ljd.aidl.action.CALCULATE_SERVICE");
        bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
    }
}
