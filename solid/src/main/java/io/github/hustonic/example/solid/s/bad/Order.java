package io.github.hustonic.example.solid.s.bad;

/**
 * 订单类
 */
public class Order {

    private int amount;

    public int amount() {
        //计算订单金额
        return amount;
    }

    public void pay() {
        //获取金额
        int amount = amount();
        //调用支付宝的接口进行支付
    }
}
