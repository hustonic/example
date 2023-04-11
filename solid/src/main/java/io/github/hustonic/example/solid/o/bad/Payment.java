package io.github.hustonic.example.solid.o.bad;

public class Payment {

    /**
     * 支付服务提供商
     */
    private String providerName;

    public void pay(Order order) {
        //获取金额
        int amount = order.amount();

        if (providerName.equals("alipay")) {
            //调用支付宝的接口进行支付
        } else if (providerName.equals("wechat")) {
            //调用微信的接口进行支付
        }
    }
}