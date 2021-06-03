package uk.gov.ons.fwmt.household.library.processors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.processor.InboundHhProcessor;
import uk.gov.ons.census.fwmt.common.processor.ProcessorKeyHh;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

import java.time.Instant;

@Qualifier("Create")
@Service
public class NisraCaseExists implements InboundHhProcessor<FwmtActionInstruction> {

  final private static String NISRA_CASE_EXISTS = "NISRA_CASE_EXISTS";

  private static final ProcessorKeyHh key = ProcessorKeyHh.builder()
      .actionInstruction(ActionInstructionType.CREATE.toString())
      .surveyName("CENSUS")
      .addressType("HH")
      .addressLevel("U")
      .build();

  @Autowired
  private GatewayEventManager eventManager;

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
//          && (cache != null && cache.existsInFwmt);
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, Instant messageReceivedTime) throws GatewayException {
    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), NISRA_CASE_EXISTS,
        "UPRN", rmRequest.getUprn(),
        "Estab UPRN", rmRequest.getEstabUprn());
  }
}
