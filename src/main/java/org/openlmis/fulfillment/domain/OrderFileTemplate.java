package org.openlmis.fulfillment.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@Table(name = "order_file_templates")
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class OrderFileTemplate extends BaseEntity {

  @Column(nullable = false)
  @Getter
  @Setter
  private String filePrefix;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean headerInFile;

  @OneToMany(
      mappedBy = "orderFileTemplate",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE},
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  @OrderBy("position ASC")
  @Getter
  @Setter
  private List<OrderFileColumn> orderFileColumns;

  /**
   * Creates new instance od OrderFileTemplate based on given{@link OrderFileTemplate.Importer}
   * @param importer instance of {@link OrderFileTemplate.Importer}
   * @return new instance of OrderFileTemplate.
   */
  public static OrderFileTemplate newInstance(OrderFileTemplate.Importer importer) {
    OrderFileTemplate orderFileTemplate =  new OrderFileTemplate();
    orderFileTemplate.setId(importer.getId());
    orderFileTemplate.setFilePrefix(importer.getFilePrefix());
    orderFileTemplate.setHeaderInFile(importer.getHeaderInFile());
    orderFileTemplate.setOrderFileColumns(new ArrayList<>());
    if (importer.getOrderFileColumns() != null) {
      importer.getOrderFileColumns().forEach(
          ofc -> orderFileTemplate.getOrderFileColumns().add(
              OrderFileColumn.newOrderFileColumn(ofc)));
    }

    return orderFileTemplate;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(OrderFileTemplate.Exporter exporter) {
    exporter.setId(id);
    exporter.setFilePrefix(filePrefix);
    exporter.setHeaderInFile(headerInFile);
  }

  public interface Exporter {
    void setId(UUID id);

    void setFilePrefix(String filePrefix);

    void setHeaderInFile(Boolean headerInFile);

  }

  public interface Importer {
    UUID getId();

    String getFilePrefix();

    Boolean getHeaderInFile();

    List<OrderFileColumn.Importer> getOrderFileColumns();

  }
}
