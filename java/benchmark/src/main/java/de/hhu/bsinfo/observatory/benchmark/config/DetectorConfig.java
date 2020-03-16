package de.hhu.bsinfo.observatory.benchmark.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DetectorConfig {

    public enum MeasurementMode {
        @SerializedName("mad") MAD,
        @SerializedName("compat") COMPAT
    }

    @SerializedName("enabled")
    @Expose
    private boolean enabled;

    @SerializedName("deviceNumber")
    @Expose
    private Integer deviceNumber;

    @SerializedName("mode")
    @Expose
    private MeasurementMode mode;

    public boolean isEnabled() {
        return enabled;
    }

    public Integer getDeviceNumber() {
        return deviceNumber;
    }

    public MeasurementMode getMode() {
        return mode;
    }
}
