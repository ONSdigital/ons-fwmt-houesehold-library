package uk.gov.ons.fwmt.household.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.fwmt.household.library.data.QuarantinedMessage;

import java.util.UUID;

public interface QuarantinedMessageRepository extends JpaRepository<QuarantinedMessage, UUID> {

}
