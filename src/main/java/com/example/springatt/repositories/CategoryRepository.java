package com.example.springatt.repositories;

import com.example.springatt.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    com.example.springatt.models.Category findByName(String name);
}
