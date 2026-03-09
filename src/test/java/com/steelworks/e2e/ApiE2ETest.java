package com.steelworks.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiE2ETest {

    @LocalServerPort
    private int port;

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
    void setUp() {
        shippingLogRepository.deleteAll();
        productionLogRepository.deleteAll();
        lotRepository.deleteAll();
        customerRepository.deleteAll();
        defectTypeRepository.deleteAll();
        productionLineRepository.deleteAll();

        ProductionLine line = new ProductionLine();
        line.setLineName("Line-E2E");
        line.setDepartment("MFG");
        line = productionLineRepository.save(line);

        DefectType defectType = new DefectType();
        defectType.setDefectCode("D-E2E");
        defectType.setDefectName("Edge Crack");
        defectType.setSeverity("Critical");
        defectType.setDescription("Critical edge crack");
        defectType = defectTypeRepository.save(defectType);

        Customer customer = new Customer();
        customer.setCustomerName("E2E Customer");
        customer.setRegion("US");
        customer = customerRepository.save(customer);

        Lot lot = new Lot();
        lot.setLotIdentifier("LOT-E2E-1");
        lot.setPartNumber("PN-E2E");
        lot.setCreatedDate(LocalDate.now());
        lot = lotRepository.save(lot);

        ProductionLog productionLog = new ProductionLog();
        productionLog.setLot(lot);
        productionLog.setProductionLine(line);
        productionLog.setDefectType(defectType);
        productionLog.setProductionDate(LocalDate.now());
        productionLog.setShift("Day");
        productionLog.setUnitsPlanned(100);
        productionLog.setUnitsActual(92);
        productionLog.setDowntimeMinutes(12);
        productionLog.setIssueFlag(true);
        productionLogRepository.save(productionLog);

        ShippingLog shippingLog = new ShippingLog();
        shippingLog.setLot(lot);
        shippingLog.setCustomer(customer);
        shippingLog.setShipDate(LocalDate.now());
        shippingLog.setSalesOrderNumber("SO-E2E");
        shippingLog.setDestinationState("IN");
        shippingLog.setCarrier("UPS");
        shippingLog.setBolNumber("BOL-E2E");
        shippingLog.setTrackingNumber("TRK-E2E");
        shippingLog.setQtyShipped(92);
        shippingLog.setShipStatus("Shipped");
        shippingLogRepository.save(shippingLog);
    }

    @Test
    void playwright_lotSearchEndpointReturnsExpectedPayload() {
        String body = fetchPageBody("/api/lots/search?lotId=LOTE2E1");
        assertTrue(body.contains("LOT-E2E-1"), "Lot search should include seeded lot identifier");
    }

    @Test
    void playwright_dashboardEndpointReturnsWeeklyGrouping() {
        String body = fetchPageBody("/api/dashboard/summary?timeGrouping=WEEKLY");
        assertTrue(body.contains("\"timeGrouping\":\"WEEKLY\""),
                "Dashboard should include selected WEEKLY grouping");
    }

    private String fetchPageBody(String path) {
        String baseUrl = "http://localhost:" + port;
        try (Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium()
                        .launch(new LaunchOptions().setHeadless(true));
                Page page = browser.newPage()) {
            Response response = page.navigate(baseUrl + path);
            if (response == null || !response.ok()) {
                throw new AssertionError(
                        "Expected HTTP 200 for path " + path + ", but got " + response);
            }
            return page.textContent("body");
        }
    }
}
