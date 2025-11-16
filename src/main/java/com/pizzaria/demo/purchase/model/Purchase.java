package com.pizzaria.demo.purchase.model;

import com.pizzaria.demo.itemProduct.model.ItemProduct;
import com.pizzaria.demo.user.model.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemProduct> items = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private LocalDateTime created;

    @Column(unique = true, nullable = true)
    private Long paymentId;


    public Purchase(User user, List<ItemProduct> items) {
        this.user = user;
        this.items = items;
        this.enabled = true;
        this.created = LocalDateTime.now();
        calculateTotal();
    }

    @PrePersist
    protected void onCreate() {
        if (created == null) {
            created = LocalDateTime.now();
        }
        enabled = true;
        if (status == null) {
            status = Status.PENDING; // ou o padrão que você quiser
        }
    }

    public Purchase() {
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getId() {
        return id;
    }

    public void calculateTotal() {
        this.total = items.stream().map(ItemProduct::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<ItemProduct> getItems() {
        return items;
    }

    public void setItems(List<ItemProduct> items) {
        this.items = items;
        calculateTotal();
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}
