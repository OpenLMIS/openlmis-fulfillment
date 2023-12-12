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

package org.openlmis.fulfillment.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.openlmis.fulfillment.domain.Template;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test-run")
public class JasperReportsViewServiceIntegrationTest {

  private static final String EMPTY_REPORT_RESOURCE = "/empty-report.jrxml";
  private static final int DOUBLE_HIKARI_DEFAULT_POOL_SIZE = 20;
  private static final String PARAM_DATASOURCE = "datasource";
  private static final String FORMAT = "format";
  private static final String PDF = "pdf";

  @InjectMocks
  private JasperReportsViewService service;

  @Spy
  private DataSource dataSource;

  private ByteArrayOutputStream bos = new ByteArrayOutputStream();
  private ObjectOutputStream out;
  private Template template = new Template();
  private Map<String, Object> params = new HashMap<>();

  @Before
  public void setUp() throws IOException {
    out = new ObjectOutputStream(bos);
  }

  @Test
  public void generateReportShouldNotThrowErrorAfterPrintingReport20Times()
      throws JRException, IOException, JasperReportViewException {
    out.writeObject(getEmptyReport());
    out.flush();

    template.setData(bos.toByteArray());
    params.put(FORMAT, PDF);

    for (int i = 0; i <= DOUBLE_HIKARI_DEFAULT_POOL_SIZE; i++) {
      service.generateReport(template, params);
    }
  }

  @Test
  public void generateReportShouldNotThrowErrorForDatasourceParam()
      throws JRException, IOException {
    out.writeObject(getEmptyReport());
    out.flush();

    template.setData(bos.toByteArray());
    params.put(PARAM_DATASOURCE, new ArrayList<>());

    service.generateReport(template, params);
  }

  @Test
  public void shouldGenerateReportForHtml()
      throws JRException, IOException, JasperReportViewException {
    out.writeObject(getEmptyReport());
    out.flush();

    template.setData(bos.toByteArray());
    params.put(FORMAT, "html");

    service.generateReport(template, params);
  }

  @Test
  public void shouldGenerateReportForCsv()
      throws JRException, IOException, JasperReportViewException {
    out.writeObject(getEmptyReport());
    out.flush();

    template.setData(bos.toByteArray());
    params.put(FORMAT, "csv");

    service.generateReport(template, params);
  }

  @Test
  public void shouldGenerateReportForXls()
      throws JRException, IOException, JasperReportViewException {
    out.writeObject(getEmptyReport());
    out.flush();

    template.setData(bos.toByteArray());
    params.put(FORMAT, "xls");

    service.generateReport(template, params);
  }

  @Test
  public void shouldCatchJasperReportViewExceptionWhenDatasourceReturnsNull()
      throws JRException, IOException, SQLException {
    out.writeObject(getEmptyReport());
    out.flush();

    template.setData(bos.toByteArray());
    params.put(FORMAT, PDF);

    when(dataSource.getConnection()).thenThrow(NullPointerException.class);
    try {
      service.generateReport(template, params);
    } catch (JasperReportViewException e) {
      assertTrue(e.getMessage().contains("fulfillment.error.jasper.reportCreationWithMessage"));
    }
  }

  @Test
  public void shouldCatchJasperReportViewExceptionWhenDatasourceReturnsSqlException()
      throws JRException, IOException, SQLException {
    out.writeObject(getEmptyReport());
    out.flush();

    template.setData(bos.toByteArray());
    params.put(FORMAT, PDF);

    when(dataSource.getConnection()).thenThrow(SQLException.class);
    try {
      service.generateReport(template, params);
    } catch (JasperReportViewException e) {
      assertTrue(e.getMessage().contains("fulfillment.error.jasper.reportCreationWithMessage"));
    }
  }

  @Test(expected = JasperReportViewException.class)
  public void shouldThrowJasperReportViewExceptionForUnknownFormat()
      throws JRException, IOException {
    out.writeObject(getEmptyReport());
    out.flush();

    template.setData(bos.toByteArray());
    params.put(FORMAT, "odt");

    service.generateReport(template, params);
  }

  @Test(expected = JasperReportViewException.class)
  public void shouldThrowJasperReportViewExceptionIfReportIsNotSavedAsObjectOutputStream() {
    template.setData(bos.toByteArray());
    params.put(FORMAT, PDF);

    service.generateReport(template, params);
  }

  private JasperReport getEmptyReport() throws JRException {
    return JasperCompileManager
        .compileReport(getClass().getResourceAsStream(EMPTY_REPORT_RESOURCE));
  }
}
