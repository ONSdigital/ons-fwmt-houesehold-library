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

@Qualifier("Update")
@Service
public class HhUpdateHeld implements InboundHhProcessor<FwmtActionInstruction> {

  private static final String HH_UPDATE_HELD = "HH_UPDATE_HELD";

  @Autowired
  private GatewayEventManager eventManager;

  private static final ProcessorKeyHh key = ProcessorKeyHh.builder()
      .actionInstruction(ActionInstructionType.UPDATE.toString())
      .surveyName("CENSUS")
      .addressType("HH")
      .addressLevel("U")
      .build();

  @Override
  public ProcessorKeyHh getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.UPDATE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("HH");
//          && (cache == null
//          || !cache.existsInFwmt);
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, Instant messageReceivedTime) throws GatewayException {
    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), HH_UPDATE_HELD,
        "A HH Update was received before a create. Update held.");
  }
}
