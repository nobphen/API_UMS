package com.ums.people.infrastructure;

import com.ums.people.domain.GeoArea;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Database access for provinces, districts, communes and villages (table geo_areas). */
public interface GeoAreaRepository extends JpaRepository<GeoArea, Long> {

    /**
     * Whether an area already uses this code, inactive ones included (the database keeps the code UNIQUE).
     * <pre>
     * SELECT g.id FROM geo_areas g WHERE g.code = ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param code official area code, e.g. 0102
     * @return true when the code is taken
     */
    boolean existsByCode(String code);

    /**
     * The dropdown list: the provinces (parentId null), or the areas directly inside one area.
     * q filters by Khmer or English name (empty q = all); inactive areas are left out unless asked for.
     * <pre>
     * SELECT g.* FROM geo_areas g
     * WHERE (? IS NULL AND g.level = 'PROVINCE' OR g.parent_id = ?)
     *   AND (? = TRUE OR g.is_active = TRUE)
     *   AND (LOWER(g.name_en) LIKE LOWER('%' || ? || '%') OR g.name_kh LIKE '%' || ? || '%')
     * ORDER BY g.name_en
     * </pre>
     *
     * @param parentId        parent area id, or null for the provinces
     * @param q               search text; "" = all
     * @param includeInactive true also returns switched-off areas
     * @return the areas, ordered by English name
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
