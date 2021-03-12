package com.YellowBlock.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final long TEN_SECOND_CANCEL = 2000;
    //change this when releasing (just for testing purposes)
    //private static final long MINIMUM_TIME_IN_MILLIS = 60000;
    private static final long MINIMUM_TIME_IN_MILLIS = 600000;
    private static final long MAXIMUM_TIME_IN_MILLIS = 14400000;
    private static final long PROGRESSBAR_STEP = 300000;
    private static final int COINS_PER_FIVE_MINUTES = 2;

    //UI elements
    private TextView mTextViewCountDown;
    private TextView mCoinsText;
    private TextView mRewardsText;
    private Button mButtonStart;
    private Button mButtonCancel;
    private Button mButtonGiveUp;
    private Button mButtonRewardedAd;
    private Button mButtonNoRewardedAd;
    private Button mButtonShop;
    private Button mTempCoinAdder;
    private ImageButton mChangeEggButton;
    private SeekBar mSeekBar;

    //Timers
    private CountDownTimer mCountDownTimer;
    private CountDownTimer mCancelCountDownTimer;

    //Booleans
    private boolean mCompleted;
    public boolean mTimerRunning;

    SharedPreferences settings;

    private long mTimeLeftInMillis =MINIMUM_TIME_IN_MILLIS;
    //This allows for timer to stay on time originally set by user
    private long currentSetTimeInMillis =MINIMUM_TIME_IN_MILLIS;
    private long mTimeLeftToCancelInMillis = TEN_SECOND_CANCEL;
    private long mStep = PROGRESSBAR_STEP/1000/60;

    private int mUserCoins;
    private int multiplier = 2;

    //Ad section here
    private RewardedAd rewardedAd;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //actual ad id "ca-app-pub-3422113791925832-9093021657" however using test ads at the moment "ca-app-pub-3940256099942544/5224354917"
        rewardedAd = new RewardedAd(this, "ca-app-pub-3422113791925832-9093021657");

        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError adError) {
                // Ad failed to load.
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //finds all the buttons and text views
        mTextViewCountDown = findViewById(R.id.textView_countdown);
        mCoinsText = findViewById(R.id.textView_userCoins);
        mButtonStart = findViewById(R.id.button_incubate);
        mButtonCancel = findViewById(R.id.button_cancel);
        mButtonGiveUp = findViewById(R.id.button_giveup);
        mButtonRewardedAd = findViewById(R.id.button_rewardedAd);
        mButtonNoRewardedAd = findViewById(R.id.button_no);
        mRewardsText = findViewById(R.id.textView_reward);
        mButtonShop = findViewById(R.id.button_shop);
        mTempCoinAdder = findViewById(R.id.button_tempCoinAdder);
        mChangeEggButton = findViewById(R.id.btnEggPicker);

        //enable when not testing
        mTextViewCountDown.setText(String.format("%d:00", MINIMUM_TIME_IN_MILLIS/1000/60));

        //sets up a shared preferences
        settings = getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE);

        //At the start up of the app it sets the coins text to whatever number is store in the userCoins shared preference and sets the mUserCoins to that value
        mCoinsText.setText(Integer.toString(settings.getInt("userCoins",0)));
        mUserCoins = settings.getInt("userCoins",0);

        //setting up the timer changer seek bar
        mSeekBar = findViewById(R.id.seekBar_timerValue);
        mSeekBar.setMin((int)MINIMUM_TIME_IN_MILLIS/1000/60);
        mSeekBar.setMax((int)MAXIMUM_TIME_IN_MILLIS/1000/60);
        mSeekBar.incrementProgressBy((int)mStep);


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextViewCountDown.setText(String.format("%d:00", progress));
                currentSetTimeInMillis = progress*1000*60;
                mTimeLeftInMillis = currentSetTimeInMillis;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mButtonStart.setOnClickListener(v -> startTimer());

        mButtonCancel.setOnClickListener(v -> cancelTimer());

        mButtonGiveUp.setOnClickListener(v -> giveUp());

        //deals with the rewarded video ad
        mButtonRewardedAd.setOnClickListener(v -> {
            if (rewardedAd.isLoaded()) {
                Activity activityContext = MainActivity.this;
                RewardedAdCallback adCallback = new RewardedAdCallback() {
                    @Override
                    public void onRewardedAdOpened() {
                        // Ad opened.
                        rewardUI(false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        // Ad closed.
                        rewardUI(false);
                        updateCoins(false);
                    }

                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem reward) {
                        updateCoins(true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(AdError adError) {
                        // Ad failed to display.
                        rewardUI(false);
                    }
                };
                rewardedAd.show(activityContext, adCallback);
            } else {
                Log.d("TAG", "The rewarded ad wasn't loaded yet.");
            }
        });

        mButtonNoRewardedAd.setOnClickListener(v -> updateCoins(false));

        //re enable when shop is working
        mButtonShop.setOnClickListener(v -> goToShopActivity());

        //temporary buttons for money testing
        mTempCoinAdder.setOnClickListener(new View.OnClickListener(){
        public void onClick(View v){
        SharedPreferences.Editor editor = settings.edit();
            mUserCoins+=20;
            editor.putInt("userCoins",mUserCoins);
            editor.apply();
            mCoinsText.setText(Integer.toString(settings.getInt("userCoins",0)));
        }
         });
        updateCountDownText();
    }

    //for going to the shop
    public void goToShopActivity(){
        Intent intent = new Intent(this, ShopActivity.class);
        this.startActivity(intent);
    }


    private void startTimer(){
        //Main countdown timer
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                mTimeLeftToCancelInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                mCompleted = true;
                mButtonStart.setVisibility(View.VISIBLE);
                mButtonGiveUp.setVisibility(View.INVISIBLE);
                cancelTimer();
                mCompleted = false;
            }
        }.start();
        //10 second cancel time timer
        mCancelCountDownTimer = new CountDownTimer(mTimeLeftToCancelInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftToCancelInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mButtonCancel.setVisibility(View.INVISIBLE);
                mButtonGiveUp.setVisibility(View.VISIBLE);
            }
        }.start();

        mButtonStart.setVisibility(View.INVISIBLE);
        mTimerRunning = true;
        mButtonCancel.setVisibility(View.VISIBLE);
        mSeekBar.setVisibility(View.INVISIBLE);
        mButtonShop.setVisibility(View.INVISIBLE);
        mChangeEggButton.setEnabled(false);
    }

    private void cancelTimer() {
        mTimeLeftInMillis = currentSetTimeInMillis;
        mTimeLeftToCancelInMillis = TEN_SECOND_CANCEL;
        updateCountDownText();
        mCountDownTimer.cancel();
        mCancelCountDownTimer.cancel();
        mTimerRunning = false;
        mButtonStart.setVisibility(View.VISIBLE);
        mButtonCancel.setVisibility(View.INVISIBLE);
        mSeekBar.setVisibility(View.VISIBLE);
        mButtonShop.setVisibility(View.VISIBLE);
        mChangeEggButton.setEnabled(true);
        if(mCompleted){
            rewardUI(true);
        }
    }

    //updates coins
    private void updateCoins(boolean mMultiplier){
        long fiveMinuteBlocks = ((currentSetTimeInMillis/1000/60/5)*COINS_PER_FIVE_MINUTES);
        //if the timer has been completed so the user hasn't cancelled or given up
        SharedPreferences.Editor editor = settings.edit();
        if(mMultiplier){
            mUserCoins = (mUserCoins + ((int)fiveMinuteBlocks *multiplier));
        }
        else{
            mUserCoins = (mUserCoins + ((int)fiveMinuteBlocks));
        }
        //currently adds 20 to the coins but will refactor the coins to reflect the time later
        //Updates the userCoins shared preference with the new value of coins and commits this
        editor.putInt("userCoins",mUserCoins);
        editor.apply();
        //updates the text to display this new number of coins
        mCoinsText.setText(Integer.toString(settings.getInt("userCoins",0)));
        rewardUI(false);
    }

    private void updateCountDownText() {
        // gets the minutes and seconds of the timers
        int minutes = (int) (mTimeLeftInMillis/1000/60);
        int seconds = (int) (mTimeLeftInMillis/1000%60);
        int cancelSeconds = (int)(mTimeLeftToCancelInMillis/1000%60);
        //Creates a string of the time left
        String timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);
        mTextViewCountDown.setText(timeLeftFormatted);
        //Lets the cancel button have a countdown timer on it so Cancel(10), Cancel(9) etc
        mButtonCancel.setText(String.format("Cancel(%02d)",cancelSeconds));
    }

    //when this method is called the mCompleted bool is set to false, this is a way of knowing whether to award coins. It sets give up button to invisible and calls the cancel timer function
    private void giveUp(){
        mCompleted = false;
        mButtonGiveUp.setVisibility(View.INVISIBLE);
        cancelTimer();
    }

    private void rewardUI(boolean visible){
        if(visible){
            mButtonRewardedAd.setVisibility(View.VISIBLE);
            mButtonNoRewardedAd.setVisibility(View.VISIBLE);
            mRewardsText.setVisibility(View.VISIBLE);
        }
        else{
            mButtonRewardedAd.setVisibility(View.INVISIBLE);
            mButtonNoRewardedAd.setVisibility(View.INVISIBLE);
            mRewardsText.setVisibility(View.INVISIBLE);
        }

    }

}