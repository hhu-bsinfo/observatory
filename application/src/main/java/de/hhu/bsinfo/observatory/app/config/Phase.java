package de.hhu.bsinfo.observatory.app.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Phase {

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("operations")
    @Expose
    private Operation[] operations;

    public String getName() {
        return name;
    }

    public Operation[] getOperations() {
        return operations;
    }
}
