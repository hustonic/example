package io.github.hustonic.example.solid.l.bad;

/**
 * 会员类
 */
public class MemberShip extends Order {

    @Override
    public void deduct() {
        throw new UnsupportedOperationException();
    }

    public void calReward() {
        //计算会员积分
    }
}
