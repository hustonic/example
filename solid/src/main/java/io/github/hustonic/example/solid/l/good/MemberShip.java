package io.github.hustonic.example.solid.l.good;

/**
 * 会员类
 */
public class MemberShip {

    //最好不重写父类实现的方法，只重写父类的抽象方法
    //如果某些行为与父类不一致，则不要继承父类。如果又想复用父类代码，则可以采用组合

    private Order order;

    public MemberShip(Order order) {
        this.order = order;
    }

    public int amount() {
        //计算订单金额
        return order.amount();
    }

    public void calReward() {
        //计算会员积分
    }
}
