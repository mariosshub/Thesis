package com.example.gymapplication.Model;

import android.content.Context;

import com.example.gymapplication.R;

import java.util.ArrayList;
import java.util.List;

public class DaysOfWeekConstant {
    public static final String MONDAY = "Monday";
    public static final String TUESDAY = "Tuesday";
    public static final String WEDNESDAY = "Wednesday";
    public static final String THURSDAY = "Thursday";
    public static final String FRIDAY = "Friday";
    public static final String SATURDAY = "Saturday";
    public static final String SUNDAY = "Sunday";

    private Context context;

    public DaysOfWeekConstant(Context context){
        this.context=context;
    }

    public List<String> getListOfDays(){
        List<String> days = new ArrayList<>();
        days.add(context.getResources().getString(R.string.monday));
        days.add(context.getResources().getString(R.string.tuesday));
        days.add(context.getResources().getString(R.string.wednesday));
        days.add(context.getResources().getString(R.string.thursday));
        days.add(context.getResources().getString(R.string.friday));
        days.add(context.getResources().getString(R.string.saturday));
        days.add(context.getResources().getString(R.string.sunday));

        return days;
    }
}
