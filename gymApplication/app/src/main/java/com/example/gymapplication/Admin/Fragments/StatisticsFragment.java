package com.example.gymapplication.Admin.Fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
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
import com.example.gymapplication.Response.SubStatisticsResponse;
import com.example.gymapplication.Service.AdminService;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsFragment extends Fragment {

    private RelativeLayout parent;
    private Resources res;
    private LoadingData loadingData;
    private AdminService adminService;
    private AuthUtil authUtil;

    LinearLayout subsListLayout;
    private Spinner statsSpinner;
    private Spinner selectOperationSpinner;
    TextView workoutNameHeader, noOfSubsHeader;

    private PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.statistics_fragment,container,false);
        res = requireActivity().getResources();

        initViews(view);

        adminService = ApiClient.getAdminService();
        authUtil = new AuthUtil(requireActivity());

        loadingData = new LoadingData(requireActivity());
        loadingData.setProgressBarForRelLayout(parent);

        setSpinners();

        return view;
    }

    private void setSpinners(){
        String[] stats = new String[]{res.getString(R.string.statistics_subs_header), res.getString(R.string.statistics_res_header), res.getString(R.string.statistics_res_hours_header)};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item,stats);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statsSpinner.setAdapter(arrayAdapter);

        String[] operations = new String[]{res.getString(R.string.statistics_subs_operation_1), res.getString(R.string.statistics_subs_operation_2)};
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item,operations);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectOperationSpinner.setAdapter(arrayAdapter2);


        statsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // select statistics for reservation
                if(position == 1){
                    requireActivity().getSupportFragmentManager().popBackStack();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction().replace(R.id.fragment_container, new ResStatisticsFragment()).addToBackStack(null).commit();
                }
                // select popular hours of reservation statistics
                else if(position == 2){
                    requireActivity().getSupportFragmentManager().popBackStack();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction().replace(R.id.fragment_container, new PopularHoursFragment()).addToBackStack(null).commit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        selectOperationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // select to show number of subs
                if(position == 0){
                    pieChart.setVisibility(View.VISIBLE);
                    setNoOfSubsStatistics();
                }
                // select to show earnings
                else{
                    pieChart.setVisibility(View.GONE);
                    setSumOfSubsLessonIncome();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setNoOfSubsStatistics(){
        loadingData.setEnableProgressBar();

        Call<SubStatisticsResponse> call = adminService.getMostSubLessons(authUtil.getToken());
        call.enqueue(new Callback<SubStatisticsResponse>() {
            @Override
            public void onResponse(@NonNull Call<SubStatisticsResponse> call, @NonNull Response<SubStatisticsResponse> response) {
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
                        SubStatisticsResponse subStatisticsResponse = response.body();
                        addSubsStatisticsListView(subStatisticsResponse.getSortedNoOfSubs());
                        setPieChart(subStatisticsResponse.getSortedPercentageNoOfSubs());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubStatisticsResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireActivity(),t.getMessage(),Toast.LENGTH_SHORT).show();
                loadingData.setDisableProgressBar();
            }
        });
    }

    private void addSubsStatisticsListView(Map<String,Long> subsStat){
        noOfSubsHeader.setText(res.getString(R.string.statistics_NoSubs_header));
        subsListLayout.removeAllViews();
        int i = 0;
        for (Map.Entry<String,Long> entry : subsStat.entrySet()) {
            View statisticsSubsListView = LayoutInflater.from(requireActivity()).inflate(R.layout.statistics_sub_list,null,false);

            TextView workoutName, noOfSubs;
            LinearLayout parentSub;

            workoutName = statisticsSubsListView.findViewById(R.id.statsWorkoutName);
            noOfSubs= statisticsSubsListView.findViewById(R.id.statsNoSubs);
            parentSub= statisticsSubsListView.findViewById(R.id.statsSubLayoutParent);

            // change the background color alternatively
            if(i%2==0){
                parentSub.setBackgroundColor(res.getColor(R.color.light_gray, requireActivity().getTheme()));
            }
            else{
                parentSub.setBackgroundColor(res.getColor(R.color.white, requireActivity().getTheme()));
            }

            workoutName.setText(entry.getKey());
            noOfSubs.setText(String.valueOf(entry.getValue()));

            subsListLayout.addView(statisticsSubsListView);
            i++;
        }
    }

    private void addSubsStatisticsListForLessonIncome(Map<String,Double> subsLessonCost){
        noOfSubsHeader.setText(res.getString(R.string.statistics_income_header));
        subsListLayout.removeAllViews();

        int i = 0;
        for (Map.Entry<String,Double> entry: subsLessonCost.entrySet()) {
            View statisticsSubsListView = LayoutInflater.from(requireActivity()).inflate(R.layout.statistics_sub_list,null,false);

            TextView workoutName, incomeOfLesson;
            LinearLayout parentSub;

            workoutName = statisticsSubsListView.findViewById(R.id.statsWorkoutName);
            incomeOfLesson= statisticsSubsListView.findViewById(R.id.statsNoSubs);
            parentSub= statisticsSubsListView.findViewById(R.id.statsSubLayoutParent);

            if(i%2==0){
                parentSub.setBackgroundColor(res.getColor(R.color.gray_blue, requireActivity().getTheme()));
            }
            else{
                parentSub.setBackgroundColor(res.getColor(R.color.white, requireActivity().getTheme()));
            }

            workoutName.setText(entry.getKey());
            incomeOfLesson.setText(String.valueOf(entry.getValue()));

            subsListLayout.addView(statisticsSubsListView);
            i++;
        }
    }

    private void setSumOfSubsLessonIncome(){
        loadingData.setEnableProgressBar();

        Call<Map<String,Double>> call = adminService.getSumLessonCost(authUtil.getToken());
        call.enqueue(new Callback<Map<String, Double>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Double>> call, @NonNull Response<Map<String, Double>> response) {
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
                                subsListLayout.removeAllViews();
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
                        addSubsStatisticsListForLessonIncome(response.body());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Double>> call, @NonNull Throwable t) {
                Toast.makeText(requireActivity(),t.getMessage(),Toast.LENGTH_SHORT).show();
                loadingData.setDisableProgressBar();
            }
        });
    }

    private void setPieChart(Map<String,Float> subsStatPercent){
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        for (Map.Entry<String,Float> entry : subsStatPercent.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue(),entry.getKey()));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries,res.getString(R.string.workouts_label));
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setValueTextSize(14);

        Description description = pieChart.getDescription();
        description.setText(res.getString(R.string.statistics_pie_chart_desc));
        pieChart.setDescription(description);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setUsePercentValues(true);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private void initViews(View view){
        parent = view.findViewById(R.id.statistics_fragment);
        statsSpinner = view.findViewById(R.id.selectStatsSpinner);
        selectOperationSpinner = view.findViewById(R.id.selectStatsMode);
        workoutNameHeader = view.findViewById(R.id.statsWorkoutNameHeader);
        noOfSubsHeader = view.findViewById(R.id.statsNoSubsHeader);
        subsListLayout = view.findViewById(R.id.layout_statistics_sub_list);
        pieChart = view.findViewById(R.id.statsPieChart);
    }
}
