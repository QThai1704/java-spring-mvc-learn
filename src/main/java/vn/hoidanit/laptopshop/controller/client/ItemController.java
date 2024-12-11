package vn.hoidanit.laptopshop.controller.client;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.domain.dto.ReceiverDTO;
import vn.hoidanit.laptopshop.service.ProductService;
import vn.hoidanit.laptopshop.service.VNPayService;
import vn.hoidanit.laptopshop.util.constant.PaymentStatusEnum;

@Controller
public class ItemController {
    private final ProductService productService;
    private final VNPayService vNPayService;

    public ItemController(ProductService productService, VNPayService vNPayService) {
        this.productService = productService;
        this.vNPayService = vNPayService;
    }

    @GetMapping("/product/{id}")
    public String getItem(Model model, @PathVariable long id) {
        Product product = this.productService.getProductById(id);
        model.addAttribute("product", product);
        return "client/product/detail";
    }

    @PostMapping("/add-product-to-cart/{id}")
    public String addProductToCart(@PathVariable long id, HttpServletRequest request) {
        long productId = id;
        String email = (String) request.getSession().getAttribute("email");
        this.productService.handleAddProductToCart(email, productId, request.getSession(), 1);
        return "redirect:/";
    }

    @GetMapping("/cart")
    public String getCartPage(HttpServletRequest request, Model model) {
        User currentUser = new User();
        long id = (long) request.getSession(false).getAttribute("id");
        currentUser.setId(id);
        Cart cart = this.productService.fetchByUser(currentUser);
        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();
        double total = 0;
        for (CartDetail item : cartDetails) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", total);
        model.addAttribute("cart", cart);
        return "client/cart/show";
    }

    @PostMapping("/delete-cart-product/{id}")
    public String postDeleteCartProduct(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession();
        this.productService.deleteCartProduct(id, session);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String getCheckOutPage(Model model, HttpServletRequest request) {
        User currentUser = new User();
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        Cart cart = this.productService.fetchByUser(currentUser);

        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();

        double totalPrice = 0;
        for (CartDetail cd : cartDetails) {
            totalPrice += cd.getPrice() * cd.getQuantity();
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);

        return "client/cart/checkout";
    }

    @PostMapping("/confirm-checkout")
    public String getCheckOutPage(@ModelAttribute("cart") Cart cart) {
        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();
        this.productService.handleUpdateCartBeforeCheckout(cartDetails);
        return "redirect:/checkout";
    }

    @PostMapping("/place-order")
    public String handlePlaceOrder(HttpServletRequest request,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverAddress") String receiverAddress,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("totalPrice") String totalPrice) throws NumberFormatException, UnsupportedEncodingException {
        User currentUser = new User();
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);
        ReceiverDTO newReceiverDTO = ReceiverDTO.builder()
                .receiverName(receiverName)
                .receiverAddress(receiverAddress)
                .receiverPhone(receiverPhone)
                .paymentMethod(paymentMethod)
                .build();
        final String uuid = UUID.randomUUID().toString().replace("-", "");
        this.productService.handlePlaceOrder(currentUser, newReceiverDTO, session, uuid);
        if (!paymentMethod.equals("COD")) {
            // todo: redirect to VNPAY
            String ip = this.vNPayService.getIpAddress(request);
            String vnpUrl = this.vNPayService.generateVNPayURL(Double.parseDouble(totalPrice), uuid, ip);
            return "redirect:" + vnpUrl;
        }

        return "redirect:/thanks";
    }

    @GetMapping("/thanks")
    public String getThankYouPage(Model model,
            @RequestParam("vnp_ResponseCode") Optional<String> vnpayResponseCode,
            @RequestParam("vnp_TxnRef") Optional<String> paymentRef) {
        if (vnpayResponseCode.isPresent() && paymentRef.isPresent()) {
            // thanh toán qua VNPAY, cập nhật trạng thái order
            PaymentStatusEnum paymentStatus = vnpayResponseCode.get().equals("00")
                    ? PaymentStatusEnum.PAYMENT_SUCCEED
                    :  PaymentStatusEnum.PAYMENT_FAILED;
            this.productService.updatePaymentStatus(paymentRef.get(), paymentStatus);
        }
        return "client/cart/thanks";
    }

    @GetMapping("/products")
    public String getProductPage(Model model,
            @RequestParam("page") Optional<String> pageOptional,
            @RequestParam("name") Optional<String> nameOptional,
            @RequestParam("factory") Optional<String> factoryOptional,
            @RequestParam("min-price") Optional<String> minPriceOptional,
            @RequestParam("max-price") Optional<String> maxPriceOptional,
            @RequestParam("price") Optional<String> priceOptional) {
        int page = 1;
        // String factory = factoryOptional.isPresent() ? factoryOptional.get() : "";
        // List<String> factorys = Arrays.asList(factoryOptional.get().split(","));
        // double minPrice = minPriceOptional.isPresent() ?
        // Double.parseDouble(minPriceOptional.get()) : 0;
        // double maxPrice = maxPriceOptional.isPresent() ?
        // Double.parseDouble(minPriceOptional.get()) : 100000000;
        // String price = priceOptional.isPresent() ? priceOptional.get() : "";
        try {
            if (pageOptional.isPresent()) {
                // convert from String to int
                page = Integer.parseInt(pageOptional.get());
            } else {
                // page = 1
            }
        } catch (Exception e) {
            // page = 1
            // TODO: handle exception
        }
        String name = nameOptional.isPresent() ? nameOptional.get() : "";
        Pageable pageable = PageRequest.of(page - 1, 6);
        Page<Product> prs = this.productService.fetchProductsWithName(pageable, name);
        // Page<Product> prs =
        // this.productService.fetchProductsGreateThanMinPrice(pageable, minPrice);
        // Page<Product> prs = this.productService.fetchProductsWithFactory(pageable,
        // factorys);
        // Page<Product> prs = this.productService.fetchProductsAboutMaxAndMin(pageable,
        // price);
        // Page<Product> prs =
        // this.productService.fetchProductsLessThanMaxPrice(pageable, maxPrice);
        List<Product> products = prs.getContent();
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", prs.getTotalPages());
        return "client/product/show";
    }
}
