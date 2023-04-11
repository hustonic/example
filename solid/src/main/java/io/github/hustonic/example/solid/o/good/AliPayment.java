package io.github.hustonic.example.solid.o.good;

public class AliPayment implements Payment {

    /**
     * 支付服务提供商
     */
    private String providerName = "alipay";

    @Override
    public void pay(Order order) {
        //获取金额
        int amount = order.amount();
        //调用支付宝的接口进行支付
    }
}