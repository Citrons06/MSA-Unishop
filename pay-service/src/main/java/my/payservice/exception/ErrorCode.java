package my.payservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"),
    NOT_AUTHORIZED(HttpStatus.UNAUTHORIZED, "NOT_AUTHORIZED"),
    NOT_MATCHED(HttpStatus.BAD_REQUEST, "NOT_MATCHED"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR"),
    CANCEL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CANCEL_FAILED"),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "OUT_OF_STOCK"),
    ALREADY_CANCELED_ORDER(HttpStatus.BAD_REQUEST, "ALREADY_CANCELED_ORDER"),
    PAY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAY_FAILED"),
    PAY_CANCEL(HttpStatus.BAD_REQUEST, "PAY_CANCEL"),
    PAY_NOT_FOUND(HttpStatus.NOT_FOUND, "PAY_NOT_FOUND"),
    INVALID_PAY_STATUS(HttpStatus.BAD_REQUEST, "INVALID_PAY_STATUS"),
    NOT_ENOUGH_STOCK(HttpStatus.BAD_REQUEST, "NOT_ENOUGH_STOCK");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}