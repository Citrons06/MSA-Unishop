package my.payservice.pay.entity;

public enum PayStatus {
    // 결제 시작, 결제 중, 결제 완료, 결제 실패, 결제 취소
    PAY_START, PAYING, PAY_COMPLETE, PAY_FAILED, PAY_CANCEL
}
