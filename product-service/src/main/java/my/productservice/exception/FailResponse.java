package my.productservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FailResponse<T> {
    private int status;
    private String message;
    private T data;

    public static <T> FailResponse<T> success(int status, String message, T data) {
        return new FailResponse<>(status, message, data);
    }

    public static FailResponse<?> fail(int status, String message) {
        return new FailResponse<>(status, message, null);
    }
}