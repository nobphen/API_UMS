package com.ums.security.menu.domain;

import com.ums.common.entity.BaseEntity;
import com.ums.security.role.domain.Permission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** One item of the navigation menu; shown only to users holding its permission (none = shown to everyone). */
@Getter
@Setter
@Entity
@Table(name = "menus")
public class Menu extends BaseEntity {

    /** The parent item; null for a top-level item. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_kh")
    private String nameKh;

    /** Front-end route; null for a group that only holds children. */
    @Column(name = "path")
    private String path;

    @Column(name = "icon")
    private String icon;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id")
    private Permission permission;
}
