package com.example.infinitimeapp;

import android.app.Activity;
import android.os.Bundle;

import com.example.infinitimeapp.common.Utils;

import androidx.annotation.Nullable;

public class WatchActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        Utils.init();
    }
}
