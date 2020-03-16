package de.hhu.bsinfo.observatory.benchmark.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IterationConfig {

    @SerializedName("size")
    @Expose
    private Integer size;

    @SerializedName("count")
    @Expose
    private Integer count;

    @SerializedName("warmUp")
    @Expose
    private Integer warmUpIterations;

    public Integer getSize() {
        return size;
    }

    public Integer getCount() {
        return count;
    }

    public Integer getWarmUpIterations() {
        return warmUpIterations;
    }
}
