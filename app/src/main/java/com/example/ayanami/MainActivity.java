package com.example.ayanami;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initOceanNumberRecord(this);
        logOceanNumberRecord(this);
    }


    public void initOceanNumberRecord(Context context){
        SharedPreferences pref = context.getSharedPreferences("OceanNumberRecord",MODE_PRIVATE);
        SharedPreferences.Editor editor = context.getSharedPreferences("OceanNumberRecord",MODE_PRIVATE).edit();
        //1_0:第一艘船（3格），船周围海的数量为0
        if(pref.getInt("1_0",0) == 0){
            for(int i = 0; i <= 12; i++){
                editor.putInt("1_"+i,10);
            }
            editor.putInt("ship1totalNumber",130);
            for(int i = 0; i <= 10; i++){
                editor.putInt("2_"+i,10);
            }
            editor.putInt("ship2totalNumber",110);
        }
        editor.apply();
    }

    public void logOceanNumberRecord(Context context){
        SharedPreferences pref = context.getSharedPreferences("OceanNumberRecord",MODE_PRIVATE);
        //1_0:第一艘船（3格），船周围海的数量为0
        for(int i = 0; i <= 12; i++){
            Log.d("Sam","oceanNumber:1_"+i+":"+pref.getInt("1_"+i,0));
        }
        Log.d("Sam","ship1totalNumber:"+pref.getInt("ship1totalNumber",0));
        for(int i = 0; i <= 10; i++){
            Log.d("Sam","oceanNumber:2_"+i+":"+pref.getInt("2_"+i,0));
        }
        Log.d("Sam","ship2totalNumber:"+pref.getInt("ship2totalNumber",0));

    }
}