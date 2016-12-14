package org.openlmis.fulfillment.service.referencedata;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.net.URI;

public class RightReferenceDataServiceTest extends BaseReferenceDataServiceTest<RightDto> {

  @Override
  protected BaseReferenceDataService<RightDto> getService() {
    return new RightReferenceDataService();
  }

  @Override
  RightDto generateInstance() {
    return new RightDto();
  }

  @Test
  public void shouldFindRightByName() throws Exception {
    // given
    String name = "testRight";

    RightDto rightDto = generateInstance();
    rightDto.setName(name);

    RightDto[] rights = new RightDto[]{rightDto};
    RightReferenceDataService service = (RightReferenceDataService) prepareService();
    ResponseEntity<RightDto[]> response = mock(ResponseEntity.class);

    // when
    when(restTemplate.getForEntity(any(URI.class), eq(service.getArrayResultClass())))
        .thenReturn(response);
    when(response.getBody()).thenReturn(rights);

    RightDto right = service.findRight(name);

    // then
    verify(restTemplate).getForEntity(
        uriCaptor.capture(), eq(service.getArrayResultClass())
    );

    URI uri = uriCaptor.getValue();
    String url = service.getReferenceDataUrl() + service.getUrl()
        + "search?" + ACCESS_TOKEN + "&name=" + name;

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(right.getName(), is(equalTo(name)));
  }

  @Test
  public void shouldReturnNullIfRightCannotBeFound() throws Exception {
    // given
    String name = "testRight";

    RightDto[] rights = new RightDto[0];
    RightReferenceDataService service = (RightReferenceDataService) prepareService();
    ResponseEntity<RightDto[]> response = mock(ResponseEntity.class);

    // when
    when(restTemplate.getForEntity(any(URI.class), eq(service.getArrayResultClass())))
        .thenReturn(response);
    when(response.getBody()).thenReturn(rights);

    RightDto right = service.findRight(name);

    // then
    verify(restTemplate).getForEntity(
        uriCaptor.capture(), eq(service.getArrayResultClass())
    );

    URI uri = uriCaptor.getValue();
    String url = service.getReferenceDataUrl() + service.getUrl()
        + "search?" + ACCESS_TOKEN + "&name=" + name;

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(right, is(nullValue()));
  }

}
