package org.openlmis.fulfillment.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.domain.FacilityFtpSetting;
import org.openlmis.fulfillment.referencedata.model.FacilityDto;
import org.openlmis.fulfillment.referencedata.service.FacilityReferenceDataService;
import org.openlmis.fulfillment.repository.FacilityFtpSettingRepository;

import java.util.Random;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class FacilityFtpSettingServiceTest {
  @Mock
  private FacilityFtpSettingRepository facilityFtpSettingRepository;

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @InjectMocks
  private FacilityFtpSettingService facilityFtpSettingService;

  @Test
  public void shouldSaveSetting() throws DuplicateFacilityFtpSettingException {
    // given
    final FacilityFtpSetting setting = randomSetting();
    final FacilityDto facility = mock(FacilityDto.class);

    when(facility.getId()).thenReturn(UUID.randomUUID());
    when(facilityReferenceDataService.findOne(any(UUID.class))).thenReturn(facility);
    when(facilityFtpSettingRepository.findFirstByFacilityId(any(UUID.class)))
        .thenReturn(null);

    // when
    facilityFtpSettingService.save(setting);

    // then
    verify(facilityFtpSettingRepository, atLeastOnce()).save(setting);
  }

  @Test(expected = DuplicateFacilityFtpSettingException.class)
  public void shouldNotSaveSettingIfFacilityIdDuplicated()
      throws DuplicateFacilityFtpSettingException {
    // given
    final FacilityFtpSetting setting = randomSetting();
    final FacilityFtpSetting duplicate = randomSetting();
    FacilityDto facility = mock(FacilityDto.class);

    when(facility.getId()).thenReturn(UUID.randomUUID());
    when(facilityReferenceDataService.findOne(any(UUID.class))).thenReturn(facility);
    when(facilityFtpSettingRepository.findFirstByFacilityId(any(UUID.class)))
        .thenReturn(duplicate);

    // when
    facilityFtpSettingService.save(setting);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotSaveSettingIfFacilityDoesNotExist()
      throws DuplicateFacilityFtpSettingException {
    // given
    FacilityFtpSetting setting = randomSetting();

    when(facilityReferenceDataService.findOne(any(UUID.class))).thenReturn(null);
    when(facilityFtpSettingRepository.findFirstByFacilityId(any(UUID.class)))
        .thenReturn(null);

    // when
    facilityFtpSettingService.save(setting);
  }

  @Test
  public void shouldGetByFacility() {
    // given
    FacilityFtpSetting setting = randomSetting();

    when(facilityFtpSettingRepository.findFirstByFacilityId(any(UUID.class)))
        .thenReturn(setting);

    // when
    FacilityFtpSetting result = facilityFtpSettingService.getByFacility(setting.getFacilityId());

    // then
    assertEquals(result.getId(), setting.getId());
  }

  @Test
  public void shouldNotGetByFacilityIfFacilityDoesNotExist() {
    // given
    FacilityFtpSetting setting = randomSetting();

    when(facilityFtpSettingRepository.findFirstByFacilityId(any(UUID.class)))
        .thenReturn(null);

    // when
    FacilityFtpSetting result = facilityFtpSettingService.getByFacility(setting.getFacilityId());

    // then
    assertNull(result);
  }

  private FacilityFtpSetting randomSetting() {
    FacilityFtpSetting setting = new FacilityFtpSetting();
    setting.setId(UUID.randomUUID());
    setting.setFacilityId(UUID.randomUUID());
    setting.setServerHost(RandomStringUtils.random(10));
    setting.setServerPort(new Random().nextInt(1000));
    setting.setRemoveDirectory(RandomStringUtils.random(10));
    setting.setUsername(RandomStringUtils.random(10));
    setting.setPassword(RandomStringUtils.random(10));
    return setting;
  }
}
