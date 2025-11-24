package com.company.payment.model;

// 金额值对象
public class Money implements ValueObject<Money> {
    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        this.amount = amount.setScale(2, RoundingMode.HALF_EVEN);
        this.currency = currency;
    }

    public Money add(Money other) {
        validateCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(new BigDecimal(quantity)), this.currency);
    }

    private void validateCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
    }

    // 实现ValueObject接口
    @Override
    public boolean sameValueAs(Money other) {
        return other != null &&
                this.amount.compareTo(other.amount) == 0 &&
                this.currency.equals(other.currency);
    }
}

// 地址值对象
public class Address implements ValueObject<Address> {
    private final String province;
    private final String city;
    private final String district;
    private final String detail;
    private final String postalCode;

    public Address(String province, String city, String district, String detail, String postalCode) {
        this.province = province;
        this.city = city;
        this.district = district;
        this.detail = detail;
        this.postalCode = postalCode;
    }

    // getters...

    @Override
    public boolean sameValueAs(Address other) {
        return other != null &&
                Objects.equals(province, other.province) &&
                Objects.equals(city, other.city) &&
                Objects.equals(district, other.district) &&
                Objects.equals(detail, other.detail) &&
                Objects.equals(postalCode, other.postalCode);
    }
}

// 订单状态枚举
public enum OrderStatus {
    PENDING_PAYMENT,    // 待支付
    PAID,               // 已支付
    CONFIRMED,          // 已确认
    PROCESSING,         // 处理中
    SHIPPED,            // 已发货
    DELIVERED,          // 已送达
    COMPLETED,          // 已完成
    CANCELLED,          // 已取消
    REFUNDED            // 已退款
}