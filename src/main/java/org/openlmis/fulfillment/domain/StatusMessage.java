package org.openlmis.fulfillment.domain;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "status_messages")
@NoArgsConstructor
public class StatusMessage extends BaseEntity {

  @ManyToOne(cascade = CascadeType.REFRESH)
  @JoinColumn(name = "orderId", nullable = false)
  @Getter
  @Setter
  private Order order;

  @Getter
  @Setter
  @Type(type = UUID_TYPE)
  private UUID authorId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Getter
  @Setter
  private RequisitionStatus status;

  @Column(nullable = false)
  @Getter
  @Setter
  private String body;

  /**
   * Create new instance of StatusMessage based on given {@link StatusMessage.Importer}
   * @param importer instance of {@link StatusMessage.Importer}
   * @return instance of StatusMessage.
   */
  public static StatusMessage newInstance(Importer importer) {
    StatusMessage statusMessage = new StatusMessage();
    statusMessage.setId(importer.getId());
    statusMessage.setAuthorId(importer.getAuthorId());
    statusMessage.setStatus(importer.getStatus());
    statusMessage.setBody(importer.getBody());
    return statusMessage;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setAuthorId(authorId);
    exporter.setBody(body);
    exporter.setStatus(status);
  }

  public interface Exporter {
    void setId(UUID id);

    void setAuthorId(UUID authorId);

    void setBody(String body);

    void setStatus(RequisitionStatus requisitionStatus);
  }

  public interface Importer {
    UUID getId();

    UUID getAuthorId();

    String getBody();

    RequisitionStatus getStatus();
  }

}
