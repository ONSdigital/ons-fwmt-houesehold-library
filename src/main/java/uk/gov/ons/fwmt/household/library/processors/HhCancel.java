package uk.gov.ons.fwmt.household.library.processors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.census.fwmt.common.data.cache.GatewayCacheHh;
import uk.gov.ons.census.fwmt.common.data.tm.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.processor.InboundHhProcessor;
import uk.gov.ons.census.fwmt.common.processor.ProcessorKeyHh;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.fwmt.routing.http.comet.CometRestClient;
import uk.gov.ons.fwmt.routing.service.GatewayCacheService;
import uk.gov.ons.fwmt.routing.service.RoutingValidator;
import uk.gov.ons.fwmt.routing.service.converter.HhCancelConverter;

import java.time.Instant;

@Qualifier("Cancel")
@Service
public class HhCancel implements InboundHhProcessor<FwmtCancelActionInstruction> {

  private static final String PROCESSING = "PROCESSING";

  public static final String COMET_CANCEL_PRE_SENDING = "COMET_CANCEL_PRE_SENDING";

  public static final String COMET_CANCEL_ACK = "COMET_CANCEL_ACK";

  private static final ProcessorKeyHh key = ProcessorKeyHh.builder()
      .actionInstruction(ActionInstructionType.CANCEL.toString())
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

  @Override public boolean isValid(FwmtCancelActionInstruction rmRequest) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.CANCEL
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("HH")
          && rmRequest.getAddressLevel().equals("U")
          && !rmRequest.isNc();
//          && (cache != null && cache.existsInFwmt);
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override public void process(FwmtCancelActionInstruction rmRequest, Instant messageReceivedTime)
      throws GatewayException {

      boolean alreadyCancelled = false;
      ResponseEntity<Void> response = null;

      eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), PROCESSING,
          "type", "HH Cancel Case",
          "action", "Cancel");

      CasePauseRequest tmRequest = HhCancelConverter.buildCancel(rmRequest);

      eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CANCEL_PRE_SENDING,
          "Case Ref", "NA",
          "TM Action", "CLOSE");
      try {
        response = cometRestClient.sendPause(tmRequest, rmRequest.getCaseId());
        routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Pause", "FAILED_TO_CREATE_TM_JOB",
            "tmRequest", tmRequest.toString(),
            "rmRequest", rmRequest.toString());
//            "cache", (cache != null) ? cache.toString() : "");
      } catch (RestClientException e) {
        String tmResponse = e.getMessage();
        if (tmResponse != null && tmResponse.contains("404") && tmResponse.contains("Unable to find Case")){
          eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), "CASE_DOES_NOT_EXIST",
              "A HH cancel case has been received for a case that does not exist",
              "Message received: " + rmRequest.toString());
          alreadyCancelled = true;
        } else {
          throw e;
        }
      }

      if(response != null && !alreadyCancelled) {
        GatewayCacheHh newCache = cacheService.getById(rmRequest.getCaseId());
        if (newCache != null) {
          cacheService.save(newCache.toBuilder().lastActionInstruction(rmRequest.getActionInstruction().toString())
              .lastActionTime(messageReceivedTime)
              .build());
        }

        eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CANCEL_ACK,
            "Case Ref", "NA",
            "HH Cancel/Pause", tmRequest.toString(),
            "Response Code", response.getStatusCode().name());
      }
    }
  }
