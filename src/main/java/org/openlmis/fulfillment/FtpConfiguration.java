package org.openlmis.fulfillment;

import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.PollableChannel;

@Configuration
@Import({JmxAutoConfiguration.class, IntegrationAutoConfiguration.class})
@IntegrationComponentScan
public class FtpConfiguration {
  private static final String FTP_HOST_NAME = "ftp";
  private static final int FTP_PORT = 21;
  private static final String FTP_USER = "admin";
  private static final String FTP_PASS = "p@ssw0rd";
  private static final String FTP_PATH = "/orders/files/csv";

  /**
   * Creates new DefaultFtpSessionFactory.
   *
   * @return Created DefaultFtpSessionFactory.
   */
  @Bean
  public DefaultFtpSessionFactory ftpSessionFactory() {
    DefaultFtpSessionFactory factory = new DefaultFtpSessionFactory();
    factory.setHost(FTP_HOST_NAME);
    factory.setPort(FTP_PORT);
    factory.setUsername(FTP_USER);
    factory.setPassword(FTP_PASS);

    return factory;
  }

  @Bean
  public PollableChannel toFtpChannel() {
    return new QueueChannel();
  }

  @Bean(name = PollerMetadata.DEFAULT_POLLER)
  public PollerMetadata poller() {
    return Pollers.fixedRate(500).get();
  }

  /**
   * Creates new IntegrationFlow.
   *
   * @return Created IntegrationFlow.
   */
  @Bean
  public IntegrationFlow ftpOutboundFlow() {
    return IntegrationFlows.from("toFtpChannel")
        .handle(Ftp.outboundAdapter(ftpSessionFactory(), FileExistsMode.FAIL)
            .useTemporaryFileName(false)
            .fileNameExpression("headers['" + FileHeaders.FILENAME + "']")
            .remoteDirectory(FTP_PATH)
            .autoCreateDirectory(true)
        ).get();
  }

}
