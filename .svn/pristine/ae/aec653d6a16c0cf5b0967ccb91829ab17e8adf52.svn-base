package com.micsig.tbook.tbookscope.top.layout.save;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TaskSuffixNumModel extends ViewModel {
    private final MutableLiveData<String> textLiveData = new MutableLiveData<>();

    public LiveData<String> getTextLiveData(){
        return textLiveData;
    }

    public void updateText(String newSuffixNum){
        textLiveData.postValue(newSuffixNum);
    }
}
