package vn.hoidanit.laptopshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hoidanit.laptopshop.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Product save(Product newProduct);

    Product findById(long id);

    Product deleteById(long id);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findAll(Specification specification, Pageable pageable);
}
