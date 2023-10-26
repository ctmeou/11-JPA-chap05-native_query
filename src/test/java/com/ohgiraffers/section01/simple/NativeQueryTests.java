package com.ohgiraffers.section01.simple;

import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NativeQueryTests {

    //application 당 1개만
    private static EntityManagerFactory entityManagerFactory;

    //스레드 세이프 하지 않고, 요청 당 1개
    private EntityManager entityManager;

    @BeforeAll //junit에서 오는 어노테이션, 테스트가 진행되기 전에 한 번 진행된다.
    public static void initFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("jpatest");
    }

    @BeforeEach //테스트 하나가 진행되기 전에 한 번씩
    public void initManager() {
        entityManager = entityManagerFactory.createEntityManager();
    }

    @AfterAll //테스트가 끝나기 전에 한 번만
    public static void closeFactory() {
        entityManagerFactory.close();
    }

    @AfterEach //테스트가 끝날 떄마다 한 번씩
    public void closeManager() {
        entityManager.close();
    }

    @Test
    public void 결과_타입을_정의한_네이트브_쿼리_사용_테스트() {

        // given
        int menuCodeParameter = 15;

        // when
        //15번 메뉴코드에 대해 조회할 것이고, 오라클에서 구동되는 오라클 구문 작성
        //테이블과 컬럼명을 이용해서 작성
        String query = "SELECT MENU_CODE, MENU_NAME, MENU_PRICE, CATEGORY_CODE, ORDERABLE_STATUS" +
                      " FROM TBL_MENU WHERE MENU_CODE = ?"; //위치 기반이기 때문에 ?로 작성한다.
//        String query = "SELECT MENU_CODE, MENU_NAME, MENU_PRICE" + //일부 컬럼만 조회하기 -> 실패(ERROR: ORA-17006: 열 이름이 부적합합니다.)
//                " FROM TBL_MENU WHERE MENU_CODE = ?";
        //유의 사항
        //1. 일부 컬럼만 작성하면 수행 불가이고 menu 엔티티를 조회하고 싶으면 모든 엔티티가 조회되도록 작성해야 한다.
        //2. 위치 기반 파라미터만 설정이 가능하기 때문에 맞춰 작성해야 한다.
        Query nativeQuery = entityManager.createNativeQuery(query, Menu.class).setParameter(1, menuCodeParameter);
                                                    //수행하고자 하는 sql구문                      위치기반이기 때문에 menuCodeParameter
        Menu foundMenu = (Menu) nativeQuery.getSingleResult(); //영속성에서 관리하는 객체(실행하는 결과가 엔티티로 조회가 되기 때문에)

        // then
        assertNotNull(foundMenu);
        assertTrue(entityManager.contains(foundMenu)); //foundMenu가 영속성 관리 객체인지 확인(네이티브 쿼리로 실행을 하지만 영속성 컨테이너에서 관리하는 객체)
        System.out.println(foundMenu);

    }

    @Test
    public void 결과_타입을_정의할_수_없는_경우_조회_테스트() {

        // when
        String query = "SELECT MENU_NAME, MENU_PRICE FROM TBL_MENU";
        //엔티티로 조회할 것이면 모든 컬럼을 조회해야 하지만 엔티티가 아니면 모든 컬럼을 조회할 필요가 없기 때문에 menuName과 menuPrice만 죄회
        List<Object[]> menuList = entityManager.createNativeQuery(query).getResultList(); //작성한 타입과 매칭된 것이 없기 때문에 Object로 받음
        // List<Object[]> menuList = entityManager.createNativeQuery(query, Object[].class).getResultList();
                                    //첫번째 인자로 수행할 쿼리 구문 작성, 두 번째 인자 타입이 있다면 타입을 작성할 수 있으나 인자를 넘기면 오류 발생
                                    //타입이 없는 경우 타입을 지정하면 에러가 발생(MappingException: Unknown entity)

        // then
        assertNotNull(menuList);
        menuList.forEach(row -> {
            Stream.of(row).forEach(col -> System.out.print(col + " "));
            System.out.println();
        });

    }

    @Test
    public void 자동_결과_매핑을_사용한_조회_테스트() {

        // when
        String query = "SELECT" +
                      " A.CATEGORY_CODE, A.CATEGORY_NAME, A.REF_CATEGORY_CODE, NVL(V.MENU_COUNT, 0) MENU_COUNT" +
                      " FROM TBL_CATEGORY A" +
                      " LEFT JOIN (SELECT COUNT(*) AS MENU_COUNT, B.CATEGORY_CODE" +
                      "            FROM TBL_MENU B" +
                      "            GROUP BY B.CATEGORY_CODE) V ON (A.CATEGORY_CODE = V.CATEGORY_CODE)" +
                      "ORDER BY 1";

        Query nativeQuery = entityManager.createNativeQuery(query, "categoryCountAutoMapping"); //쿼리 구문이랑 resultSetMapping할 설정 작성
        List<Object[]> categoryList = nativeQuery.getResultList(); //object배열인 이유 : 하나의 엔티티와 하나의 컬럼으로 구성되어 있기 때문에 두 개가 배열에 전달이 되어 있는 상태

        // then
        assertNotNull(categoryList);
        assertTrue(entityManager.contains(categoryList.get(0)[0])); //categoryList가 영속성인지 확인 -> test 성공(= 엔티티는 영속성 매니저에서 관리되고 있다.)
        categoryList.forEach(row -> {
            Stream.of(row).forEach(col -> System.out.print(col + " "));
            System.out.println();
        });

    }

    @Test
    public void 수동_결과_매핑을_사용한_조회_테스트() {

        // when
        String query = "SELECT" +
                " A.CATEGORY_CODE, A.CATEGORY_NAME, A.REF_CATEGORY_CODE, NVL(V.MENU_COUNT, 0) MENU_COUNT" +
                " FROM TBL_CATEGORY A" +
                " LEFT JOIN (SELECT COUNT(*) AS MENU_COUNT, B.CATEGORY_CODE" +
                "            FROM TBL_MENU B" +
                "            GROUP BY B.CATEGORY_CODE) V ON (A.CATEGORY_CODE = V.CATEGORY_CODE)" +
                "ORDER BY 1";

        Query nativeQuery = entityManager.createNativeQuery(query, "categoryCountManualMapping");
        List<Object[]> categoryList = nativeQuery.getResultList();

        // then
        assertNotNull(categoryList);
        assertTrue(entityManager.contains(categoryList.get(0)[0]));
        categoryList.forEach(row -> {
            Stream.of(row).forEach(col -> System.out.print(col + " "));
            System.out.println();
        });

    }

}
