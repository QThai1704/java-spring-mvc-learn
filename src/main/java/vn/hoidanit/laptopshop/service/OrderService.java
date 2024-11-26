package vn.hoidanit.laptopshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.repository.OrderDetailRepository;
import vn.hoidanit.laptopshop.repository.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    // Get
    public List<Order> fetchOrders() {
        return this.orderRepository.findAll();
    }

    public Optional<Order> fetchOrderById(long id) {
        if (this.orderRepository.findById(id).isPresent()) {
            return this.orderRepository.findById(id);
        }
        return null;
    }

    // Save
    public Order saveOrder(Order order) {
        return this.orderRepository.save(order);
    }

    // Detail
    public Order getOrderById(long id) {
        return this.orderRepository.findById(id).orElse(null);
    }

    // Update
    public Order updateOrder(Order order) {
        return this.orderRepository.save(order);
    }

    // Delete
    public void deleteOrder(long id) {
        Order order = this.getOrderById(id);
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            this.orderDetailRepository.delete(orderDetail);
        }
        this.orderRepository.delete(order);
    }
}
