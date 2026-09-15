/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.fulfillment.web.errorhandler;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_JASPER_REPORT_CREATION_WITH_MESSAGE;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.fulfillment.i18n.MessageService;
import org.openlmis.fulfillment.service.JasperReportViewException;
import org.openlmis.fulfillment.util.Message;

@RunWith(MockitoJUnitRunner.class)
public class ServiceErrorHandlingTest {

  private static final String UPSTREAM_STATUS = "502 BAD_GATEWAY";
  private static final String CAUSE = "report service unavailable";
  private static final String LOCALIZED =
      "Could not create a Jasper Report (original message: 502 BAD_GATEWAY)";

  @Mock
  private MessageService messageService;

  @InjectMocks
  private ServiceErrorHandling errorHandling;

  private Message.LocalizedMessage localizedMessage;

  @Before
  public void setUp() {
    localizedMessage = new Message(ERROR_JASPER_REPORT_CREATION_WITH_MESSAGE)
        .new LocalizedMessage(LOCALIZED);
    when(messageService.localize(any(Message.class))).thenReturn(localizedMessage);
  }

  @Test
  public void shouldLocalizeJasperReportViewException() {
    Message.LocalizedMessage result =
        errorHandling.handleJasperReportViewException(jasperReportViewException());

    assertEquals(localizedMessage, result);
  }

  @Test
  public void shouldKeepTheUpstreamStatusInTheLocalizedMessage() {
    errorHandling.handleJasperReportViewException(jasperReportViewException());

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(messageService).localize(captor.capture());

    assertEquals(ERROR_JASPER_REPORT_CREATION_WITH_MESSAGE, captor.getValue().getKey());
    assertThat(captor.getValue().toString(), containsString(UPSTREAM_STATUS));
  }

  private JasperReportViewException jasperReportViewException() {
    return new JasperReportViewException(new IllegalStateException(CAUSE),
        ERROR_JASPER_REPORT_CREATION_WITH_MESSAGE, UPSTREAM_STATUS);
  }
}
