package com.example.wheele_commander.backend;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.example.wheele_commander.backend.interfaces.IConnection;
import com.example.wheele_commander.backend.interfaces.IConnectionManager;
import com.example.wheele_commander.backend.listeners.IConnectionReconnectListener;
import com.example.wheele_commander.backend.listeners.IReceiveListener;
import com.example.wheele_commander.deserializer.Data;
import com.example.wheele_commander.deserializer.Warning;
import com.example.wheele_commander.viewmodel.MessageType;

public abstract class CommunicationService extends Service {
    protected final IBinder binder = new CommunicationServiceBinder();
    protected final IReceiveListener receiveListener = this::onMessageReceived;
    protected final IConnectionReconnectListener reconnectListener = this::onReconnect;

    protected String TAG = "CommunicationService";
    protected IConnectionManager connectionManager;
    protected CommunicationThread communicationThread;
    protected MutableLiveData<Message> movementMessageData;
    protected MutableLiveData<Message> batteryMessageData;
    protected MutableLiveData<Message> warningMessageData;

    public class CommunicationServiceBinder extends Binder {
        public CommunicationService getService() {
            return CommunicationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: On bind called");
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        movementMessageData = new MutableLiveData<>();
        batteryMessageData = new MutableLiveData<>();
        warningMessageData = new MutableLiveData<>();
        initializeService();
    }

    protected abstract void initializeService();

    @Override
    public void onDestroy() {
        super.onDestroy();
        connectionManager.disconnect();
        communicationThread.stopCommunication();
    }

    protected void onMessageReceived(Message message) {
        if (message.what == MessageConstants.DATA_MESSAGE) {
            Data data = (Data) message.obj;

            if (data.getSpeed() != null) {
                Message velocityMessage = new Message();
                velocityMessage.what = MessageType.VELOCITY_UPDATE;
                velocityMessage.arg1 = data.getSpeed();
                movementMessageData.postValue(velocityMessage);
            }
            if (data.getBattery() != null) {
                Message batteryMessage = new Message();
                batteryMessage.what = MessageType.BATTERY_UPDATE;
                batteryMessage.arg1 = data.getBattery();
                batteryMessageData.postValue(batteryMessage);
            }
        } else if (message.what == MessageConstants.WARNING_MESSAGE) {
            Message warningMessage = new Message();
            warningMessage.what = MessageConstants.WARNING_MESSAGE;
            warningMessage.arg1 = ((Warning) message.obj).getCode();
            warningMessageData.postValue(warningMessage);
        }
    }

    protected void onReconnect(IConnection connection) {
        startCommunicationThread(connection);
    }

    protected void startCommunicationThread(IConnection connection) {
        communicationThread = new CommunicationThread(connection);
        communicationThread.setReceiveListener(receiveListener);
        communicationThread.start();
    }

    public void send(Message message) {
        // TODO: Check communicationManager.isConnected() == True
        if (communicationThread != null)
            communicationThread.send(message);
    }

    public MutableLiveData<Message> getMovementMessageData() {
        return movementMessageData;
    }

    public MutableLiveData<Message> getBatteryMessageData() {
        return batteryMessageData;
    }

    public MutableLiveData<Message> getWarningMessageData() {
        return warningMessageData;
    }
}
