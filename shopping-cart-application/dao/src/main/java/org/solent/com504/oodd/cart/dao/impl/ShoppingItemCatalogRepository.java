package org.solent.com504.oodd.cart.dao.impl;

import org.solent.com504.oodd.cart.model.dto.ShoppingItem;
import org.solent.com504.oodd.cart.model.dto.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingItemCatalogRepository  extends JpaRepository<ShoppingItem,Long>{
    
    @Query("SELECT i FROM ShoppingItem i WHERE i.name = :name")
    public ShoppingItem findItemByName(@Param("name")String name);
    
}
