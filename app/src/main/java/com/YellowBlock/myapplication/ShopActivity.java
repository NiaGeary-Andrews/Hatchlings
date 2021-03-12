package com.YellowBlock.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class ShopActivity extends AppCompatActivity {

    private ImageButton fireEgg;
    private ImageButton waterEgg;
    private TextView coinsText;
    private TextView outcomeText;
    private int mUserCoins;

    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        settings = getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE);

        coinsText = findViewById(R.id.coinsText);
        outcomeText = findViewById(R.id.OutcomeText);
        fireEgg = findViewById(R.id.btnFireEgg);
        waterEgg = findViewById(R.id.btnWaterEgg);
        mUserCoins = settings.getInt("userCoins",0);

        coinsText.setText(Integer.toString(settings.getInt("userCoins",0)));

        fireEgg.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                deductCoins(5);
                fireEgg.setEnabled(false);
            }
        });

        waterEgg.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                deductCoins(5);
                waterEgg.setEnabled(false);
            }
        });
    }

    private void deductCoins(int price){
        SharedPreferences.Editor editor = settings.edit();
        if(mUserCoins>price){
            mUserCoins-=price;
            editor.putInt("userCoins",mUserCoins);
            editor.apply();
            coinsText.setText(Integer.toString(settings.getInt("userCoins",0)));
            //add code here that deals with actual buying of egg, e.g. disabling button for future and adding egg to users inventory
            outcomeText.setText("Egg bought");
        }
        else{
            //tell them they cant afford it
            outcomeText.setText("Not enough coins");
        }
    }
}