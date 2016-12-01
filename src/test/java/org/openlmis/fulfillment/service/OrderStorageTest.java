package org.openlmis.fulfillment.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.service.OrderFileStorage.LOCAL_DIR;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderFileTemplate;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OrderFileStorage.class})
public class OrderStorageTest {
  private static final String FILE_PREFIX = "prefix-";
  private static final String ORDER_CODE = "order-code-123";
  private static final String FILE_NAME = FILE_PREFIX + ORDER_CODE + ".csv";
  private static final String FULL_PATH = LOCAL_DIR + '/' + FILE_NAME;

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Mock
  private OrderCsvHelper csvHelper;

  @Mock
  private OrderFileTemplateService orderFileTemplateService;

  @InjectMocks
  private OrderFileStorage orderFileStorage;

  @Mock
  private Order order;

  @Mock
  private OrderFileTemplate template;

  @Mock
  private BufferedWriter writer;

  private IOException exception = new IOException("test purpose");

  @Before
  public void setUp() throws Exception {
    PowerMockito.mockStatic(Files.class);

    when(Files.newBufferedWriter(any(Path.class))).thenReturn(writer);
    when(orderFileTemplateService.getOrderFileTemplate()).thenReturn(template);

    when(order.getOrderCode()).thenReturn(ORDER_CODE);
    when(template.getFilePrefix()).thenReturn(FILE_PREFIX);
  }

  @Test
  public void shouldStoreAnOrder() throws Exception {
    orderFileStorage.store(order);

    verify(orderFileTemplateService).getOrderFileTemplate();
    verify(csvHelper).writeCsvFile(order, template, writer);

    ArgumentCaptor<Path> captor = ArgumentCaptor.forClass(Path.class);

    verifyStatic();
    Files.newBufferedWriter(captor.capture());

    Path value = captor.getValue();
    assertThat(value.toString(), is(FULL_PATH));
  }

  @Test
  public void shouldThrowExceptionIfThereIsProblemWithStoringAnOrder() throws Exception {
    doThrow(exception).when(csvHelper).writeCsvFile(order, template, writer);

    expected.expect(OrderStorageException.class);
    expected.expectMessage("I/O while creating the order CSV file");
    expected.expectCause(is(exception));

    orderFileStorage.store(order);
  }

  @Test
  public void shouldDeleteAnOrder() throws Exception {
    orderFileStorage.delete(order);

    ArgumentCaptor<Path> captor = ArgumentCaptor.forClass(Path.class);

    verifyStatic();
    Files.deleteIfExists(captor.capture());

    Path value = captor.getValue();
    assertThat(value.toString(), is(FULL_PATH));
  }

  @Test(expected = OrderFileException.class)
  public void shouldThrowExceptionIfThereIsProblemWithDeletingAnOrder() throws Exception {
    when(Files.deleteIfExists(any(Path.class))).thenThrow(exception);

    expected.expect(OrderStorageException.class);
    expected.expectMessage("I/O while deleting the order CSV file");
    expected.expectCause(is(exception));

    orderFileStorage.delete(order);
  }

  @Test
  public void shouldReturnOrderAsPath() throws Exception {
    Path path = orderFileStorage.getOrderAsPath(order);
    assertThat(path.toString(), is(FULL_PATH));
  }

}
