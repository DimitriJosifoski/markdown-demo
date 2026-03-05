package com.steelworks.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.steelworks.model.Customer;
import com.steelworks.model.DefectType;
import com.steelworks.model.Lot;
import com.steelworks.model.ProductionLine;
import com.steelworks.model.ProductionLog;
import com.steelworks.model.ShippingLog;
import com.steelworks.repository.CustomerRepository;
import com.steelworks.repository.DefectTypeRepository;
import com.steelworks.repository.LotRepository;
import com.steelworks.repository.ProductionLineRepository;
import com.steelworks.repository.ProductionLogRepository;
import com.steelworks.repository.ShippingLogRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    private static final String CONFLICT_LOT_IDENTIFIER = "LOT-900";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShippingLogRepository shippingLogRepository;

    @Autowired
    private ProductionLogRepository productionLogRepository;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DefectTypeRepository defectTypeRepository;

    @Autowired
    private ProductionLineRepository productionLineRepository;

    @BeforeEach
    void cleanDatabase() {
        shippingLogRepository.deleteAll();
        productionLogRepository.deleteAll();
        lotRepository.deleteAll();
        customerRepository.deleteAll();
        defectTypeRepository.deleteAll();
        productionLineRepository.deleteAll();
    }

    @Test
    void lotSearch_supportsFuzzyMatchAndCrossReferenceFields() throws Exception {
        ProductionLine line = saveProductionLine("Line-A");
        DefectType criticalDefect = saveDefectType("D-CRACK", "Crack", "Critical");
        Customer customer = saveCustomer("Acme Steel");
        Lot lot = saveLot("LOT-123", "PN-9");
        saveProductionLog(lot, line, criticalDefect, LocalDate.now().minusDays(1), true);
        saveShippingLog(lot, customer, LocalDate.now());

        String responseBody = mockMvc.perform(get("/api/lots/search").param("lotId", "lot123"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].lotIdentifier", is("LOT-123")))
                .andExpect(jsonPath("$[0].shippingStatus", is("SHIPPED")))
                .andExpect(jsonPath("$[0].defectSeverity", is("CRITICAL"))).andReturn()
                .getResponse().getContentAsString();
        assertTrue(responseBody.contains("LOT-123"),
                "Lot search response should include target lot");
    }

    @Test
    void dashboardSummary_returnsRankingsAndRiskAlerts() throws Exception {
        ProductionLine line = saveProductionLine("Line-A");
        DefectType criticalDefect = saveDefectType("D-BURR", "Burr", "Critical");
        Customer customer = saveCustomer("Acme Steel");
        Lot lot = saveLot("LOT-500", "PN-500");
        saveProductionLog(lot, line, criticalDefect, LocalDate.now().minusDays(2), true);
        saveShippingLog(lot, customer, LocalDate.now().minusDays(1));

        String responseBody = mockMvc
                .perform(get("/api/dashboard/summary").param("timeGrouping", "WEEKLY"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.timeGrouping", is("WEEKLY")))
                .andExpect(jsonPath("$.productionLineRankings", hasSize(1)))
                .andExpect(jsonPath("$.shippingRiskAlerts", hasSize(1))).andReturn().getResponse()
                .getContentAsString();
        assertTrue(responseBody.contains("WEEKLY"),
                "Dashboard response should contain selected time grouping");
    }

    @Test
    void conflictAndOrphanEndpoints_flagExpectedLots() throws Exception {
        ProductionLine lineA = saveProductionLine("Line-A");
        ProductionLine lineB = saveProductionLine("Line-B");
        DefectType defect = saveDefectType("D-PIT", "Pitting", "Major");
        Lot conflictLot = saveLot(CONFLICT_LOT_IDENTIFIER, "PN-900");
        saveProductionLog(conflictLot, lineA, defect, LocalDate.now().minusDays(1), true);
        saveProductionLog(conflictLot, lineB, defect, LocalDate.now(), true);

        String conflictsBody = mockMvc.perform(get("/api/lots/conflicts"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].lotIdentifier", is(CONFLICT_LOT_IDENTIFIER))).andReturn()
                .getResponse().getContentAsString();

        mockMvc.perform(get("/api/lots/orphaned")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].lotIdentifier", is(CONFLICT_LOT_IDENTIFIER)))
                .andExpect(jsonPath("$[0].inShipping", is(false)));

        assertTrue(conflictsBody.contains(CONFLICT_LOT_IDENTIFIER),
                "Conflict endpoint should return the conflicting lot");
    }

    private ProductionLine saveProductionLine(String lineName) {
        ProductionLine line = new ProductionLine();
        line.setLineName(lineName);
        line.setDepartment("MFG");
        return productionLineRepository.save(line);
    }

    private DefectType saveDefectType(String defectCode, String defectName, String severity) {
        DefectType defectType = new DefectType();
        defectType.setDefectCode(defectCode);
        defectType.setDefectName(defectName);
        defectType.setSeverity(severity);
        defectType.setDescription(defectName + " defect");
        return defectTypeRepository.save(defectType);
    }

    private Customer saveCustomer(String customerName) {
        Customer customer = new Customer();
        customer.setCustomerName(customerName);
        customer.setRegion("US");
        return customerRepository.save(customer);
    }

    private Lot saveLot(String lotIdentifier, String partNumber) {
        Lot lot = new Lot();
        lot.setLotIdentifier(lotIdentifier);
        lot.setPartNumber(partNumber);
        lot.setCreatedDate(LocalDate.now());
        return lotRepository.save(lot);
    }

    private ProductionLog saveProductionLog(Lot lot, ProductionLine line, DefectType defectType,
            LocalDate productionDate, boolean issueFlag) {
        ProductionLog log = new ProductionLog();
        log.setLot(lot);
        log.setProductionLine(line);
        log.setDefectType(defectType);
        log.setProductionDate(productionDate);
        log.setShift("Day");
        log.setUnitsPlanned(100);
        log.setUnitsActual(95);
        log.setDowntimeMinutes(15);
        log.setIssueFlag(issueFlag);
        return productionLogRepository.save(log);
    }

    private ShippingLog saveShippingLog(Lot lot, Customer customer, LocalDate shipDate) {
        ShippingLog log = new ShippingLog();
        log.setLot(lot);
        log.setCustomer(customer);
        log.setShipDate(shipDate);
        log.setSalesOrderNumber("SO-" + lot.getId());
        log.setDestinationState("IN");
        log.setCarrier("UPS");
        log.setBolNumber("BOL-" + lot.getId() + "-" + shipDate);
        log.setTrackingNumber("TRK-" + lot.getId());
        log.setQtyShipped(90);
        log.setShipStatus("Shipped");
        return shippingLogRepository.save(log);
    }
}
