package io.github.hustonic.example.solid.d.bad;

public class Payment {

    public void pay(Order order) {
        //获取金额
        int amount = order.amount();
        //调用支付宝的接口进行支付
    }
}