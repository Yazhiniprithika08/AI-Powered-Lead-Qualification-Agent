package com.sales.leadqualifier.repository;

import com.sales.leadqualifier.model.Lead;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

/**
 * Repository interface extending MongoRepository to perform CRUD and custom queries
 * on the Lead collection in MongoDB.
 */
public interface LeadRepository extends MongoRepository<Lead, String> {
    
    /**
     * Search leads by company name (case-insensitive substring match).
     *
     * @param companyName company name snippet to query
     * @return list of matching leads
     */
    List<Lead> findByCompanyNameContainingIgnoreCase(String companyName);
}
