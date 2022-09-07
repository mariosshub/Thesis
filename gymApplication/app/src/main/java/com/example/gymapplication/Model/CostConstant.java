package com.example.gymapplication.Model;

import android.content.Context;

import com.example.gymapplication.R;

import java.util.ArrayList;
import java.util.List;

public class CostConstant {
    public static final String COSTPERDAY = "CPD";
    public static final String COSTPERWEEK = "CPW";
    public static final String COSTPERMONTH = "CPM";
    public static final String COSTPERYEAR = "CPY";

    private Context context;

    public CostConstant(Context context){
        this.context = context;
    }

    public List<String> getListOfCosts(){
        List<String> cost = new ArrayList<>();
        cost.add(context.getResources().getString(R.string.cost_per_day_enum));
        cost.add(context.getResources().getString(R.string.cost_per_week_enum));
        cost.add(context.getResources().getString(R.string.cost_per_month_enum));
        cost.add(context.getResources().getString(R.string.cost_per_year_enum));
        return cost;
    }
}
