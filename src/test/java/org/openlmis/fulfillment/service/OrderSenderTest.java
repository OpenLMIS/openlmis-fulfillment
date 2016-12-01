package org.openlmis.fulfillment.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.domain.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.nio.file.Path;

@RunWith(MockitoJUnitRunner.class)
public class OrderSenderTest {

  @Mock(name = "toFtpChannel")
  private MessageChannel toFtpChannel;

  @Mock
  private OrderStorage orderStorage;

  @InjectMocks
  private OrderFtpSender orderFtpSender;

  @Mock
  private Order order;

  @Mock
  private Path path;

  @Before
  public void setUp() throws Exception {
    when(orderStorage.getOrderAsPath(order)).thenReturn(path);
  }

  @Test
  public void shouldReturnTrueIfMessageHasBeenSentSuccessfully() throws Exception {
    assertThat(orderFtpSender.send(order), is(true));
  }

  @Test
  public void shouldReturnFalseIfMessageHasNotBeenSentSuccessfully() throws Exception {
    when(toFtpChannel.send(any(Message.class))).thenThrow(new RuntimeException("test purpose"));
    assertThat(orderFtpSender.send(order), is(false));
  }
}
