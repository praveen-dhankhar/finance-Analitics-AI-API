package com.financeapp.service;

import com.financeapp.service.impl.ForecastServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ForecastServiceUnitTest {

    @Test
    void simpleMovingAverage_shouldComputeCorrectValues() {
        ForecastServiceImpl svc = new ForecastServiceImpl(null, null, null, null);
        List<Double> vals = Arrays.asList(1d, 2d, 3d, 4d, 5d);
        double[] sma = svc.simpleMovingAverage(vals, 3);
        assertThat(sma).containsExactly(2.0, 3.0, 4.0);
    }

    @Test
    void ewma_shouldSmoothSeries() {
        ForecastServiceImpl svc = new ForecastServiceImpl(null, null, null, null);
        List<Double> vals = Arrays.asList(10d, 20d, 30d, 40d);
        double[] ewma = svc.exponentialWeightedMovingAverage(vals, 0.5);
        assertThat(ewma.length).isEqualTo(vals.size());
        assertThat(ewma[0]).isEqualTo(10.0);
        assertThat(ewma[3]).isBetween(30.0, 40.0);
    }

    @Test
    void linearRegressionForecast_shouldProjectTrend() {
        ForecastServiceImpl svc = new ForecastServiceImpl(null, null, null, null);
        List<Double> vals = Arrays.asList(1d, 2d, 3d, 4d, 5d);
        double[] fc = svc.linearRegressionForecast(vals, 3);
        assertThat(fc.length).isEqualTo(3);
        assertThat(fc[0]).isLessThan(fc[1]);
        assertThat(fc[1]).isLessThan(fc[2]);
    }

    @Test
    void seasonalDecomposition_shouldUseSeasonLength() {
        ForecastServiceImpl svc = new ForecastServiceImpl(null, null, null, null);
        List<Double> vals = Arrays.asList(10d, 20d, 30d, 10d, 20d, 30d, 10d, 20d, 30d);
        double[] fc = svc.seasonalDecomposition(vals, 3, 3);
        assertThat(fc.length).isEqualTo(3);
    }
}


