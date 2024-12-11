package vn.hoidanit.laptopshop.domain;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.hoidanit.laptopshop.util.constant.PaymentStatusEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    double totalPrice;
    String receiverName;
    String receiverAddress;
    String receiverPhone;
    String status;

    @Column(columnDefinition = "MEDIUMTEXT")
    String paymentRef;

    @Enumerated(EnumType.STRING)
    PaymentStatusEnum paymentStatus;
    String paymentMethod;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // OrderDetail
    @OneToMany(mappedBy = "order")
    List<OrderDetail> orderDetails;
}
