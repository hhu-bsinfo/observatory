package de.hhu.bsinfo.infinibench.app.util;

import com.google.gson.Gson;
import de.hhu.bsinfo.infinibench.InfiniBench;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonResourceLoader {

    private static Gson gson = new Gson();

    private JsonResourceLoader() {}

    public static <T> T loadJsonObject(String resourceName, Class<T> clazz) throws IOException {
        InputStream inputStream = InfiniBench.class.getClassLoader().getResourceAsStream(resourceName);

        if(inputStream == null) {
            throw new IOException("Unable to open input stream for resource '" + resourceName + "'!");
        }

        T ret = gson.fromJson(new BufferedReader(new InputStreamReader(inputStream)), clazz);

        if(ret == null) {
            throw new IOException("Unable to construct object of type '" + clazz.getSimpleName() + "' from resource '" + resourceName + "'!");
        }

        return ret;
    }

}
