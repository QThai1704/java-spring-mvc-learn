package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.service.ProductService;
import vn.hoidanit.laptopshop.service.UploadService;

@Controller
public class ProductController {
    private final ProductService productService;
    private final UploadService uploadService;

    public ProductController(ProductService productService, UploadService uploadService) {
        this.productService = productService;
        this.uploadService = uploadService;
    }

    // Show
    @GetMapping("/admin/product")
    public String getProduct(Model model, @RequestParam("page") Optional<String> pageOptional) {
        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                page = Integer.parseInt(pageOptional.get());
            }
        } catch (Exception e) {}
        Pageable pageable = PageRequest.of(page - 1, 2);
        Page<Product> prs = this.productService.fetchProducts(pageable);
        List<Product> listAllProduct = prs.getContent();
        model.addAttribute("products", listAllProduct);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", prs.getTotalPages());
        return "admin/product/show";
    }

    // Create
    @GetMapping("/admin/product/create")
    public String getCreateProduct(Model model) {
        model.addAttribute("newProduct", new Product());
        return "admin/product/create";
    }

    @PostMapping("/admin/product/create")
    public String postCreateProductPage(Model model, @ModelAttribute("newProduct") @Valid Product newProduct,
            BindingResult newProductBingingResult, @RequestParam("imgProductFile") MultipartFile file) {
        List<FieldError> errors = newProductBingingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(error.getField() + " - " + error.getDefaultMessage());
        }
        if (newProductBingingResult.hasErrors()) {
            return "admin/product/create";
        }
        String imgProduct = this.uploadService.handleSaveUploadFile(file, "product");
        double price = (double) newProduct.getPrice();
        long quantity = (long) newProduct.getQuantity();
        newProduct.setPrice(price);
        newProduct.setDetailDesc(newProduct.getDetailDesc());
        newProduct.setShortDesc(newProduct.getShortDesc());
        newProduct.setQuantity(quantity);
        newProduct.setFactory(newProduct.getFactory());
        newProduct.setTarget(newProduct.getTarget());
        newProduct.setImage(imgProduct);

        this.productService.saveProduct(newProduct);
        return "redirect:/admin/product";
    }

    // View
    @GetMapping("/admin/product/detail/{id}")
    public String getViewProduct(Model model, @PathVariable long id) {
        Product product = this.productService.getProductById(id);
        model.addAttribute("product", product);
        return "admin/product/detail";
    }

    // Update
    @GetMapping(value = "/admin/product/update/{id}")
    public String getUpdateProduct(Model model, @PathVariable long id) {
        Product product = this.productService.getProductById(id);
        model.addAttribute("newProduct", product);
        return "admin/product/update";
    }

    @PostMapping("/admin/product/update")
    public String postUpdateProduct(@ModelAttribute("newProduct") @Valid Product product,
            BindingResult newProductBingingResult, @RequestParam("imgProductFile") MultipartFile file) {
        Product currentProduct = this.productService.getProductById(product.getId());
        List<FieldError> errors = newProductBingingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(error.getField() + " - " + error.getDefaultMessage());
        }
        if (newProductBingingResult.hasErrors()) {
            return "admin/product/update";
        }
        if (currentProduct != null) {
            if (!file.isEmpty()) {
                String imgProduct = this.uploadService.handleSaveUploadFile(file, "product");
                currentProduct.setImage(imgProduct);
            }
            currentProduct.setName(product.getName());
            currentProduct.setPrice(product.getPrice());
            currentProduct.setDetailDesc(product.getDetailDesc());
            currentProduct.setShortDesc(product.getShortDesc());
            currentProduct.setQuantity(product.getQuantity());
            currentProduct.setFactory(product.getFactory());
            currentProduct.setTarget(product.getTarget());
            this.productService.saveProduct(currentProduct);
        }
        return "redirect:/admin/product";
    }

    // Delete
    @GetMapping("/admin/product/delete/{id}")
    public String getDeleteProduct(Model model, @ModelAttribute("newProduct") Product product) {
        Product currentProduct = this.productService.getProductById(product.getId());
        model.addAttribute("newProduct", currentProduct);
        return "admin/product/delete";
    }

    @PostMapping("/admin/product/delete")
    public String postDeleteProduct(Model model, @ModelAttribute("newProduct") Product product) {
        this.productService.deleteProduct(product.getId());
        return "redirect:/admin/product";
    }
}
