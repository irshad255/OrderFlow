package com.orderflow.repository;

import com.orderflow.entity.Order;
import com.orderflow.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByStatus(
            OrderStatus status,
            Pageable pageable
    );
}

    /*Is code mein kya naya hai?
- Page<Order> — orders ke saath pagination metadata bhi return karega, jaise total pages aur total elements.
- Pageable — batata hai kaunsa page chahiye, kitne records chahiye aur kis order mein sort karna hai.
- findByStatus() — sirf requested status ke orders fetch karega. Spring Data JPA method name se query generate karega.*/
