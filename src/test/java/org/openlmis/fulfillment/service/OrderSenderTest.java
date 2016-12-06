package org.openlmis.fulfillment.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.domain.FacilityFtpSetting;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.repository.FacilityFtpSettingRepository;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class OrderSenderTest {

  @Mock
  private ProducerTemplate producerTemplate;

  @Mock
  private OrderStorage orderStorage;

  @Mock
  private FacilityFtpSettingRepository facilityFtpSettingRepository;

  @InjectMocks
  private OrderFtpSender orderFtpSender;

  @Mock
  private Order order;

  @Mock
  private Path path;

  @Mock
  private File file;

  @Before
  public void setUp() throws Exception {
    FacilityFtpSetting setting = new FacilityFtpSetting();
    setting.setId(UUID.randomUUID());
    setting.setFacilityId(UUID.randomUUID());
    setting.setProtocol("ftp");
    setting.setServerHost("host");
    setting.setServerPort(21);
    setting.setRemoteDirectory("remote/dir");
    setting.setLocalDirectory("local/dir");
    setting.setUsername("username");
    setting.setPassword("password");
    setting.setPassiveMode(true);

    when(orderStorage.getOrderAsPath(order)).thenReturn(path);
    when(facilityFtpSettingRepository.findFirstByFacilityId(any())).thenReturn(setting);

    when(path.toFile()).thenReturn(file);
  }

  @Test
  public void shouldReturnTrueIfMessageHasBeenSentSuccessfully() throws Exception {
    assertThat(orderFtpSender.send(order), is(true));
  }

  @Test
  public void shouldReturnFalseIfMessageHasNotBeenSentSuccessfully() throws Exception {
    doThrow(new RuntimeException("test purpose"))
    .when(producerTemplate).sendBodyAndHeader(anyString(), any(), eq(Exchange.FILE_NAME), any());

    assertThat(orderFtpSender.send(order), is(false));
  }
}
