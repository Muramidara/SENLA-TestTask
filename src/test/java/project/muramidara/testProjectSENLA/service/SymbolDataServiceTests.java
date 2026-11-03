package project.muramidara.testProjectSENLA.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.muramidara.testProjectSENLA.dto.SymbolDataDto;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;
import project.muramidara.testProjectSENLA.mapper.SymbolDataMapper;
import project.muramidara.testProjectSENLA.mapper.SymbolPageDtoMapper;
import project.muramidara.testProjectSENLA.repository.SymbolDataRepository;
import project.muramidara.testProjectSENLA.util.DataUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class SymbolDataServiceTests {

    @Mock
    private SymbolDataRepository symbolDataRepository;
    @Mock
    private SymbolDataMapper symbolDataMapper;
    @Mock
    private SymbolPageDtoMapper symbolPageDtoMapper;
    @InjectMocks
    private SymbolDataService symbolDataService;

    @Test
    @DisplayName("Test find last symboldata functionality")
    public void givenSavedSymbol_whenGetLastSymbolData_thenSymbolDataDtoIsReturned(){
        //given
        Optional<SymbolDataEntity> savedSymbolData = Optional.of(DataUtils.getSymbolDataEntity1Persistent());
        SymbolDataDto returnedSymbolDataDto = DataUtils.getSymbolDataDto1();
        BDDMockito.given(symbolDataRepository.findFirstByOrderByFetchedAtDesc()).willReturn(savedSymbolData);
        BDDMockito.given(symbolDataMapper.mapEntityToDto(any(SymbolDataEntity.class)))
                .willReturn(returnedSymbolDataDto);
        //when
        SymbolDataDto savedSymbolDataDto = symbolDataService.getLastSymbolData();

        //then
        assertThat(savedSymbolDataDto).isNotNull();
    }

}
