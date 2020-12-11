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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.service.JasperReportsViewService.PARAM_DATASOURCE;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.fulfillment.domain.Template;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@SuppressWarnings({"PMD.UnusedPrivateField"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(BlockJUnit4ClassRunner.class)
@PrepareForTest({JasperReportsViewService.class, JasperFillManager.class,
    JasperExportManager.class})
public class JasperReportsViewServiceTest {

  private static final String FORMAT = "format";
  private static final String PDF = "pdf";

  @Mock
  DataSource dataSource;

  @Mock
  private JasperCsvExporter jasperCsvExporter;

  @Mock
  private JasperXlsExporter jasperXlsExporter;

  @Mock
  private JasperHtmlExporter jasperHtmlExporter;

  @Mock
  private JasperPdfExporter jasperPdfExporter;

  @InjectMocks
  private JasperReportsViewService jasperReportsViewService;

  private Template template;
  private JasperReport jasperReport;
  private ObjectInputStream objectInputStream;
  private ObjectOutputStream objectOutputStream;
  private ByteArrayOutputStream byteArrayOutputStream;
  private byte[] reportTemplateData;
  private byte[] expectedReportData;

  @Before
  public void setUp() throws Exception {
    initializeExporterMocks();
    template = mock(Template.class);
    when(template.getName()).thenReturn("report1.jrxml");
    reportTemplateData = new byte[1];
    when(template.getData()).thenReturn(reportTemplateData);
    jasperReport = mock(JasperReport.class);
    expectedReportData = new byte[1];

    objectInputStream = mock(ObjectInputStream.class);
    objectOutputStream = mock(ObjectOutputStream.class);
    byteArrayOutputStream = mock(ByteArrayOutputStream.class);

    ByteArrayInputStream byteArrayInputStream = mock(ByteArrayInputStream.class);
    whenNew(ByteArrayInputStream.class).withArguments(reportTemplateData)
        .thenReturn(byteArrayInputStream);
    whenNew(ObjectInputStream.class).withArguments(byteArrayInputStream)
        .thenReturn(objectInputStream);
    whenNew(ByteArrayOutputStream.class).withNoArguments().thenReturn(byteArrayOutputStream);
    whenNew(ObjectOutputStream.class).withArguments(byteArrayOutputStream)
        .thenReturn(objectOutputStream);
    
    mockStatic(JasperFillManager.class);
    mockStatic(JasperExportManager.class);
  }

  @Test
  public void generateReportShouldReturnReportForDataSource() throws Exception {
    when(objectInputStream.readObject()).thenReturn(jasperReport);
    when(dataSource.getConnection()).thenReturn(mock(Connection.class));
    JasperPrint jasperPrint = mock(JasperPrint.class);
    PowerMockito.when(JasperFillManager.fillReport(any(JasperReport.class), anyMap(),
        any(Connection.class)))
        .thenReturn(jasperPrint);
    PowerMockito.when(jasperPdfExporter.exportReport())
        .thenReturn(expectedReportData);

    byte[] actualReportData = jasperReportsViewService.generateReport(template,
        getParamsWithFormat(PDF));

    assertEquals(expectedReportData, actualReportData);
  }

  @Test
  public void generateReportShouldReturnReportForBeanDataSource() throws Exception {
    when(objectInputStream.readObject()).thenReturn(jasperReport);
    JasperPrint jasperPrint = mock(JasperPrint.class);
    PowerMockito.when(JasperFillManager.fillReport(any(JasperReport.class), anyMap(),
        any(JRBeanCollectionDataSource.class)))
        .thenReturn(jasperPrint);
    PowerMockito.when(jasperPdfExporter.exportReport())
        .thenReturn(expectedReportData);
    Map<String, Object> params = new HashMap<>();
    params.put(PARAM_DATASOURCE, new ArrayList<>());

    byte[] actualReportData = jasperReportsViewService.generateReport(template, params);

    assertEquals(expectedReportData, actualReportData);
  }

  @Test
  public void shouldSelectCsvExporterForCsvFormat() throws Exception {
    jasperReportsViewService.generateReport(template, getParamsWithFormat("csv"));
    verify(jasperCsvExporter, times(1)).exportReport();
  }

  @Test
  public void shouldSelectPdfExporterForPdfFormat() throws Exception {
    jasperReportsViewService.generateReport(template, getParamsWithFormat("pdf"));
    verify(jasperPdfExporter, times(1)).exportReport();
  }

  @Test
  public void shouldSelectXlsExporterForXlsFormat() throws Exception {
    jasperReportsViewService.generateReport(template, getParamsWithFormat("xls"));
    verify(jasperXlsExporter, times(1)).exportReport();
  }

  @Test
  public void shouldSelectHtmlExporterForHtmlFormat() throws Exception {
    jasperReportsViewService.generateReport(template, getParamsWithFormat("html"));
    verify(jasperHtmlExporter, times(1)).exportReport();
  }

  @Test(expected = JasperReportViewException.class)
  public void shouldThrowJasperReportViewExceptionForUnknownFormat() {
    jasperReportsViewService.generateReport(template, getParamsWithFormat("odt"));
  }

  private Map<String, Object> getParamsWithFormat(String format) {
    Map<String, Object> params = new HashMap<>();
    params.put(FORMAT, format);
    return params;
  }

  private void initializeExporterMocks() throws Exception {
    whenNew(JasperCsvExporter.class).withAnyArguments().thenReturn(jasperCsvExporter);
    whenNew(JasperXlsExporter.class).withAnyArguments().thenReturn(jasperXlsExporter);
    whenNew(JasperHtmlExporter.class).withAnyArguments().thenReturn(jasperHtmlExporter);
    whenNew(JasperPdfExporter.class).withAnyArguments().thenReturn(jasperPdfExporter);
  }
}
