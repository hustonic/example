package io.github.hustonic.example.solid.d.good;

public class Payment {

    //依赖接口或抽象类，后续如果新增了不同的可支付的实体，只需增加新的类实现Payable接口，不需要修改现有代码

    public void pay(Payable payable) {
        //获取金额
        int amount = payable.amount();
        //调用支付宝的接口进行支付
    }
}