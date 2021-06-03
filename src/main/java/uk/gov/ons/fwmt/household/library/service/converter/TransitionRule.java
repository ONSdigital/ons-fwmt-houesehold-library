package uk.gov.ons.fwmt.household.library.service.converter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.fwmt.household.library.enums.TransitionAction;
import uk.gov.ons.fwmt.household.library.enums.TransitionRequestAction;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransitionRule {
  private TransitionAction action;
  @Builder.Default
  private String cacheType = null;
  @Builder.Default
  private TransitionRequestAction requestAction = TransitionRequestAction.NONE;
}
