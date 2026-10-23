package com.ums.people.infrastructure;

import com.ums.people.domain.Country;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CountryRepository extends JpaRepository<Country, String> {

    /** Active countries for a dropdown, Cambodia first, then by English name; q filters by Khmer or English name or code. */
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
