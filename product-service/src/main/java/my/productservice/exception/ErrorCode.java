package my.productservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND"),
    NOT_AUTHORIZED(HttpStatus.UNAUTHORIZED, "NOT_AUTHORIZED"),
    NOT_MATCHED(HttpStatus.BAD_REQUEST, "NOT_MATCHED"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND"),
    UPDATE_FAILED(HttpStatus.BAD_REQUEST, "UPDATE_FAILED"),
    STOCK_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "STOCK_NOT_ENOUGH"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE"),
    LOCK_ACQUISITION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "LOCK_ACQUISITION_FAILED"),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "INVALID_QUANTITY");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}