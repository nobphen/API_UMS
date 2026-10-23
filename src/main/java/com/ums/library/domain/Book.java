package com.ums.library.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A title in the catalogue; the physical books on the shelf are its copies. */
@Getter
@Setter
@Entity
@Table(name = "books")
public class Book extends BaseEntity {

    @Column(name = "isbn")
    private String isbn;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "authors")
    private String authors;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "publish_year")
    private Integer publishYear;

    @Column(name = "edition")
    private String edition;

    @Column(name = "language")
    private String language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private LibraryCategory category;

    @Column(name = "cover_url")
    private String coverUrl;
}
