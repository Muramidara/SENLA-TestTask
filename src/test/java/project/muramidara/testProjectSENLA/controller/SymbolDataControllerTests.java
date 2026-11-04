package project.muramidara.testProjectSENLA.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import project.muramidara.testProjectSENLA.dto.AveragePriceDto;
import project.muramidara.testProjectSENLA.dto.SymbolDataDto;
import project.muramidara.testProjectSENLA.dto.SymbolPageDto;
import project.muramidara.testProjectSENLA.dto.TrendDto;
import project.muramidara.testProjectSENLA.exception.IllegalDateValueException;
import project.muramidara.testProjectSENLA.exception.SymbolDataNotFoundException;
import project.muramidara.testProjectSENLA.service.SymbolDataService;
import project.muramidara.testProjectSENLA.util.DataUtils;


import java.math.BigDecimal;
import java.time.LocalDate;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest
public class SymbolDataControllerTests {

    @Autowired
    private MockMvc mockMvc;


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

    @Test
    @DisplayName("Test find last functionality")
    public void givenNoSavedSymbolData_whenFindLast_thenErrorResponse() throws Exception {
        //given
        BDDMockito.given(symbolDataService.getLastSymbolData())
                .willThrow(new SymbolDataNotFoundException("SymbolData not found"));

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/price"));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(404)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("SymbolData not found")));
    }

