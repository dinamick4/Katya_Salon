package org.cosmetology.repository;

import org.cosmetology.model.Skin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Предназначен для работы с таблицей 'skin'
 */
public interface SkinRepository extends JpaRepository<Skin, Long> {

    /**
     * поиск записи по типу кожи
     *
     * @param skinType тип кожи
     */
    @Query("SELECT s FROM Skin s WHERE s.skinType = :skinType")
    List<Skin> findBySkinType(@Param("skinType") String skinType);
}