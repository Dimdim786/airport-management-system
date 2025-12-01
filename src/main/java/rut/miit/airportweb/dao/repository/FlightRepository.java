package rut.miit.airportweb.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rut.miit.airportweb.dao.entity.FlightEntity;

import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<FlightEntity, Integer> {

    @Query("SELECT f FROM flight_entity f WHERE f.flightNumber = :flightNumber")
    Optional<FlightEntity> findByFlightNumber(String flightNumber);

    @Query("SELECT f FROM flight_entity f WHERE f.departureTime = :departureTime AND f.arrivalTime = :arrivalTime")
    Optional<FlightEntity> findByDepartureTimeAndArrivalTime (String departureTime, String arrivalTime);

    @Query("SELECT f FROM flight_entity f WHERE f.departureCity = :departureCity AND f.arrivalCity = :arrivalCity")
    Optional<FlightEntity> findByDepartureCityAndArrivalCity(String departureCity, String arrivalCity);

}
