package ru.netology.patient.service.medical;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;

import java.math.BigDecimal;
import java.time.LocalDate;


import static org.junit.jupiter.api.Assertions.*;

class MedicalServiceImplTest {
    PatientInfo patientInfo1 = new PatientInfo("123", "Vasya", "Ivanov", LocalDate.of(1999, 01, 01),
            new HealthInfo(new BigDecimal("37.0"), new BloodPressure(120, 70)));
    PatientInfoRepository patientInfoRepositoryMock = Mockito.mock(PatientInfoRepository.class);
    SendAlertService sendAlertServiceMock = Mockito.mock(SendAlertService.class);
    MedicalService medicalService = new MedicalServiceImpl(patientInfoRepositoryMock, sendAlertServiceMock);

    @BeforeEach
    void setUp() {
        Mockito.when(patientInfoRepositoryMock.getById(Mockito.anyString()))
                .thenReturn(patientInfo1);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "190, 60, 1",
            "120, 70, 0"
    })
    void checkBloodPressure(int high, int low, int times) {
        medicalService.checkBloodPressure("123", new BloodPressure(high, low));
        Mockito.verify(sendAlertServiceMock, Mockito.times(times)).send(Mockito.anyString());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "36.6,0",
            "35.0,1",
            "35.5,0",
            "37.0,0"
    })
    void checkTemperature(BigDecimal temperature, int times) {
        medicalService.checkTemperature("123", temperature);
        Mockito.verify(sendAlertServiceMock, Mockito.times(times)).send(Mockito.anyString());
    }

    @Test
    void checkBloodPressureMessage() {
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        medicalService.checkBloodPressure("123", new BloodPressure(190, 70));
        Mockito.verify(sendAlertServiceMock).send(argumentCaptor.capture());
        String result = "Warning, patient with id: 123, need help";
        assertEquals(result, argumentCaptor.getValue());

    }

    @Test
    void checkTemperatureMessage() {
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepositoryMock, sendAlertServiceMock);
        medicalService.checkTemperature("123", new BigDecimal("35.0"));
        Mockito.verify(sendAlertServiceMock).send(argumentCaptor.capture());
        String result = "Warning, patient with id: 123, need help";
        assertEquals(result, argumentCaptor.getValue());
    }
}