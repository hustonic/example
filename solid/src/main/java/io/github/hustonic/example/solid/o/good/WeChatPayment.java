package io.github.hustonic.example.solid.o.good;

public class WeChatPayment implements Payment {

    /**
     * 支付服务提供商
     */
    private String providerName = "wechat";

    @Override
    public void pay(Order order) {
        //获取金额
        int amount = order.amount();
        //调用微信的接口进行支付
    }
}