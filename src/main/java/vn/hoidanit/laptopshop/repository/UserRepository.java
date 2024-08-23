package vn.hoidanit.laptopshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hoidanit.laptopshop.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User save(User hoidanit);

    User findByEmail(String email);

    User findById(long id);

    // User deleteBy(long id);

    boolean existsByEmail(String email);

    Page<User> findAll(Pageable pageable);
}
