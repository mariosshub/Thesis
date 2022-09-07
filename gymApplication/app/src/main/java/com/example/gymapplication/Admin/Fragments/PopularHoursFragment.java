package com.example.gymapplication.Admin.Fragments;

import android.app.DatePickerDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.R;
import com.example.gymapplication.Service.AdminService;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PopularHoursFragment extends Fragment {
    private RelativeLayout parent;
    private Resources res;
    private LoadingData loadingData;
    private AdminService adminService;
    private AuthUtil authUtil;

    private Spinner statsSpinner;
    private Button dateButton;
    private DatePickerDialog datePickerDialog;
    private BarChart barChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popular_hours_fragment,container,false);
        res = requireActivity().getResources();

        initViews(view);

        adminService = ApiClient.getAdminService();
        authUtil = new AuthUtil(requireActivity());

        loadingData = new LoadingData(requireActivity());
        loadingData.setProgressBarForRelLayout(parent);

        setSpinner();
        initDatePicker();

        dateButton.setText(getTodaysDate());
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        return view;
    }

    private void setSpinner(){
        String[] stats = new String[]{res.getString(R.string.statistics_res_hours_header), res.getString(R.string.statistics_res_header), res.getString(R.string.statistics_subs_header)};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item,stats);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statsSpinner.setAdapter(arrayAdapter);

        statsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    requireActivity().getSupportFragmentManager().popBackStack();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction().replace(R.id.fragment_container, new ResStatisticsFragment()).addToBackStack(null).commit();
                }
                else if (position == 2){
                    requireActivity().getSupportFragmentManager().popBackStack();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction().replace(R.id.fragment_container, new StatisticsFragment()).addToBackStack(null).commit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getPopularHoursOfDate(String date){
        loadingData.setEnableProgressBar();

        Call<Map<String,Long>> call = adminService.getPopularHoursOfDay(authUtil.getToken(), date);
        call.enqueue(new Callback<Map<String, Long>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Long>> call, @NonNull Response<Map<String, Long>> response) {
                if(!response.isSuccessful()){
                    if(response.code() == 403){
                        new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                        Toast.makeText(requireActivity(), res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(response.errorBody() != null) {
                            try {
                                String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                                Toast.makeText(requireActivity(),message,Toast.LENGTH_SHORT).show();
                                barChart.clear();
                                barChart.invalidate();
                            }
                            catch (Exception e){
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                    loadingData.setDisableProgressBar();
                }
                else{
                    if(response.body() != null){
                        loadingData.setDisableProgressBar();
                        Map<String,Long> popularHours = response.body();
                        if(!popularHours.isEmpty()){
                            fillChart(popularHours);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Long>> call, @NonNull Throwable t) {
                Toast.makeText(requireActivity(),t.getMessage(),Toast.LENGTH_SHORT).show();
                loadingData.setDisableProgressBar();
            }
        });
    }

    private String getTodaysDate(){
        Calendar calendarToday = Calendar.getInstance();
        int day = calendarToday.get(Calendar.DAY_OF_MONTH);
        int month = calendarToday.get(Calendar.MONTH) +1;
        int year = calendarToday.get(Calendar.YEAR);
        return  day+"/"+month+"/"+year;

    }

    private String getFixedDayAndMonth(int year, int month, int day){
        String fixedDay, fixedMonth;
        if(day <= 9){
            fixedDay = String.format("%02d", day);
        }
        else{
            fixedDay = String.valueOf(day);
        }
        if(month <= 9){
            fixedMonth = String.format("%02d", month);
        }
        else{
            fixedMonth = String.valueOf(month);
        }

        return year+"-"+fixedMonth+"-"+fixedDay;
    }

    private void initDatePicker(){
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month+=1;
                String date = dayOfMonth+"/"+month+"/"+year;
                dateButton.setText(date);

                String dateFixed = getFixedDayAndMonth(year,month,dayOfMonth);
                getPopularHoursOfDate(dateFixed);
            }
        };
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        month +=1;
        int year = calendar.get(Calendar.YEAR);

        String date = getFixedDayAndMonth(year,month,day);
        getPopularHoursOfDate(date);

        datePickerDialog = new DatePickerDialog(requireActivity(), dateSetListener,year,month,day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

    }

    private void fillChart(Map<String,Long> popularHours){
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> hours = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String,Long> entry : popularHours.entrySet()) {
            barEntries.add(new BarEntry(i,entry.getValue().floatValue()));
            hours.add(entry.getKey());
            i++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries,res.getString(R.string.no_of_reservations));

        BarData data = new BarData(barDataSet);
        barChart.setData(data);
        Description description = barChart.getDescription();
        description.setText(res.getString(R.string.statistics_barChart_desc_popular_hours));
        barChart.setDescription(description);
        barChart.setFitBars(true); // make the x-axis fit exactly all bars
        barChart.setScaleEnabled(true);
        barChart.setDragEnabled(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setLabelRotationAngle(-80);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(hours));
        xAxis.setLabelCount(hours.size());
        barChart.invalidate(); // refresh
    }

    private void initViews(View view){
        parent = view.findViewById(R.id.popular_hours_fragment);
        statsSpinner = view.findViewById(R.id.selectStatsResPopHoursSpinner);
        dateButton = view.findViewById(R.id.datePickerPopHoursButton);
        barChart = view.findViewById(R.id.popularHoursBarChart);
    }
}
