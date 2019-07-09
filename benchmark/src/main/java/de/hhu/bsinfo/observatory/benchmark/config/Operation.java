package de.hhu.bsinfo.observatory.benchmark.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Operation {

    @SerializedName("size")
    @Expose
    private Integer size;

    @SerializedName("count")
    @Expose
    private Integer count;

    public Integer getSize() {
        return size;
    }

    public Integer getCount() {
        return count;
    }
}
