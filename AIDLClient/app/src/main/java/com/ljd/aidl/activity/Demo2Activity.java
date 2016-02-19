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

import com.ljd.aidl.IComputerManager;
import com.ljd.aidl.client.R;
import com.ljd.aidl.entity.ComputerEntity;

import java.util.List;

public class Demo2Activity extends AppCompatActivity {

    private IComputerManager mRemoteComputerManager;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if(mRemoteComputerManager != null){
                mRemoteComputerManager.asBinder().unlinkToDeath(mDeathRecipient,0);
                mRemoteComputerManager = null;
                bindService();
            }
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IComputerManager computerManager = IComputerManager.Stub.asInterface(service);
            mRemoteComputerManager = computerManager;
            try {
                mRemoteComputerManager.asBinder().linkToDeath(mDeathRecipient,0);
                List<ComputerEntity> computerList = computerManager.getComputerList();
                for (int i =0;i<computerList.size();i++){
                    Log.d("brand",computerList.get(i).brand);
                    Log.d("model",computerList.get(i).model);
                    Log.d("computerId",String.valueOf(computerList.get(i).computerId));
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteComputerManager = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo2);
        bindService();
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    private void bindService(){
        Intent intent = new Intent();
        intent.setAction("com.ljd.aidl.action.COMPUTER_SERVICE");
        bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
    }
}
