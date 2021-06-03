package uk.gov.ons.fwmt.household.library.service.converter;

import uk.gov.ons.census.fwmt.common.data.tm.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;

public final class HhPauseConverter {

  private HhPauseConverter() {
  }

  public static CasePauseRequest buildPause(FwmtActionInstruction ffu) {
    return CasePauseRequest.builder()
        .code(ffu.getPauseCode())
        .effectiveFrom(ffu.getPauseFrom().toString())
        .reason(ffu.getPauseReason())
        .build();
  }
}
