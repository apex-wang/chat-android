package com.hyphenate.chatuidemo.section.chat.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.hyphenate.chatuidemo.common.livedatas.MessageChangeLiveData;

public class MessageViewModel extends AndroidViewModel {
    private MessageChangeLiveData messageObservable;

    public MessageViewModel(@NonNull Application application) {
        super(application);
        messageObservable = MessageChangeLiveData.getInstance();
    }

    public void setMessageChange(String change) {
        messageObservable.postValue(change);
    }

    public LiveData<String> getMessageChange() {
        return messageObservable;
    }
}