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
import android.widget.TextView;
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

public class ResStatisticsFragment extends Fragment {
    private RelativeLayout parent;
    private Resources res;
    private LoadingData loadingData;
    private AdminService adminService;
    private AuthUtil authUtil;

    private BarChart barChart;
    private Button dateButton;
    private DatePickerDialog datePickerDialog;
    private TextView selectDate, selectMonth;
    private Spinner statsSpinner, selectOperationSpinner, selectMonthSpinner;

    private Calendar calendarToday;
    private boolean defaultMonth = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reservations_statistics,container,false);
        res = requireActivity().getResources();

        initViews(view);

        adminService = ApiClient.getAdminService();
        authUtil = new AuthUtil(requireActivity());

        loadingData = new LoadingData(requireActivity());
        loadingData.setProgressBarForRelLayout(parent);

        setSpinners();
        initDatePicker();

        dateButton.setText(getYesterdayDate());
        dateButton.setOnClickListener(v -> datePickerDialog.show());

        return view;
    }

    private void initViews(View view){
        parent = view.findViewById(R.id.resStatisticsFragment);
        statsSpinner = view.findViewById(R.id.selectStatsResSpinner);
        selectDate = view.findViewById(R.id.selectDateResStats);
        selectMonth = view.findViewById(R.id.selectMonthResStats);
        selectMonthSpinner = view.findViewById(R.id.statsMonthSpinner);
        selectOperationSpinner = view.findViewById(R.id.selectStatsResMode);
        dateButton = view.findViewById(R.id.datePickerButton);
        barChart = view.findViewById(R.id.statsBarChart);
    }

    private void setSpinners(){
        String[] stats = new String[]{res.getString(R.string.statistics_res_header),res.getString(R.string.statistics_res_hours_header), res.getString(R.string.statistics_subs_header)};
        ArrayAdapter<String> arrayAdapterStats = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item,stats);
        arrayAdapterStats.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statsSpinner.setAdapter(arrayAdapterStats);

        String[] operation = new String[]{res.getString(R.string.statistics_subs_operation_day), res.getString(R.string.statistics_subs_operation_month)};
        ArrayAdapter<String> arrayAdapterOperation = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item,operation);
        arrayAdapterOperation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectOperationSpinner.setAdapter(arrayAdapterOperation);

        String[] months = new String[]{res.getString(R.string.january),res.getString(R.string.february),res.getString(R.string.march),res.getString(R.string.april),
                res.getString(R.string.may),res.getString(R.string.june),res.getString(R.string.july),res.getString(R.string.august),
                res.getString(R.string.september),res.getString(R.string.october),res.getString(R.string.november),res.getString(R.string.december)};
        ArrayAdapter<String> arrayAdapterMonth = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item,months);
        arrayAdapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectMonthSpinner.setAdapter(arrayAdapterMonth);


        statsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // select popular hours of reservation statistics
                if(position == 1){
                    requireActivity().getSupportFragmentManager().popBackStack();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction().replace(R.id.fragment_container, new PopularHoursFragment()).addToBackStack(null).commit();
                }
                // select statistics for subscriptions
                else if(position == 2){
                    requireActivity().getSupportFragmentManager().popBackStack();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction().replace(R.id.fragment_container, new StatisticsFragment()).addToBackStack(null).commit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        selectOperationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // day selection
                if(position == 0){
                    selectDate.setVisibility(View.VISIBLE);
                    dateButton.setVisibility(View.VISIBLE);
                    selectMonth.setVisibility(View.GONE);
                    selectMonthSpinner.setVisibility(View.GONE);
                }
                // month selection
                else if (position == 1){
                    selectDate.setVisibility(View.GONE);
                    dateButton.setVisibility(View.GONE);
                    selectMonth.setVisibility(View.VISIBLE);
                    selectMonthSpinner.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        selectMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(defaultMonth){
                    selectMonthSpinner.setSelection(calendarToday.get(Calendar.MONTH));
                    defaultMonth = false;
                }
                else{
                    getMostPopularLessonsByMonth(calendarToday.get(Calendar.YEAR),position + 1);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private String getYesterdayDate(){
        calendarToday = Calendar.getInstance();
        calendarToday.add(Calendar.DATE, -1);
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

                getMostPopularLessonsByDate(dateFixed);
            }
        };
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        month +=1;
        int year = calendar.get(Calendar.YEAR);

        String date = getFixedDayAndMonth(year,month,day);
        getMostPopularLessonsByDate(date);

        datePickerDialog = new DatePickerDialog(requireActivity(), dateSetListener,year,month,day);
        // set yesterdays date
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 86400000);

    }
    private void getMostPopularLessonsByDate(String day){
        loadingData.setEnableProgressBar();

        Call<Map<String,Long>> call = adminService.getMostPopularLessonsByDate(authUtil.getToken(),day);
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
                                if(message.equals("no_res_for_day"))
                                    Toast.makeText(requireActivity(),res.getString(R.string.no_res_for_day),Toast.LENGTH_SHORT).show();
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
                        Map<String,Long> resStat = response.body();
                        if(!resStat.isEmpty()){
                            fillResChart(resStat,res.getString(R.string.statistics_barChart_desc_day));
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

    private void getMostPopularLessonsByMonth(int year, int month){
        loadingData.setEnableProgressBar();

        Call<Map<String,Long>> call = adminService.getMostPopularLessonsByMonth(authUtil.getToken(),year,month);
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
                                if(message.equals("no_res_for_month"))
                                    Toast.makeText(requireActivity(),res.getString(R.string.no_res_for_month),Toast.LENGTH_SHORT).show();
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
                        Map<String,Long> resStat = response.body();
                        if(!resStat.isEmpty()){
                            fillResChart(resStat,res.getString(R.string.statistics_barChart_desc_month));
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
    private void fillResChart(Map<String,Long> resStat, String desc){
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> workouts = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String,Long> entry : resStat.entrySet()) {
            barEntries.add(new BarEntry(i,entry.getValue().floatValue()));
            workouts.add(entry.getKey());
            i++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries,res.getString(R.string.no_of_reservations));

        BarData data = new BarData(barDataSet);
        barChart.setData(data);
        Description description = barChart.getDescription();
        description.setText(desc);
        barChart.setDescription(description);
        barChart.setFitBars(true); // make the x-axis fit exactly all bars
        barChart.setScaleEnabled(true);
        barChart.setDragEnabled(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setLabelRotationAngle(-80);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(workouts));
        xAxis.setLabelCount(workouts.size());
        barChart.invalidate(); // refresh
    }
}
