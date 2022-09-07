package com.example.gymapplication.Utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class LoadingData {
    private final Context context;
    private ProgressBar progressBar;
    private ConstraintLayout constraintLayout;
    private RelativeLayout relativeLayout;
    public LoadingData(Context context){
        this.context = context;
    }

    // create a progress bar for Constraint Layout
    public void setProgressBarForConLayout(ConstraintLayout constraintLayout){
        this.constraintLayout = constraintLayout;
        // Create horizontal progressBar dynamically...
        this.progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(30, 50, 30, 50);
        progressBar.setLayoutParams(layoutParams);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
        constraintLayout.addView(progressBar);
    }

    // create progress bar for Relative Layout
    public void setProgressBarForRelLayout(RelativeLayout relativeLayout){
        this.relativeLayout = relativeLayout;
        this.progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.setMargins(30, 80, 30, 50);
        progressBar.setLayoutParams(relativeParams);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
        relativeLayout.addView(progressBar);

    }

    public void setEnableProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
        // disable user from interacting with screen
        ((AppCompatActivity)context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void setDisableProgressBar(){
        progressBar.setVisibility(View.GONE);
        if(constraintLayout != null){
            constraintLayout.removeView(progressBar);
        }
        else if(relativeLayout != null){
            relativeLayout.removeView(progressBar);
        }
        // enable interaction with screen
        ((AppCompatActivity)context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
