package com.ums.people.infrastructure;

import com.ums.people.domain.GeoArea;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GeoAreaRepository extends JpaRepository<GeoArea, Long> {

    /**
     * Whether an area already uses this code.
     * <pre>
     * SELECT g.id FROM geo_areas g WHERE g.code = ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param code area code
     * @return true when the code is taken
     */
    boolean existsByCode(String code);

    /**
     * The dropdown list: provinces (parentId empty), or the children of one area. q filters by Khmer or
     * English name (empty text matches all); inactive areas are left out unless asked for.
     */
    @Query("""
            select a from GeoArea a
            where (:parentId is null and a.level = com.ums.people.domain.GeoArea.Level.PROVINCE
                   or a.parent.id = :parentId)
              and (:includeInactive = true or a.active = true)
              and (lower(a.nameEn) like lower(concat('%', :q, '%')) or a.nameKh like concat('%', :q, '%'))
            order by a.nameEn
            """)
    List<GeoArea> findForList(Long parentId, String q, boolean includeInactive);
}
