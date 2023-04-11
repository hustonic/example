package io.github.hustonic.example.solid.d.good;

/**
 * 订单类
 */
public class Order implements Payable {

    private int amount;

    @Override
    public int amount() {
        //计算订单金额
        return amount;
    }

}
