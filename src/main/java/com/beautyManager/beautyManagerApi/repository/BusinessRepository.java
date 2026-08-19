package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.BusinessesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<BusinessesEntity, UUID> {

    @Query("select b from BusinessesEntity b order by b.created_at asc")
    List<BusinessesEntity> findAllOrderedByCreation();
}