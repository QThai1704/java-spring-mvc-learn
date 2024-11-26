package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.service.OrderService;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/admin/order")
    public String getOrder(Model model) {
        List<Order> orders = this.orderService.fetchOrders();
        model.addAttribute("orders", orders);
        return "admin/order/show";
    }

    @GetMapping("/admin/order/{id}")
    public String detailOrder(@PathVariable long id, Model model) {
        Optional<Order> order = this.orderService.fetchOrderById(id);
        List<OrderDetail> orderDetails = order.get().getOrderDetails();
        model.addAttribute("orderDetails", orderDetails);
        return "admin/order/detail";
    }

    @GetMapping("/admin/order/update/{id}")
    public String updateOrder(Model model, @PathVariable long id) {
        Optional<Order> newOrder = this.orderService.fetchOrderById(id);
        model.addAttribute("newOrder", newOrder.get());
        return "admin/order/update";
    }

    @PostMapping("/admin/order/update")
    public String updateOrder(@ModelAttribute("newOrder") Order order) {
        Order currentOrder = this.orderService.fetchOrderById(order.getId()).get();
        currentOrder.setStatus(order.getStatus());
        this.orderService.saveOrder(currentOrder);
        return "redirect:/admin/order";
    }

    @GetMapping("/admin/order/delete/{id}")
    public String deleteOrder(Model model, @PathVariable long id) {
        Order newOrder = this.orderService.fetchOrderById(id).get();
        model.addAttribute("newOrder", newOrder);
        return "admin/order/delete";
    }

    @PostMapping("/admin/order/delete")
    public String deleteOrder(@ModelAttribute("newOrder") Order order) {
        this.orderService.deleteOrder(order.getId());
        return "redirect:/admin/order";
    }
}
