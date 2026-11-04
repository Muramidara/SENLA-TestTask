package project.muramidara.testProjectSENLA.controller;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import project.muramidara.testProjectSENLA.dto.SymbolDataDto;
import project.muramidara.testProjectSENLA.service.SymbolDataService;
import project.muramidara.testProjectSENLA.util.DataUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest
public class SymbolDataControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SymbolDataService symbolDataService;

    @Test
    @DisplayName("Test find last functionality")
    public void givenSavedSymbolData_whenFindLast_thenSuccessResponse() throws Exception {
        //given
        SymbolDataDto expectedSymbolDataDto = DataUtils.getSymbolDataDto1();
        BDDMockito.given(symbolDataService.getLastSymbolData())
                .willReturn(expectedSymbolDataDto);

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/price"));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.symbol",
                        CoreMatchers.is(expectedSymbolDataDto.getSymbol())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price",
                        CoreMatchers.is(expectedSymbolDataDto.getPrice())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.fetchedAt",
                        CoreMatchers.is(expectedSymbolDataDto.getFetchedAt())));
    }

}
