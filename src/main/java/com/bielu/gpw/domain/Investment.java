package com.bielu.gpw.domain;

import java.math.BigDecimal;
import java.util.Date;

public interface Investment {

  BigDecimal value();
  
  Date startDate();
}
