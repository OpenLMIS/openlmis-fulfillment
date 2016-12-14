package org.openlmis.fulfillment.service.referencedata;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RunWith(Parameterized.class)
public class SupplyLineReferenceDataServiceParameterizedTest
    extends BaseReferenceDataServiceTest<SupplyLineDto> {

  private static final String URI_QUERY_NAME = "name";
  private static final String URI_QUERY_VALUE = "value";

  @Override
  protected BaseReferenceDataService<SupplyLineDto> getService() {
    return new SupplyLineReferenceDataService();
  }

  @Override
  SupplyLineDto generateInstance() {
    return new SupplyLineDto();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    super.setUp();
  }

  private UUID program = UUID.randomUUID();
  private UUID supervisoryNode;
  private UUID supplyingFacility;

  /**
   * Creates new instance of Parameterized Test.
   *
   * @param supervisoryNode   UUID of supervisory node
   * @param supplyingFacility UUID of supplying facility
   */
  public SupplyLineReferenceDataServiceParameterizedTest(UUID supervisoryNode,
                                                         UUID supplyingFacility) {
    this.supervisoryNode = supervisoryNode;
    this.supplyingFacility = supplyingFacility;
  }

  /**
   * Get test data.
   *
   * @return collection of objects that will be passed to test constructor.
   */
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {null, null},
        {null, UUID.randomUUID()},
        {UUID.randomUUID(), null},
        {UUID.randomUUID(), UUID.randomUUID()}
    });
  }

  @Test
  public void shouldFindSupplyLines() {
    // given
    SupplyLineReferenceDataService service = (SupplyLineReferenceDataService) prepareService();
    ResponseEntity<SupplyLineDto[]> response = mock(ResponseEntity.class);

    // when
    when(restTemplate.getForEntity(any(URI.class), eq(service.getArrayResultClass())))
        .thenReturn(response);
    when(response.getBody()).thenReturn(new SupplyLineDto[]{generateInstance()});

    Collection<SupplyLineDto> collection = service
        .search(program, supervisoryNode, supplyingFacility);

    // then
    assertThat(collection, hasSize(1));

    verify(restTemplate, atLeastOnce()).getForEntity(
        uriCaptor.capture(), eq(service.getArrayResultClass())
    );

    URI uri = uriCaptor.getValue();
    List<NameValuePair> parse = URLEncodedUtils.parse(uri, "UTF-8");

    assertThat(parse, hasItem(allOf(
        hasProperty(URI_QUERY_NAME, is("programId")),
        hasProperty(URI_QUERY_VALUE, is(program.toString())))
    ));

    if (null != supervisoryNode) {
      assertThat(parse, hasItem(allOf(
          hasProperty(URI_QUERY_NAME, is("supervisoryNodeId")),
          hasProperty(URI_QUERY_VALUE, is(supervisoryNode.toString())))
      ));
    } else {
      assertThat(parse, not(hasItem(hasProperty(URI_QUERY_NAME, is("supervisoryNodeId")))));
    }

    if (null != supplyingFacility) {
      assertThat(parse, hasItem(allOf(
          hasProperty(URI_QUERY_NAME, is("supplyingFacilityId")),
          hasProperty(URI_QUERY_VALUE, is(supplyingFacility.toString())))
      ));
    } else {
      assertThat(parse, not(hasItem(hasProperty(URI_QUERY_NAME, is("supplyingFacilityId")))));
    }
  }

}
