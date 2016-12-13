package org.openlmis.fulfillment.web.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TypeIdResolverTest {
  private static final String FTP = "ftp";
  private static final String LOCAL = "local";

  @Mock
  private DatabindContext context;

  private TypeIdResolver resolver = new TransferPropertiesDto.TypeIdResolver();

  @Test
  public void shouldReturnFirstWordFromClassName() throws Exception {
    assertThat(resolver.idFromValueAndType(null, FtpTransferPropertiesDto.class), is(FTP));
    assertThat(resolver.idFromValueAndType(null, LocalTransferPropertiesDto.class), is(LOCAL));
  }

  @Test
  public void shouldReturnFirstWordFromObjectName() throws Exception {
    assertThat(resolver.idFromValue(new FtpTransferPropertiesDto()), is(FTP));
    assertThat(resolver.idFromValue(new LocalTransferPropertiesDto()), is(LOCAL));

    assertThat(resolver.idFromValueAndType(new FtpTransferPropertiesDto(), null), is(FTP));
    assertThat(resolver.idFromValueAndType(new LocalTransferPropertiesDto(), null), is(LOCAL));
  }

  @Test
  public void shouldHaveCustomMechanism() throws Exception {
    assertThat(resolver.getMechanism(), is(JsonTypeInfo.Id.CUSTOM));
  }

  @Test
  public void shouldCreateDtoTypeFromId() throws Exception {
    resolver.typeFromId(context, FTP);
    verify(context).constructType(FtpTransferPropertiesDto.class);

    resolver.typeFromId(context, LOCAL);
    verify(context).constructType(FtpTransferPropertiesDto.class);
  }
}
