package my.productservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class CommonExceptionAdvice {

    @ExceptionHandler(value = CommonException.class)
    public ResponseEntity<?> handleGlobalException(CommonException e) {
        log.warn("Handled GlobalException: {}", e.getErrorCode().getMessage(), e);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(FailResponse.fail(e.getErrorCode().getHttpStatus().value(), e.getErrorCode().getMessage()));
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Handled IllegalArgumentException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(FailResponse.fail(HttpStatus.BAD_REQUEST.value(), "Invalid request parameters"));
    }

    @ExceptionHandler(value = NullPointerException.class)
    public ResponseEntity<?> handleNullPointerException(NullPointerException e) {
        log.error("Handled NullPointerException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FailResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected server error"));
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        log.error("Handled RuntimeException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FailResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Runtime error occurred"));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("Handled Exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FailResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred"));
    }
}
