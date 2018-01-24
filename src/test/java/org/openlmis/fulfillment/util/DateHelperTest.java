package org.openlmis.fulfillment.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateHelperTest {

  private static final ZoneId ZONE_ID = ZoneId.of("UTC");

  @Mock
  Clock clock;

  @InjectMocks
  DateHelper dateHelper;

  @Before
  public void setUp() {
    Mockito.when(clock.getZone()).thenReturn(ZONE_ID);
    Mockito.when(clock.instant()).thenReturn(Instant.now());
  }

  @Test
  public void shouldGetCurrentDate() {
    assertEquals(LocalDate.now(clock), dateHelper.getCurrentDate());
  }

  @Test
  public void shouldGetCurrentDateWithSystemTimeZone() {
    assertEquals(ZonedDateTime.now(clock), dateHelper.getCurrentDateTimeWithSystemZone());
  }
}
