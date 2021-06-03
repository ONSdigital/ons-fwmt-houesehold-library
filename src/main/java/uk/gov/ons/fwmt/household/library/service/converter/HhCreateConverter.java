package uk.gov.ons.fwmt.household.library.service.converter;

import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;

import java.util.List;
import java.util.Objects;

public final class HhCreateConverter {

  private HhCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertHH(
      FwmtActionInstruction ffu, CaseRequest.CaseRequestBuilder builder) {
    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(ffu, builder);

    commonBuilder.type(CaseType.HH);
    commonBuilder.surveyType(SurveyType.HH);
    commonBuilder.category("HH");

    Geography outGeography = Geography.builder().oa(ffu.getOa()).build();

    Address outAddress = Address.builder()
        .lines(List.of(
            ffu.getAddressLine1(),
            Objects.toString(ffu.getAddressLine2(), ""),
            Objects.toString(ffu.getAddressLine3(), "")
        ))
        .town(ffu.getTownName())
        .postcode(ffu.getPostcode())
        .geography(outGeography)
        .build();
    commonBuilder.address(outAddress);

    return commonBuilder;
  }

  public static CaseRequest convertHhEnglandAndWales(FwmtActionInstruction ffu) {
    return HhCreateConverter
        .convertHH(ffu, CaseRequest.builder())
        .sai("Sheltered Accommodation".equals(ffu.getEstabType()))
        .blankFormReturned(ffu.isBlankFormReturned())
        .uaa(ffu.isUndeliveredAsAddress())
        .build();
  }

  public static CaseRequest convertHhNisra(FwmtActionInstruction ffu) {
    return HhCreateConverter
        .convertHH(ffu, CaseRequest.builder())
        .requiredOfficer(ffu.getFieldOfficerId())
        .sai("Sheltered Accommodation".equals(ffu.getEstabType()))
        .blankFormReturned(ffu.isBlankFormReturned())
        .uaa(ffu.isUndeliveredAsAddress())
        .build();
  }
}
