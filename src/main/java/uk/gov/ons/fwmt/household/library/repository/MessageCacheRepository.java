package uk.gov.ons.fwmt.household.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import uk.gov.ons.fwmt.household.library.data.MessageCache;

import javax.persistence.LockModeType;

@Repository
public interface MessageCacheRepository extends JpaRepository<MessageCache, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  MessageCache findByCaseIdAndAndMessageType(String caseId, String messageType);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  boolean existsByCaseId(String caseId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  boolean existsByCaseIdAndMessageType(String caseId, String messageType);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  MessageCache findByCaseId(String caseId);

}
