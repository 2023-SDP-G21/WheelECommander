package com.example.wheele_commander.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Observer;

import com.example.wheele_commander.backend.CommunicationService;

public abstract class AbstractViewModel extends AndroidViewModel {
    private static final String TAG = "AbstractViewModel";
    protected Context context;
    protected CommunicationService communicationService;
    protected ServiceConnection serviceConnection;
    protected Observer<Message> messageObserver;

    public AbstractViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        messageObserver = this::handleMessage;
    }

    public abstract void handleMessage(Message message);

    @Override
    protected void onCleared() {
        super.onCleared();
        // TODO: Causes errors, when application is terminated
        // Log.d(TAG, "onCleared: Clearing ViewModel");
        // context.unbindService(serviceConnection);
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }
}
