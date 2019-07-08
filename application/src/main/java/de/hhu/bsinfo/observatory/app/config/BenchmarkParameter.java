package de.hhu.bsinfo.observatory.app.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BenchmarkParameter {

    @SerializedName("key")
    @Expose
    private String key;

    @SerializedName("value")
    @Expose
    private String value;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}