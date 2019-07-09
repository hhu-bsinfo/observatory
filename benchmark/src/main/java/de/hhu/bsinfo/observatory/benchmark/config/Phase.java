package de.hhu.bsinfo.observatory.benchmark.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Phase {

    public enum Mode {
        @SerializedName("unidirectional") UNIDIRECTIONAL,
        @SerializedName("bidirectional") BIDIRECTIONAL
    }

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("modes")
    @Expose
    private Mode[] modes;

    @SerializedName("operations")
    @Expose
    private Operation[] operations;

    public String getName() {
        return name;
    }

    public Mode[] getModes() {
        return modes;
    }

    public Operation[] getOperations() {
        return operations;
    }
}
