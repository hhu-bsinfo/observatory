package de.hhu.bsinfo.observatory.app.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Config {

    @SerializedName("className")
    @Expose
    private String className;

    @SerializedName("parameters")
    @Expose
    private Parameter[] parameters;

    @SerializedName("phases")
    @Expose
    private Phase[] phases;

    public String getClassName() {
        return className;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public Phase[] getPhases() {
        return phases;
    }
}
