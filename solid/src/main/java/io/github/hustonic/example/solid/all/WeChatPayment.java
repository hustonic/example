package io.github.hustonic.example.solid.all;

/**
 * @author Huston
 */
public class WeChatPayment implements Payment {

    /**
     * 支付服务提供商
     */
    private String provideName = "wechat";

    @Override
    public void pay(Order order) {
        //获取金额
        int amount = order.amount();
        //调用微信的接口进行支付
    }
}
