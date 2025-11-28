package com.company.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

//地址值对象
@Data
@Embeddable
public class Address {
    //收货人姓名
    @Column(nullable = false, length = 50)
    private String receiverName;
    //收货人电话
    @Column(nullable = false, length = 15)
    private String receiverPhone;
    //省市区三级地址
    @Column(nullable = false, length = 100)
    private String province;
    //省市区三级地址
    @Column(nullable = false, length = 100)
    private String city;
    //省市区三级地址
    @Column(nullable = false, length = 100)
    private String district;
    //详细地址
    @Column(nullable = false, length = 200)
    private String detailAddress;

}
