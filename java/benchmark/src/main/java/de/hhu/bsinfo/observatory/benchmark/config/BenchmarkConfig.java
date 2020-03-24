package de.hhu.bsinfo.observatory.benchmark.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BenchmarkConfig {

    @SerializedName("className")
    @Expose
    private String className;

    @SerializedName("resultName")
    @Expose
    private String resultName;

    @SerializedName("detector")
    @Expose
    private DetectorConfig detectorConfig;

    @SerializedName("parameters")
    @Expose
    private ParameterConfig[] parameters;

    @SerializedName("operations")
    @Expose
    private OperationConfig[] operations;

    public String getClassName() {
        return className;
    }

    public String getResultName() {
        return resultName;
    }

    public DetectorConfig getDetectorConfig() {
        return detectorConfig;
    }

    public ParameterConfig[] getParameters() {
        return parameters;
    }

    public OperationConfig[] getOperations() {
        return operations;
    }
}
