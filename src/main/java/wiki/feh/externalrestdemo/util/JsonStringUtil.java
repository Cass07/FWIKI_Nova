package wiki.feh.externalrestdemo.util;


import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonStringUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON string", e);
        }
    }
}
