package de.hhu.bsinfo.observatory.app.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BenchmarkConfig {

    @SerializedName("className")
    @Expose
    private String className;

    @SerializedName("parameters")
    @Expose
    private BenchmarkParameter[] parameters = null;

    public String getClassName() {
        return className;
    }

    public BenchmarkParameter[] getParameters() {
        return parameters;
    }
}
