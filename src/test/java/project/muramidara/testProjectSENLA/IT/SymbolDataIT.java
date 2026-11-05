package project.muramidara.testProjectSENLA.IT;


import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.utility.TestcontainersConfiguration;
import project.muramidara.testProjectSENLA.dto.TrendDto;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;
import project.muramidara.testProjectSENLA.repository.SymbolDataRepository;
import project.muramidara.testProjectSENLA.util.DataUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
                        CoreMatchers.is(savedSymbolDataEntity.getFetchedAt().toString())));
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


    @Test
    @DisplayName("Test find average functionality")
    public void givenSavedSymbolData_whenFindAverage_thenSuccessResponse() throws Exception {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);

        symbolDataRepository.saveAll(List.of(
                DataUtils.getSymbolDataEntity1Transient(),
                DataUtils.getSymbolDataEntity2Transient()
        ));

        BigDecimal expectedAveragePrice = new BigDecimal("1.5");

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices/average")
                .param("from", fromDate.toString())
                .param("to", toDate.toString()));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.averagePrice",
                        CoreMatchers.is(expectedAveragePrice),
                        BigDecimal.class));
    }

    @Test
    @DisplayName("Test find average incorrect dates functionality")
    public void givenIncorrectDates_whenFindAverage_thenErrorResponse() throws Exception {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 31);
        LocalDate toDate = LocalDate.of(2000, 1, 1);

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices/average")
                .param("from", fromDate.toString())
                .param("to", toDate.toString()));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",
                        CoreMatchers.is(404)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("fromDate(%s) can't be after toDate(%s)"
                                .formatted(fromDate, toDate))));
    }

    @Test
    @DisplayName("Test find average not found functionality")
    public void givenNoSavedSymbolData_whenFindAverage_thenErrorResponse() throws Exception {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices/average")
                .param("from", fromDate.toString())
                .param("to", toDate.toString()));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",
                        CoreMatchers.is(404)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("SymbolData not found")));
    }

    @Test
    @DisplayName("Test find all symbols in period functionality")
    public void givenSavedSymbolData_whenFindAllInPeriod_thenSuccessResponse() throws Exception {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);
        int page = 0;
        int size = 10;

        symbolDataRepository.saveAll(List.of(
                DataUtils.getSymbolDataEntity1Transient(),
                DataUtils.getSymbolDataEntity2Transient(),
                DataUtils.getSymbolDataEntity10Transient()
        ));

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
                        CoreMatchers.is(page)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size",
                        CoreMatchers.is(size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements",
                        CoreMatchers.is(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext",
                        CoreMatchers.is(false)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()",
                        CoreMatchers.is(3)));
    }

    @Test
    @DisplayName("Test find all symbols in period with incorrect dates functionality")
    public void givenIncorrectDates_whenFindAllInPeriod_thenErrorResponse() throws Exception {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 31);
        LocalDate toDate = LocalDate.of(2000, 1, 1);
        int page = 0;
        int size = 10;

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices")
                .param("from", fromDate.toString())
                .param("to", toDate.toString())
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",
                        CoreMatchers.is(404)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("fromDate(%s) can't be after toDate(%s)"
                                .formatted(fromDate, toDate))));
    }

    @Test
    @DisplayName("Test find all symbols in period with default pagination functionality")
    public void givenNoPaginationParameters_whenFindAllInPeriod_thenDefaultPaginationIsUsed() throws Exception {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);

        symbolDataRepository.save(DataUtils.getSymbolDataEntity1Transient());

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices")
                .param("from", fromDate.toString())
                .param("to", toDate.toString()));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.page",
                        CoreMatchers.is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size",
                        CoreMatchers.is(10)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements",
                        CoreMatchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext",
                        CoreMatchers.is(false)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()",
                        CoreMatchers.is(1)));
    }

    @Test
    @DisplayName("Test find trend in period functionality")
    public void givenSavedSymbolData_whenFindTrendInPeriod_thenSuccessResponse() throws Exception {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);

        symbolDataRepository.saveAll(List.of(
                DataUtils.getSymbolDataEntity1Transient(),
                DataUtils.getSymbolDataEntity20Transient()
        ));

        TrendDto expectedTrendDto = DataUtils.getTrendDto1();

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

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices/trend")
                .param("from", fromDate.toString())
                .param("to", toDate.toString()));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",
                        CoreMatchers.is(404)))
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

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/prices/trend")
                .param("from", fromDate.toString())
                .param("to", toDate.toString()));

        //then
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",
                        CoreMatchers.is(404)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("SymbolData not found")));
    }

}
