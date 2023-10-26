package com.ohgiraffers.section01.simple;

import javax.persistence.*;

@Entity(name = "category_section01")
@Table(name = "tbl_category")
@SqlResultSetMappings(
        value = {
                // 자동 엔티티 매핑 : @Column으로 매핑 설정이 되어 있는 경우 사용
                @SqlResultSetMapping(
                        name = "categoryCountAutoMapping", //resultSet의 이름
                        entities = {@EntityResult(entityClass = Category.class)},
                        columns = {@ColumnResult(name = "MENU_COUNT")}
                ),
                // 수동 엔티티 매핑 : @Column으로 매핑 설정이 되어 있지 않은 경우 사용
                //-> 명시적으로 작성하는 방법은 속성을 추가해야 된다.(필드명과 컬럼명을 작성)
                @SqlResultSetMapping(
                        name = "categoryCountManualMapping",
                        entities = {@EntityResult(entityClass = Category.class, fields = {
                                @FieldResult(name = "categoryCode", column = "category_code"),
                                @FieldResult(name = "categoryName", column = "category_name"),
                                @FieldResult(name = "refCategoryCode", column = "ref_category_code")
                        })},
                        columns = {@ColumnResult(name = "MENU_COUNT")}
                )
        }
)
public class Category {

    @Id
    private int categoryCode;
    private String categoryName;
    private Integer refCategoryCode;

    public Category() {
    }

    public Category(int categoryCode, String categoryName, Integer refCategoryCode) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.refCategoryCode = refCategoryCode;
    }

    public int getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(int categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getRefCategoryCode() {
        return refCategoryCode;
    }

    public void setRefCategoryCode(Integer refCategoryCode) {
        this.refCategoryCode = refCategoryCode;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryCode=" + categoryCode +
                ", categoryName='" + categoryName + '\'' +
                ", refCategoryCode=" + refCategoryCode +
                '}';
    }

}
