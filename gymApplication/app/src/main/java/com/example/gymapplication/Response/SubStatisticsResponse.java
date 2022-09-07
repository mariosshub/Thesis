package com.example.gymapplication.Response;

import java.util.Map;

public class SubStatisticsResponse {
    Map<String,Long> sortedNoOfSubs;
    Map<String,Float> sortedPercentageNoOfSubs;

    public SubStatisticsResponse(Map<String, Long> sortedNoOfSubs, Map<String, Float> sortedPercentageNoOfSubs) {
        this.sortedNoOfSubs = sortedNoOfSubs;
        this.sortedPercentageNoOfSubs = sortedPercentageNoOfSubs;
    }

    public Map<String, Long> getSortedNoOfSubs() {
        return sortedNoOfSubs;
    }


    public void setSortedNoOfSubs(Map<String, Long> sortedNoOfSubs) {
        this.sortedNoOfSubs = sortedNoOfSubs;
    }

    public Map<String, Float> getSortedPercentageNoOfSubs() {
        return sortedPercentageNoOfSubs;
    }

    public void setSortedPercentageNoOfSubs(Map<String, Float> sortedPercentageNoOfSubs) {
        this.sortedPercentageNoOfSubs = sortedPercentageNoOfSubs;
    }
}
