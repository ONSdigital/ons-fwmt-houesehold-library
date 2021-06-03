package uk.gov.ons.fwmt.household.library.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.cache.GatewayCacheHh;
import uk.gov.ons.fwmt.household.library.repository.GatewayCacheRepository;

/**
 * This class is bare-bones because it's a simple connector between the rest of the code and the caching implementation
 * Please don't subvert this class by touching the GatewayCacheRepository
 * If we ever change from a database to redis, this class will form the breaking point
 */

@Slf4j
@Service
public class GatewayCacheService {
  public final GatewayCacheRepository repository;

  public GatewayCacheService(GatewayCacheRepository repository) {
    this.repository = repository;
  }

  public GatewayCacheHh getById(String caseId) {
    return repository.findByCaseId(caseId);
  }

  public GatewayCacheHh getByOriginalCaseId(String caseId) {
    return repository.findByOriginalCaseId(caseId);
  }

  public boolean doesEstabUprnExist(String uprn) {
    return repository.existsByEstabUprn(uprn);
  };

  public boolean doesEstabUprnAndTypeExist(String uprn, int type) {
    return repository.existsByEstabUprnAndType(uprn, type);}

  public boolean doesUprnAndTypeExist(String estabUprn, int type) {
    return repository.existsByUprnAndType(estabUprn, type);}

  public String getEstabCaseId(String estabUprn) {
    return repository.findByEstabUprn(estabUprn);
  }

  public String getUprnCaseId(String uprn) {
    return repository.findByUprn(uprn);
  }

  public GatewayCacheHh save(GatewayCacheHh cache) {
    return repository.save(cache);
  }


}
