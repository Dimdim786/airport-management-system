package rut.miit.airportweb.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rut.miit.airportweb.dao.entity.PassengerEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<PassengerEntity, Integer> {

    @Query("SELECT p FROM passenger_entity p WHERE p.passportNumber = :passportNumber")
    Optional<PassengerEntity> findByPassportNumber(String passportNumber);

    @Query("SELECT p FROM passenger_entity p WHERE p.phone = :phone")
    Optional<PassengerEntity> findByPhone(String phone);

    @Query("SELECT p FROM passenger_entity p WHERE p.email = :email")
    Optional<PassengerEntity> findByEmail(String email);

    @Query("SELECT DISTINCT p FROM passenger_entity p JOIN FETCH p.user u WHERE u.firstName = :firstName AND u.lastName=:lastName")
    List<PassengerEntity> findAllByFirstNameAndLastName(String firstName, String lastName);

    @Query("SELECT p FROM passenger_entity p JOIN FETCH p.user u WHERE u.username = :username")
    List<PassengerEntity> findByUsername(String username);
}
