package rut.miit.airportweb.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rut.miit.airportweb.dao.entity.TicketEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, Integer> {

    Optional<TicketEntity> findByTicketNumber(String ticket_number);

    @Query("SELECT DISTINCT t FROM ticket_entity t JOIN FETCH t.flight f WHERE f.flightNumber = :flightNumber")
    List<TicketEntity> findAllByFlight(String flightNumber);

    @Query("SELECT DISTINCT t FROM ticket_entity t JOIN FETCH t.passenger p WHERE p.passportNumber = :passportNumber")
    List<TicketEntity> findAllByPassportNumber(String passportNumber);

    @Modifying
    @Query("UPDATE ticket_entity t SET t.status = :ticketStatus WHERE t.ticketNumber = :ticketNumber")
    int updateTicketStatus(String ticketNumber, String ticketStatus);

    // Добавляем метод для получения после обновления
    default Optional<TicketEntity> updateTicketStatusAndGet(String ticketNumber, String ticketStatus) {
        int updated = updateTicketStatus(ticketNumber, ticketStatus);
        if (updated > 0) {
            return findByTicketNumber(ticketNumber);
        }
        return Optional.empty();
    }

    @Query("SELECT COUNT(t) FROM ticket_entity t WHERE t.flight.flightNumber = :flightNumber AND t.status = :status")
    long countByFlightFlightNumberAndStatus(
            @Param("flightNumber") String flightNumber,
            @Param("status") String status);

}
