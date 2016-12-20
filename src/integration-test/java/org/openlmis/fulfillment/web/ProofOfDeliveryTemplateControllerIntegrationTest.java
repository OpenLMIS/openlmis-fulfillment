package org.openlmis.fulfillment.web;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.domain.Template;
import org.openlmis.fulfillment.domain.TemplateParameter;
import org.openlmis.fulfillment.repository.TemplateRepository;
import org.openlmis.fulfillment.service.ReportingException;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

@SuppressWarnings({"PMD.UnusedPrivateField"})
public class ProofOfDeliveryTemplateControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/proofOfDeliveryTemplates";
  private static final String ACCESS_TOKEN = "access_token";
  private static final String PRINT_POD = "Print POD";
  private static final String DESCRIPTION_POD = "Template to print Proof Of Delivery";
  private static final String CONSISTENCY_REPORT = "Consistency Report";
  private ClassPathResource podReport;

  @MockBean
  private TemplateRepository templateRepository;

  @Before
  public void setUp() throws IOException {
    this.setUpBootstrapData();

    podReport = new ClassPathResource("reports/podPrint.jrxml");
  }

  @Test
  public void shouldSavePodReportTemplate() throws Exception {
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType("multipart/form-data")
        .multiPart("file", podReport.getFilename(), podReport.getInputStream())
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDownloadPodTeportTemplate() throws Exception {
    Template template = new Template(PRINT_POD, null, null, CONSISTENCY_REPORT, DESCRIPTION_POD);

    JasperReport report = JasperCompileManager.compileReport(podReport.getInputStream());
    JRParameter[] jrParameters = report.getParameters();
    if (jrParameters != null && jrParameters.length > 0) {

      ArrayList<TemplateParameter> parameters = new ArrayList<>();
      for (JRParameter jrParameter : jrParameters) {
        if (!jrParameter.isSystemDefined()) {
          parameters.add(createParameter(jrParameter));
        }
      }
      template.setTemplateParameters(parameters);
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bos);
    out.writeObject(report);
    template.setData(bos.toByteArray());

    given(templateRepository.findByName(anyString())).willReturn(template);

    String jrxml = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType("application/xml")
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .body()
        .asString();

    assertNotNull(jrxml);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenThereIsNoProofOfDeliveryTemplate() {

    given(templateRepository.findByName(anyString())).willReturn(null);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType("application/xml")
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private TemplateParameter createParameter(JRParameter jrParameter) throws ReportingException {
    String displayName = jrParameter.getPropertiesMap().getProperty("displayName");
    String dataType = jrParameter.getValueClassName();

    TemplateParameter templateParameter = new TemplateParameter();
    templateParameter.setName(jrParameter.getName());
    templateParameter.setDisplayName(displayName);
    templateParameter.setDescription(jrParameter.getDescription());
    templateParameter.setDataType(dataType);

    String selectSql = jrParameter.getPropertiesMap().getProperty("selectSql");

    if (isNotBlank(selectSql)) {
      templateParameter.setSelectSql(selectSql);
    }

    if (jrParameter.getDefaultValueExpression() != null) {
      templateParameter.setDefaultValue(jrParameter.getDefaultValueExpression()
          .getText().replace("\"", "").replace("\'", ""));
    }

    return templateParameter;
  }
}
