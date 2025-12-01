package rut.miit.airportweb.dao.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rut.miit.airportweb.dao.entity.UserEntity;

import java.util.List;
import java.util.Optional;


/**
 * Пример репозитория для сущности пользователя.
 * В нём представлены примерные методы, возможно потом будем добавлять другие методы.
 * Как видно, можно кастомные запросы писать под @Query, но там используется не нативный SQL, а
 * HQL (Hibernate Query Language)
 * В целом по названию методов думаю понятно что они делают
 * */
@Repository
public interface UserRepository {

    Optional<UserEntity> findByUsername(String username);

    @Query("SELECT DISTINCT u FROM user_entity u JOIN FETCH u.passenger")
    List<UserEntity> findAllOptimized();

    @Query("SELECT u FROM user_entity u WHERE u.firstName=:firstName AND u.lastName=:lastName")
    Optional<UserEntity> findByFirstNameAndLastName(String firstName, String lastName);

}
