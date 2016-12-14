package org.openlmis.fulfillment.domain;


import java.util.ArrayList;

public class OrderFileTemplateBuilder {

  /**
   * Creates new instance od OrderFiletemplate based on given{@link OrderFileTemplate.Importer}
   * @param importer instance of {@link OrderFileTemplate.Importer}
   * @return new instance of OrderFileTemplate.
   */
  public static OrderFileTemplate newOrderFileTemplate(OrderFileTemplate.Importer importer) {
    OrderFileTemplate orderFileTemplate =  new OrderFileTemplate();
    orderFileTemplate.setId(importer.getId());
    orderFileTemplate.setFilePrefix(importer.getFilePrefix());
    orderFileTemplate.setHeaderInFile(importer.getHeaderInFile());
    orderFileTemplate.setOrderFileColumns(new ArrayList<>());
    if (importer.getOrderFileColumns() != null) {
      for (OrderFileColumn.Importer orderFileColumn : importer.getOrderFileColumns()) {
        OrderFileColumn item = OrderFileColumn.newOrderFileColumn(orderFileColumn);
        orderFileTemplate.getOrderFileColumns().add(item);
      }
    }

    return orderFileTemplate;
  }
}
