package uk.gov.ons.fwmt.household.library.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * This class is a caching service that reduces the frequency of date checks
 */

@Slf4j
@Service
public class CeFollowUpSchedulingService {

  @Value("${ce.followUpDate}")
  Date followUpDate;

  @Value("${ce.startDate}")
  Date startDate;


  public boolean isInFollowUp() {
    Date todaysDate = new Date();

    return (todaysDate.after(startDate) && todaysDate.after(followUpDate));

  }

}
