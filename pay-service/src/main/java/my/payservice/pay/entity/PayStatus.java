package my.payservice.pay.entity;

public enum PayStatus {
    // 재고 확인, 결제 시작, 재고 차감, 결제 완료, 결제 실패, 결제 취소
    STOCK_DEDUCTED, PAY_COMPLETE, PAY_FAILED, PAY_CANCEL, STOCK_CHECKING
}
