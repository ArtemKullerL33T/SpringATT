package com.example.springatt.repositories;

import com.example.springatt.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    List<Cart> findByPersonId(int id);

    @Modifying
    @Query(value = "DELETE FROM product_cart WHERE product_id=?1", nativeQuery = true)
    void deleteCartByProductId(int id);


}
