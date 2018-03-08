/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.fulfillment.web;

import static java.util.Comparator.comparing;
import static org.openlmis.fulfillment.service.ResourceNames.BASE_PATH;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.core.json.JsonConverter;
import org.javers.repository.jql.QueryBuilder;
import org.openlmis.fulfillment.service.ObjReferenceExpander;
import org.openlmis.fulfillment.util.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(BASE_PATH)
public abstract class BaseController {

  @Resource(name = "javersProvider")
  private Javers javers;

  @Autowired
  private ObjReferenceExpander objReferenceExpander;

  protected void expandDto(Object dto, Set<String> expands) {
    objReferenceExpander.expandDto(dto, expands);
  }

  Map<String, String> getErrors(BindingResult bindingResult) {
    return bindingResult
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(FieldError::getField, FieldError::getCode));
  }

  ResponseEntity<String> getAuditLogResponse(Class type, UUID id, String author,
                                             String changedPropertyName,
                                             Pageable page) {
    String auditLogs = getAuditLog(type, id, author, changedPropertyName, page);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    return new ResponseEntity<>(auditLogs, headers, HttpStatus.OK);
  }

  /**
   * Return a list of changes via JSON.
   *
   * @param type                The type of class for which we wish to retrieve historical changes.
   * @param id                  The ID of class for which we wish to retrieve historical changes.
   *                            If null, entries are returned regardless of their ID.
   * @param author              The author of the changes which should be returned.
   *                            If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *                            If null or empty, changes associated with any and all properties
   *                            are returned.
   * @param page                A Pageable object with PageNumber and PageSize values used
   *                            for pagination.
   */
  private String getAuditLog(Class type, UUID id, String author, String changedPropertyName,
                             Pageable page) {
    List<Change> changes = getChanges(type, id, author, changedPropertyName, page);
    JsonConverter jsonConverter = javers.getJsonConverter();
    return jsonConverter.toJson(changes);
  }


  /*
    Return JaVers changes for the specified type, optionally filtered by id, author, and property.
  */
  private List<Change> getChanges(Class type, UUID id, String author, String changedPropertyName,
                                  Pageable page) {
    int skip = Pagination.getPageNumber(page);
    int limit = Pagination.getPageSize(page);

    QueryBuilder queryBuilder = QueryBuilder
        .byInstanceId(id, type)
        .withNewObjectChanges(true)
        .skip(skip)
        .limit(limit);

    if (StringUtils.isNotBlank(author)) {
      queryBuilder = queryBuilder.byAuthor(author);
    }

    if (StringUtils.isNotBlank(changedPropertyName)) {
      queryBuilder = queryBuilder.andProperty(changedPropertyName);
    }

    return javers
        .findChanges(queryBuilder.build())
        .stream()
        .sorted(
            comparing((Change change) -> change.getCommitMetadata().get().getCommitDate())
            .reversed())
        .collect(Collectors.toList());
  }

}
