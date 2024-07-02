package my.orderservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"),
    NOT_AUTHORIZED(HttpStatus.UNAUTHORIZED, "NOT_AUTHORIZED"),
    NOT_MATCHED(HttpStatus.BAD_REQUEST, "NOT_MATCHED"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR"),
    UPDATE_FAILED(HttpStatus.BAD_REQUEST, "UPDATE_FAILED"),
    CANCEL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CANCEL_FAILED"),
    RETURN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RETURN_FAILED"),
    NOT_SELLING_PRODUCT(HttpStatus.BAD_REQUEST, "NOT_SELLING_PRODUCT"),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "OUT_OF_STOCK"),
    ALREADY_CANCELED_ORDER(HttpStatus.BAD_REQUEST, "ALREADY_CANCELED_ORDER"),
    ALREADY_DELIVERING_ORDER(HttpStatus.BAD_REQUEST, "ALREADY_DELIVERING_ORDER"),
    CANCEL_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "CANCEL_PERIOD_EXPIRED");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}