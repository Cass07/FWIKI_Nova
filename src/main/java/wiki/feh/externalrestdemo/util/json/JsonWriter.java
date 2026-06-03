package wiki.feh.externalrestdemo.util.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import wiki.feh.externalrestdemo.util.json.exception.JsonSerializeFailedException;

@Getter
@AllArgsConstructor
public class JsonWriter {
    private String text;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonWriter of(Object target) {
        try {
            return new JsonWriter(objectMapper.writeValueAsString(target));
        } catch (Exception e) {
            throw new JsonSerializeFailedException("Failed to convert text to JSON string", e);
        }
    }

    @Override
    public String toString() {
        return text;
    }
}
