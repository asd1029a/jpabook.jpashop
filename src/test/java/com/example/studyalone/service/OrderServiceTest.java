package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class OrderServiceTest {

    @PersistenceContext
    EntityManager em;

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    void 상품주문() throws Exception{
        Member member = createMember();
        Item item = createBook("helloWorld", 10000, 10);
        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);


        Order getOrder = orderRepository.findOne(orderId);


        Assertions.assertThat(OrderStatus.ORDER).isEqualTo(getOrder.getStatus());
        Assertions.assertThat(1).isEqualTo(getOrder.getOrderItems().size());
        Assertions.assertThat(20000).isEqualTo(getOrder.getTotalPrice());
        Assertions.assertThat(8).isEqualTo(item.getStockQuantity());
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception{
        Member member = createMember();
        Item item = createBook("hai", 10000, 10);

        int orderCount = 11;

        try{
            orderService.order(member.getId(),item.getId(),orderCount);
        }catch (NotEnoughStockException e){
            fail("재고 수량 부족 예외 발생!!!");
        }

    }
    @Test
    void 주문취소(){
        Member member = createMember();
        Item item = createBook("책A",10000,10);
        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        orderService.cancelOrder(orderId);

        Order getOrder = orderRepository.findOne(orderId);

        Assertions.assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.CANCEL);
        Assertions.assertThat(item.getStockQuantity()).isEqualTo(10);

    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울","금천구","331-12"));
        em.persist(member);
        return member;
    }

    private Book createBook(String name, int price,int stockQuantity){
        Book book = new Book();
        book.setName(name);
        book.setStockQuantity(stockQuantity);
        book.setPrice(price);
        em.persist(book);
        return book;
    }

}