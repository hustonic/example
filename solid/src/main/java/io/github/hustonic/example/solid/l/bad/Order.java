package io.github.hustonic.example.solid.l.bad;

/**
 * 订单类
 */
public class Order {

    private int amount;

    public int amount() {
        //计算订单金额
        return amount;
    }

    public void deduct() {
        //扣减金额
    }
}
