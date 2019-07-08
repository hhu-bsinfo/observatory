package de.hhu.bsinfo.infinibench.app.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RootConfig {

    @SerializedName("benchmarks")
    @Expose
    private BenchmarkConfig[] benchmarks = null;

    public BenchmarkConfig[] getBenchmarks() {
        return benchmarks;
    }
}