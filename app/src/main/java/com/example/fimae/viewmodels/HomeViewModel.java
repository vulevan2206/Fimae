package com.example.fimae.viewmodels;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;


public class HomeViewModel extends BaseObservable {

    private String address;
    public ObservableField<String> status = new ObservableField<>();



    public HomeViewModel(@NonNull android.content.Context context){
        //initStringeeConnection(context);
    }

    @Bindable
    public String getAddress() {
        return address;
    }



    public void onClickCall(){
        // when click call
        status.set("Connecting...");
    }

}
