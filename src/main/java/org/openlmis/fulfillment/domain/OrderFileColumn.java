package org.openlmis.fulfillment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "order_file_columns")
@NoArgsConstructor
@AllArgsConstructor
public class OrderFileColumn extends BaseEntity {

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean openLmisField;

  @Getter
  @Setter
  private String dataFieldLabel;

  @Getter
  @Setter
  private String columnLabel;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean include;

  @Column(nullable = false)
  @Getter
  @Setter
  private Integer position;

  @Getter
  @Setter
  private String format;

  @Getter
  @Setter
  private String nested;

  @Getter
  @Setter
  private String keyPath;

  @Getter
  @Setter
  private String related;

  @Getter
  @Setter
  private String relatedKeyPath;

  @ManyToOne(cascade = CascadeType.REFRESH)
  @JoinColumn(name = "orderFileTemplateId", nullable = false)
  @Getter
  @Setter
  private OrderFileTemplate orderFileTemplate;

  /**
   * Creates new OrderFileColumn object based on data from {@link OrderFileColumn.Importer}
   *
   * @param importer instance of {@link OrderFileColumn.Importer}
   * @return new instance of OrderFileColumn.
   */
  public static OrderFileColumn newOrderFileColumn(OrderFileColumn.Importer importer) {
    OrderFileColumn orderFileColumn = new OrderFileColumn();
    orderFileColumn.setId(importer.getId());
    orderFileColumn.setOpenLmisField(importer.getOpenLmisField());
    orderFileColumn.setDataFieldLabel(importer.getDataFieldLabel());
    orderFileColumn.setColumnLabel(importer.getColumnLabel());
    orderFileColumn.setInclude(importer.getInclude());
    orderFileColumn.setPosition(importer.getPosition());
    orderFileColumn.setNested(importer.getNested());
    orderFileColumn.setKeyPath(importer.getKeyPath());
    orderFileColumn.setRelated(importer.getRelated());
    orderFileColumn.setRelatedKeyPath(importer.getRelatedKeyPath());
    return orderFileColumn;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(OrderFileColumn.Exporter exporter) {
    exporter.setId(id);
    exporter.setColumnLabel(columnLabel);
    exporter.setOpenLmisField(openLmisField);
    exporter.setDataFieldLabel(dataFieldLabel);
    exporter.setInclude(include);
    exporter.setRelated(related);
    exporter.setPosition(position);
    exporter.setNested(nested);
    exporter.setRelatedKeyPath(relatedKeyPath);
    exporter.setKeyPath(keyPath);

  }

  public interface Exporter {
    void setId(UUID id);

    void setOpenLmisField(Boolean openLmisField);

    void setDataFieldLabel(String dataFieldLabel);

    void setColumnLabel(String columnLabel);

    void setInclude(Boolean include);

    void setPosition(Integer position);

    void setFormat(String format);

    void setNested(String nested);

    void setKeyPath(String keyPath);

    void setRelated(String related);

    void setRelatedKeyPath(String relatedKeyPath);

  }

  public interface Importer {
    UUID getId();

    Boolean getOpenLmisField();

    String getDataFieldLabel();

    String getColumnLabel();

    Boolean getInclude();

    Integer getPosition();

    String getFormat();

    String getNested();

    String getKeyPath();

    String getRelated();

    String getRelatedKeyPath();

  }

}