    @Test
    @DisplayName("Test find average functionality")
    public void givenSavedSymbolData_whenFindAverage_thenSuccessResponse() throws Exception {
        //given
        AveragePriceDto expectedAveragePriceDto = DataUtils.getAveragePriceDto1();
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);
        BDDMockito.given(symbolDataService.getAveragePrice(fromDate, toDate))
                .willReturn(expectedAveragePriceDto);

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices/average")
                .param("from", fromDate.toString())
                .param("to", toDate.toString()));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.averagePrice",
                        CoreMatchers.is(expectedAveragePriceDto.getAveragePrice()), BigDecimal.class));
    }

    @Test
    @DisplayName("Test find average incorrect dates functionality")
    public void givenIncorrectDates_whenFindAverage_thenErrorResponse() throws Exception {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 31);
        LocalDate toDate = LocalDate.of(2000, 1, 1);
        BDDMockito.given(symbolDataService.getAveragePrice(fromDate, toDate))
                .willThrow(new IllegalDateValueException("fromDate(%s) can't be after toDate(%s)"
                        .formatted(fromDate, toDate)));

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices/average")
                .param("from", fromDate.toString())
                .param("to", toDate.toString()));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(404)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("fromDate(%s) can't be after toDate(%s)".formatted(fromDate, toDate))));
    }

    @Test
    @DisplayName("Test find average not found functionality")
    public void givenNoSavedSymbolData_whenFindAverage_thenErrorResponse() throws Exception {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);
        BDDMockito.given(symbolDataService.getAveragePrice(fromDate, toDate))
                .willThrow(new SymbolDataNotFoundException("SymbolData not found"));

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices/average")
                .param("from", fromDate.toString())
                .param("to", toDate.toString()));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(404)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("SymbolData not found")));
    }


    @Test
    @DisplayName("Test find all symbols in period functionality")
    public void givenSavedSymbolData_whenFindAllInPeriod_thenSuccessResponse() throws Exception {
        //given
        SymbolPageDto expectedSymbolPageDto = DataUtils.getSymbolPageDto1();
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);
        int page = 0;
        int size = 10;

        BDDMockito.given(symbolDataService.getAllSymbolsInPeriod(fromDate, toDate, page, size))
                .willReturn(expectedSymbolPageDto);

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices")
                .param("from", fromDate.toString())
                .param("to", toDate.toString())
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.page",
                        CoreMatchers.is(expectedSymbolPageDto.getPage())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size",
                        CoreMatchers.is(expectedSymbolPageDto.getSize())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements",
                        CoreMatchers.is((int) expectedSymbolPageDto.getTotalElements())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext",
                        CoreMatchers.is(expectedSymbolPageDto.isHasNext())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()",
                        CoreMatchers.is(expectedSymbolPageDto.getItems().size())));
    }

    @Test
    @DisplayName("Test find all symbols in period with incorrect dates functionality")
    public void givenIncorrectDates_whenFindAllInPeriod_thenErrorResponse() throws Exception {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 31);
        LocalDate toDate = LocalDate.of(2000, 1, 1);
        int page = 0;
        int size = 10;

        BDDMockito.given(symbolDataService.getAllSymbolsInPeriod(fromDate, toDate, page, size))
                .willThrow(new IllegalDateValueException("fromDate(%s) can't be after toDate(%s)"
                        .formatted(fromDate, toDate)));

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices")
                .param("from", fromDate.toString())
                .param("to", toDate.toString())
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(404)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("fromDate(%s) can't be after toDate(%s)"
                                .formatted(fromDate, toDate))));
    }

    @Test
    @DisplayName("Test find all symbols in period with default pagination functionality")
    public void givenNoPaginationParameters_whenFindAllInPeriod_thenDefaultPaginationIsUsed() throws Exception {
        //given
        SymbolPageDto expectedSymbolPageDto = DataUtils.getSymbolPageDto1();
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);

        BDDMockito.given(symbolDataService.getAllSymbolsInPeriod(fromDate, toDate, 0, 10))
                .willReturn(expectedSymbolPageDto);

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices")
                .param("from", fromDate.toString())
                .param("to", toDate.toString()));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.page",
                        CoreMatchers.is(expectedSymbolPageDto.getPage())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size",
                        CoreMatchers.is(expectedSymbolPageDto.getSize())));

        BDDMockito.then(symbolDataService)
                .should()
                .getAllSymbolsInPeriod(fromDate, toDate, 0, 10);
    }


    @Test
    @DisplayName("Test find trend in period functionality")
    public void givenSavedSymbolData_whenFindTrendInPeriod_thenSuccessResponse() throws Exception {
        //given
        TrendDto expectedTrendDto = DataUtils.getTrendDto1();
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);

        BDDMockito.given(symbolDataService.getTrend(fromDate, toDate))
                .willReturn(expectedTrendDto);

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices/trend")
                .param("from", fromDate.toString())
                .param("to", toDate.toString()));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.symbol",
                        CoreMatchers.is(expectedTrendDto.getSymbol())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.start_date",
                        CoreMatchers.is(expectedTrendDto.getStart_date())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.end_date",
                        CoreMatchers.is(expectedTrendDto.getEnd_date())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.trend",
                        CoreMatchers.is(expectedTrendDto.getTrend())));
    }

    @Test
    @DisplayName("Test find trend in period with incorrect dates functionality")
    public void givenIncorrectDates_whenFindTrendInPeriod_thenErrorResponse() throws Exception {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 31);
        LocalDate toDate = LocalDate.of(2000, 1, 1);

        BDDMockito.given(symbolDataService.getTrend(fromDate, toDate))
                .willThrow(new IllegalDateValueException("fromDate(%s) can't be after toDate(%s)"
                        .formatted(fromDate, toDate)));

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices/trend")
                .param("from", fromDate.toString())
                .param("to", toDate.toString()));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(404)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("fromDate(%s) can't be after toDate(%s)"
                                .formatted(fromDate, toDate))));
    }

    @Test
    @DisplayName("Test find trend in period with no saved symbol data functionality")
    public void givenNoSavedSymbolData_whenFindTrendInPeriod_thenErrorResponse() throws Exception {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);

        BDDMockito.given(symbolDataService.getTrend(fromDate, toDate))
                .willThrow(new SymbolDataNotFoundException("SymbolData not found"));

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices/trend")
                .param("from", fromDate.toString())
                .param("to", toDate.toString()));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(404)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("SymbolData not found")));
    }


}
