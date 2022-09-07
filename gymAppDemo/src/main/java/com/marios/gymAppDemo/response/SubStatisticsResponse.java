package com.marios.gymAppDemo.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
public class SubStatisticsResponse {
    Map<String,Long> sortedNoOfSubs;
    Map<String,Float> sortedPercentageNoOfSubs;
}
