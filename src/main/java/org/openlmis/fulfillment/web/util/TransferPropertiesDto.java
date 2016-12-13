package org.openlmis.fulfillment.web.util;

import static org.apache.commons.lang.StringUtils.lowerCase;
import static org.apache.commons.lang.StringUtils.splitByCharacterTypeCamelCase;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import org.openlmis.fulfillment.domain.TransferProperties;

import lombok.Getter;
import lombok.Setter;

import java.util.Locale;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeIdResolver(TransferPropertiesDto.TypeIdResolver.class)
public abstract class TransferPropertiesDto
    implements TransferProperties.Importer, TransferProperties.Exporter {

  @Getter
  @Setter
  private UUID id;

  @Getter
  @Setter
  private UUID facilityId;

  public static final class TypeIdResolver extends TypeIdResolverBase {

    @Override
    public String idFromValue(Object value) {
      return idFromClass(value.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
      return value == null ? idFromClass(suggestedType) : idFromValue(value);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
      return JsonTypeInfo.Id.CUSTOM;
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
      switch (id) {
        case "ftp":
          return context.constructType(FtpTransferPropertiesDto.class);
        case "local":
          return context.constructType(LocalTransferPropertiesDto.class);
        default:
          throw new IllegalArgumentException("The id is not supported: " + id);
      }
    }

    private String idFromClass(Class<?> type) {
      String[] strings = splitByCharacterTypeCamelCase(type.getSimpleName());

      if (strings.length > 0) {
        return lowerCase(strings[0], Locale.ENGLISH);
      }

      throw new IllegalStateException("The type: " + type + " should have camel case name");
    }

  }

}
