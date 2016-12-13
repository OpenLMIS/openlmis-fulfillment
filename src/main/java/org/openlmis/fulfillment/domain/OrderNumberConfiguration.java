package org.openlmis.fulfillment.domain;

import org.openlmis.fulfillment.service.referencedata.ProgramDto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_number_configurations")
@EqualsAndHashCode(callSuper = false)
public class OrderNumberConfiguration extends BaseEntity {

  @Getter
  @Setter
  @Column
  private String orderNumberPrefix;

  @Getter
  @Setter
  @Column(nullable = false)
  private Boolean includeOrderNumberPrefix;

  @Getter
  @Setter
  @Column(nullable = false)
  private Boolean includeProgramCode;

  @Getter
  @Setter
  @Column(nullable = false)
  private Boolean includeTypeSuffix;

  /**
   * Generates order number for given parameters.
   *
   * @param order   order instance.
   * @param program Program associated with the order.
   * @return Generated orderNumber.
   * @throws OrderNumberException if the order parameter is {@code null}
   */
  public String generateOrderNumber(Order order, ProgramDto program) {
    if (order == null) {
      throw new OrderNumberException("Order cannot be empty");
    }

    StringBuilder orderNumber = new StringBuilder();

    if (includeOrderNumberPrefix && orderNumberPrefix != null) {
      orderNumber.append(getOrderNumberPrefix());
    }

    if (includeProgramCode && program != null) {
      orderNumber.append(getTruncatedProgramCode(program.getCode()));
    }

    orderNumber.append(order.getExternalId().toString());

    if (includeTypeSuffix) {
      orderNumber.append(order.getEmergency() ? "E" : "R");
    }

    return orderNumber.toString();
  }

  private String getTruncatedProgramCode(String code) {
    return code.length() > 35 ? code.substring(0, 35) : code;
  }
}
