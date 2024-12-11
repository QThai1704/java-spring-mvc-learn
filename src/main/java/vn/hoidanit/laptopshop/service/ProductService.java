package vn.hoidanit.laptopshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.domain.dto.ReceiverDTO;
import vn.hoidanit.laptopshop.repository.CartDetailRepository;
import vn.hoidanit.laptopshop.repository.CartRepository;
import vn.hoidanit.laptopshop.repository.OrderDetailRepository;
import vn.hoidanit.laptopshop.repository.OrderRepository;
import vn.hoidanit.laptopshop.repository.ProductRepository;
import vn.hoidanit.laptopshop.service.specification.ProductSpecs;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ProductService(ProductRepository productRepository, CartRepository cartRepository,
            CartDetailRepository cartDetailRepository, UserService userService, OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.cartRepository = cartRepository;
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public Product saveProduct(Product newProduct) {
        return this.productRepository.save(newProduct);
    }

    // Fetch
    public Page<Product> fetchProductsGreateThanMinPrice(Pageable pageable, double price) {
        return this.productRepository.findAll(ProductSpecs.priceGe(price), pageable);
    }

    public Page<Product> fetchProductsLessThanMaxPrice(Pageable pageable, double price) {
        return this.productRepository.findAll(ProductSpecs.priceLe(price), pageable);
    }

    public Page<Product> fetchProductsWithName(Pageable pageable, String name) {
        return this.productRepository.findAll(ProductSpecs.nameLike(name), pageable);
    }

    public Page<Product> fetchProductsWithFactory(Pageable pageable, String factory) {
        return this.productRepository.findAll(ProductSpecs.factoryLike(factory), pageable);
    }

    public Page<Product> fetchProductsWithFactorys(Pageable pageable, List<String> factory) {
        return this.productRepository.findAll(ProductSpecs.factorysIn(factory), pageable);
    }

    public Page<Product> fetchProductsAboutMaxAndMin(Pageable pageable, String price) {
        double min = 0;
        double max = 0;
        if (price.equals("10-15")) {
            min = 10000000;
            max = 15000000;
            return this.productRepository.findAll(ProductSpecs.priceAnd(min, max), pageable);
        } else if (price.equals("15-30")) {
            min = 15000000;
            max = 30000000;
            return this.productRepository.findAll(ProductSpecs.priceAnd(min, max), pageable);
        } else if (price.equals("tren-30")) {
            min = 30000000;
            return this.productRepository.findAll(ProductSpecs.priceGe(min), pageable);
        } else if (price.equals("duoi-10")) {
            max = 10000000;
            return this.productRepository.findAll(ProductSpecs.priceLe(max), pageable);
        }
        return this.productRepository.findAll(pageable);
    }

    public Page<Product> fetchProducts(Pageable pageable) {
        return this.productRepository.findAll(pageable);
    }

    public Product getProductById(long id) {
        return this.productRepository.findById(id);
    }

    // Delete
    public void deleteProduct(long id) {
        this.productRepository.deleteById(id);
    }

    public void handleAddProductToCart(String email, long productId, HttpSession session) {
        User user = this.userService.getUserByEmail(email);
        if (user != null) {
            Cart cart = this.cartRepository.findByUser(user);
            if (cart == null) {
                Cart newCart = new Cart();
                newCart.setUser(user);
                newCart.setSum(0);
                cart = this.cartRepository.save(newCart);
            }
            Product product = this.productRepository.findById(productId);
            CartDetail oldCartDetail = this.cartDetailRepository.findByCartAndProduct(cart, product);
            if (oldCartDetail == null) {
                CartDetail cartDetail = new CartDetail();
                cartDetail.setCart(cart);
                cartDetail.setProduct(product);
                cartDetail.setQuantity(1);
                cartDetail.setPrice(product.getPrice());
                this.cartDetailRepository.save(cartDetail);
                int sum = cart.getSum() + 1;
                cart.setSum(sum);
                this.cartRepository.save(cart);
                session.setAttribute("sum", sum);
            } else {
                oldCartDetail.setQuantity(oldCartDetail.getQuantity() + 1);
                this.cartDetailRepository.save(oldCartDetail);
            }
        }
    }

    public void deleteCartProduct(long id, HttpSession session) {
        Optional<CartDetail> cartOptional = this.cartDetailRepository.findById(id);
        if (cartOptional.isPresent()) {
            CartDetail cartDetail = cartOptional.get();
            Cart cart = cartDetail.getCart();
            this.cartDetailRepository.deleteById(id);
            if (cart.getSum() > 1) {
                int sum = cart.getSum() - 1;
                cart.setSum(sum);
                this.cartRepository.save(cart);
                session.setAttribute("sum", sum);
            } else {
                this.cartRepository.delete(cart);
                session.setAttribute("sum", 0);
            }
        }
    }

    public Cart fetchByUser(User user) {
        return this.cartRepository.findByUser(user);
    }

    public void handleUpdateCartBeforeCheckout(List<CartDetail> cartDetails) {
        for (CartDetail cartDetail : cartDetails) {
            Optional<CartDetail> cdOptional = this.cartDetailRepository.findById(cartDetail.getId());
            if (cdOptional.isPresent()) {
                CartDetail currentCartDetail = cdOptional.get();
                currentCartDetail.setQuantity(cartDetail.getQuantity());
                this.cartDetailRepository.save(currentCartDetail);
            }
        }
    }

    public void handlePlaceOrder(User user, ReceiverDTO receiverDTO, HttpSession session) {
        double totalPrice = 0;
        Order order = Order.builder()
                .user(user)
                .receiverName(receiverDTO.getReceiverName())
                .receiverAddress(receiverDTO.getReceiverAddress())
                .receiverPhone(receiverDTO.getReceiverPhone())
                .build();
        order = this.orderRepository.save(order);

        // create OrderDetail
        // Get cart by user
        Cart cart = this.cartRepository.findByUser(user);
        if (cart != null) {
            List<CartDetail> cartDetails = cart.getCartDetails();
            for (CartDetail cartDetail : cartDetails) {
                OrderDetail orderDetail = OrderDetail.builder()
                        .order(order)
                        .product(cartDetail.getProduct())
                        .quantity(cartDetail.getQuantity())
                        .price(cartDetail.getPrice())
                        .build();
                this.orderDetailRepository.save(orderDetail);
            }

            // Delete records in table cart_detail
            for (CartDetail cartDetail : cartDetails) {
                totalPrice += cartDetail.getPrice() * cartDetail.getQuantity();
                this.cartDetailRepository.delete(cartDetail);
            }

            // Delete cart
            order.setTotalPrice(totalPrice);
            this.orderRepository.save(order);
            this.cartRepository.delete(cart);

            // Update attribute sum in session
            session.setAttribute("sum", 0);
        }
    }
}
