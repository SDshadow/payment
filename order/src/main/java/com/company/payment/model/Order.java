package com.company.payment.model;


import java.time.LocalDateTime;
import java.util.List;

//订单聚合根
// 订单聚合根
public class Order implements AggregateRoot<Order> {
    private OrderId orderId;
    private String customerId;
    private OrderStatus status;
    private Address shippingAddress;
    private Address billingAddress;
    private List<OrderItem> items;
    private Money totalAmount;
    private Money discountAmount;
    private Money shippingFee;
    private Money finalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String cancelReason;

    // 私有构造函数，通过工厂方法创建
    private Order(String customerId, Address shippingAddress, Address billingAddress) {
        this.orderId = OrderId.generate();
        this.customerId = customerId;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.items = new ArrayList<>();
        this.totalAmount = new Money(BigDecimal.ZERO, Currency.getInstance("CNY"));
        this.discountAmount = new Money(BigDecimal.ZERO, Currency.getInstance("CNY"));
        this.shippingFee = new Money(BigDecimal.ZERO, Currency.getInstance("CNY"));
        this.finalAmount = new Money(BigDecimal.ZERO, Currency.getInstance("CNY"));
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 工厂方法 - 创建新订单
    public static Order create(String customerId, Address shippingAddress, Address billingAddress) {
        Order order = new Order(customerId, shippingAddress, billingAddress);
        order.registerDomainEvent(new OrderCreatedEvent(OrderId.generate(), customerId));
        return order;
    }

    // 核心业务方法 - 添加订单项
    public void addItem(OrderItem item) {
        if (!this.status.equals(OrderStatus.PENDING_PAYMENT)) {
            throw new IllegalStateException("Cannot add item to order in current status: " + status);
        }

        this.items.add(item);
        recalculateAmounts();
        this.updatedAt = LocalDateTime.now();
    }

    // 核心业务方法 - 支付
    public void pay(Money paidAmount) {
        if (!this.status.equals(OrderStatus.PENDING_PAYMENT)) {
            throw new IllegalStateException("Order cannot be paid in current status: " + status);
        }

        if (!paidAmount.sameValueAs(this.finalAmount)) {
            throw new IllegalArgumentException("Paid amount does not match order amount");
        }

        this.status = OrderStatus.PAID;
        this.updatedAt = LocalDateTime.now();
        registerDomainEvent(new OrderPaidEvent(this.orderId, this.customerId, paidAmount));
    }

    // 核心业务方法 - 取消订单
    public void cancel(String reason) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + status);
        }

        this.status = OrderStatus.CANCELLED;
        this.cancelReason = reason;
        this.updatedAt = LocalDateTime.now();
        registerDomainEvent(new OrderCancelledEvent(this.orderId, this.customerId, reason));
    }

    // 核心业务方法 - 确认订单
    public void confirm() {
        if (!this.status.equals(OrderStatus.PAID)) {
            throw new IllegalStateException("Only paid orders can be confirmed");
        }

        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    // 业务规则 - 检查订单是否可取消
    public boolean canBeCancelled() {
        return this.status.equals(OrderStatus.PENDING_PAYMENT) ||
                this.status.equals(OrderStatus.PAID) ||
                this.status.equals(OrderStatus.CONFIRMED);
    }

    // 重新计算金额
    private void recalculateAmounts() {
        Money total = items.stream()
                .map(OrderItem::calculateSubtotal)
                .reduce(new Money(BigDecimal.ZERO, Currency.getInstance("CNY")), Money::add);

        this.totalAmount = total;
        this.finalAmount = total.add(shippingFee).subtract(discountAmount);
    }

    // 设置运费
    public void setShippingFee(Money shippingFee) {
        this.shippingFee = shippingFee;
        recalculateAmounts();
        this.updatedAt = LocalDateTime.now();
    }

    // 应用折扣
    public void applyDiscount(Money discount) {
        this.discountAmount = discount;
        recalculateAmounts();
        this.updatedAt = LocalDateTime.now();
    }

    // getters...

    @Override
    public boolean sameIdentityAs(Order other) {
        return other != null && Objects.equals(this.orderId, other.orderId);
    }
}

// 订单ID值对象
public class OrderId implements ValueObject<OrderId> {
    private final String id;

    public OrderId(String id) {
        this.id = id;
    }

    public static OrderId generate() {
        return new OrderId("ORD_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    // getter and sameValueAs implementation...
}