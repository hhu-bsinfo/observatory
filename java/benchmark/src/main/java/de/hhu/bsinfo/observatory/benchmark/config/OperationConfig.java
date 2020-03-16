package de.hhu.bsinfo.observatory.benchmark.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OperationConfig {

    public enum OperationMode {
        @SerializedName("unidirectional") UNIDIRECTIONAL,
        @SerializedName("bidirectional") BIDIRECTIONAL
    }

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("modes")
    @Expose
    private OperationMode[] modes;

    @SerializedName("repetitions")
    @Expose
    private int repetitions;

    @SerializedName("iterations")
    @Expose
    private IterationConfig[] iterations;

    public String getName() {
        return name;
    }

    public OperationMode[] getModes() {
        return modes;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public IterationConfig[] getIterations() {
        return iterations;
    }
}
