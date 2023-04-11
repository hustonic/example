package io.github.hustonic.example.solid.i.bad;

/**
 * @author Huston
 */
public interface Payment {

    void pay(Order order);

    /**
     * 借款
     */
    void borrow();
}
