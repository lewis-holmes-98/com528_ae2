package org.solent.com504.oodd.cart.dao.impl;

import java.util.List;
import org.solent.com504.oodd.cart.model.dto.Invoice;
import org.solent.com504.oodd.cart.model.dto.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT i FROM Invoice i WHERE i.purchaser = :purchaser")
    public List<Invoice> findByPurchaser(@Param("purchaser") User purchaser);

    @Query("SELECT i FROM Invoice i WHERE i.uuid = :uuid")
    public Invoice findByuuid(@Param("uuid") String uuid);

}
