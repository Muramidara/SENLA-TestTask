package project.muramidara.testProjectSENLA.IT;


import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.utility.TestcontainersConfiguration;
import project.muramidara.testProjectSENLA.dto.SymbolDataDto;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;
import project.muramidara.testProjectSENLA.exception.SymbolDataNotFoundException;
import project.muramidara.testProjectSENLA.repository.SymbolDataRepository;
import project.muramidara.testProjectSENLA.util.DataUtils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class SymbolDataIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SymbolDataRepository symbolDataRepository;

    @BeforeEach
    public void setUp(){
        symbolDataRepository.deleteAll();
    }

    @Test
    @DisplayName("Test find last functionality")
    public void givenSavedSymbolData_whenFindLast_thenSuccessResponse() throws Exception {
        //given
        SymbolDataEntity savedSymbolDataEntity = DataUtils.getSymbolDataEntity1Transient();
        symbolDataRepository.save(savedSymbolDataEntity);

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/price"));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.symbol",
                        CoreMatchers.is(savedSymbolDataEntity.getSymbol())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price",
                        CoreMatchers.is(savedSymbolDataEntity.getPrice().toPlainString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.fetchedAt",
                        CoreMatchers.is(savedSymbolDataEntity.getFetchedAt())));
    }

    @Test
    @DisplayName("Test find last not found functionality")
    public void givenNoSavedSymbolData_whenFindLast_thenErrorResponse() throws Exception {
        //given


        //when
        ResultActions result = mockMvc.perform(get("/api/v1/price"));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(404)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("SymbolData not found")));
    }
}
