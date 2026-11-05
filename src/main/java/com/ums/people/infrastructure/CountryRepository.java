package com.ums.people.infrastructure;

import com.ums.people.domain.Country;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Countries; the id is the ISO code (KH, TH …), so findById("KH") finds Cambodia. */
public interface CountryRepository extends JpaRepository<Country, String> {

    /**
     * Active countries for a dropdown, Cambodia first, then by English name.
     * q filters by English or Khmer name, nationality or exact code (empty q = all);
     * withNationality = true leaves out territories with no nationality.
     * <pre>
     * SELECT c.* FROM countries c
     * WHERE c.is_active = TRUE
     *   AND (? = FALSE OR c.nationality_en IS NOT NULL)
     *   AND (LOWER(c.name_en) LIKE LOWER('%' || ? || '%') OR c.name_kh LIKE '%' || ? || '%'
     *        OR LOWER(c.nationality_en) LIKE LOWER('%' || ? || '%') OR UPPER(c.code) = UPPER(?))
     * ORDER BY CASE WHEN c.code = 'KH' THEN 0 ELSE 1 END, c.name_en
     * </pre>
     *
     * @param q               search text; "" = all
     * @param withNationality true for the Nationality list
     * @return the countries, Cambodia first
     */
    @Query("""
            select c from Country c
            where c.active = true
              and (:withNationality = false or c.nationalityEn is not null)
              and (lower(c.nameEn) like lower(concat('%', :q, '%')) or c.nameKh like concat('%', :q, '%')
                   or lower(c.nationalityEn) like lower(concat('%', :q, '%')) or upper(c.code) = upper(:q))
            order by case when c.code = 'KH' then 0 else 1 end, c.nameEn
            """)
    List<Country> findForList(String q, boolean withNationality);
}
