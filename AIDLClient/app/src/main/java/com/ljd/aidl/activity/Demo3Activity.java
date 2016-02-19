package com.ljd.aidl.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ljd.aidl.IComputerManagerObserver;
import com.ljd.aidl.IOnComputerArrivedListener;
import com.ljd.aidl.client.R;
import com.ljd.aidl.entity.ComputerEntity;

import java.util.List;

public class Demo3Activity extends AppCompatActivity {


    private static final int MESSAGE_COMPUTER_ARRIVED = 1;

    private IComputerManagerObserver mRemoteComputerManager;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_COMPUTER_ARRIVED:
                    Log.d("receive computer :",msg.obj+"");
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }

        }
    };

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
            IComputerManagerObserver computerManager = IComputerManagerObserver.Stub.asInterface(service);
            mRemoteComputerManager = computerManager;
            try {
                mRemoteComputerManager.asBinder().linkToDeath(mDeathRecipient,0);
                List<ComputerEntity> computerList = computerManager.getComputerList();
                for (int i =0;i<computerList.size();i++){
                    Log.d("brand",computerList.get(i).brand);
                    Log.d("model",computerList.get(i).model);
                    Log.d("computerId",String.valueOf(computerList.get(i).computerId));
                }
                ComputerEntity computer = new ComputerEntity(3,"hp","envy13");
                computerManager.addComputer(computer);
                List<ComputerEntity> newList = computerManager.getComputerList();
                for (int i =0;i<newList.size();i++){
                    Log.d("brand",newList.get(i).brand);
                    Log.d("model",newList.get(i).model);
                    Log.d("computerId",String.valueOf(newList.get(i).computerId));
                }
                computerManager.registerUser(mOnComputerArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteComputerManager = null;
        }
    };

    private IOnComputerArrivedListener mOnComputerArrivedListener = new IOnComputerArrivedListener.Stub(){

        @Override
        public void onComputerArrived(ComputerEntity computer) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_COMPUTER_ARRIVED,computer.model).sendToTarget();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo3);
        bindService();
    }

    @Override
    protected void onDestroy() {
        if (mRemoteComputerManager != null && mRemoteComputerManager.asBinder().isBinderAlive()){
            try {
                mRemoteComputerManager.unRegisterUser(mOnComputerArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        super.onDestroy();
    }

    private void bindService(){
        Intent intent = new Intent();
        intent.setAction("com.ljd.aidl.action.COMPUTER_OBSERVER_SERVICE");
        bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
    }
}
