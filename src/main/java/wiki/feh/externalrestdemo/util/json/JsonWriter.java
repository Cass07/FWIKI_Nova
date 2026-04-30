package wiki.feh.externalrestdemo.util.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JsonWriter {
    private String text;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonWriter of(Object target) {
        try {
            return new JsonWriter(objectMapper.writeValueAsString(target));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert text to JSON string", e);
        }
    }

    @Override
    public String toString() {
        return text;
    }
}
