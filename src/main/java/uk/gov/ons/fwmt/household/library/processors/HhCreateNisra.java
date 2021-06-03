package uk.gov.ons.fwmt.household.library.processors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.cache.GatewayCacheHh;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.processor.InboundHhProcessor;
import uk.gov.ons.census.fwmt.common.processor.ProcessorKeyHh;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.fwmt.routing.http.comet.CometRestClient;
import uk.gov.ons.fwmt.routing.service.GatewayCacheService;
import uk.gov.ons.fwmt.routing.service.RoutingValidator;
import uk.gov.ons.fwmt.routing.service.converter.HhCreateConverter;

import java.time.Instant;

@Qualifier("Create")
@Service
public class HhCreateNisra implements InboundHhProcessor<FwmtActionInstruction> {

  private static final ProcessorKeyHh key = ProcessorKeyHh.builder()
      .actionInstruction(ActionInstructionType.CREATE.toString())
      .surveyName("CENSUS")
      .addressType("HH")
      .addressLevel("U")
      .build();

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  @Override
  public ProcessorKeyHh getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.CREATE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("HH")
          && rmRequest.getOa().startsWith("N");
//          && (cache == null || !cache.existsInFwmt);
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, Instant messageReceivedTime) throws GatewayException {
    CaseRequest tmRequest = HhCreateConverter.convertHhNisra(rmRequest);

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), "COMET_CREATE_PRE_SENDING",
        "Case Ref", tmRequest.getReference(),
        "Survey Type", tmRequest.getSurveyType().toString());

    ResponseEntity<Void> response = cometRestClient.sendCreate(tmRequest, rmRequest.getCaseId());
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Create", "FAILED_TO_CREATE_TM_JOB",
        "tmRequest", tmRequest.toString(),
        "rmRequest", rmRequest.toString());
//        "cache", (cache!=null)?cache.toString():"");

    GatewayCacheHh newCache = cacheService.getById(rmRequest.getCaseId());
    if (newCache == null) {
      cacheService.save(GatewayCacheHh.builder().caseId(rmRequest.getCaseId()).existsInFwmt(true)
          .uprn(rmRequest.getUprn()).estabUprn(rmRequest.getEstabUprn()).type(10)
          .lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime).build());
    } else {
      cacheService.save(newCache.toBuilder().existsInFwmt(true)
          .lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime).build());
    }

    eventManager
        .triggerEvent(String.valueOf(rmRequest.getCaseId()), "COMET_CREATE_ACK",
            "Case Ref", rmRequest.getCaseRef(),
            "Response Code", response.getStatusCode().name(),
            "Survey Type", tmRequest.getSurveyType().toString(),
            "HH Create Nisra", tmRequest.toString());
  }
}
