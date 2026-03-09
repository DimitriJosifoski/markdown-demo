package com.steelworks.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.steelworks.dto.ConsolidatedLotView;
import com.steelworks.dto.DataConflictDTO;
import com.steelworks.dto.LotSearchRequest;
import com.steelworks.dto.LotSearchResult;
import com.steelworks.dto.OrphanedRecordDTO;
import com.steelworks.service.DataIntegrityService;
import com.steelworks.service.LotLookupService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for LotLookupController. Verifies correct delegation to services and HTTP response
 * structure.
 */
@ExtendWith(MockitoExtension.class)
class LotLookupControllerTest {

    @Mock
    private LotLookupService lotLookupService;

    @Mock
    private DataIntegrityService dataIntegrityService;

    @InjectMocks
    private LotLookupController lotLookupController;

    @Test
    void searchLots_shouldDelegateToLotLookupService() {
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 5);
        LotSearchResult lotSearchResult = new LotSearchResult();
        lotSearchResult.setLotIdentifier("LOT-100");
        when(lotLookupService.searchLots(any(LotSearchRequest.class)))
                .thenReturn(List.of(lotSearchResult));

        ResponseEntity<List<LotSearchResult>> response = lotLookupController.searchLots("lot100",
                startDate, endDate);
        ArgumentCaptor<LotSearchRequest> requestCaptor = ArgumentCaptor
                .forClass(LotSearchRequest.class);
        verify(lotLookupService).searchLots(requestCaptor.capture());
        LotSearchRequest capturedRequest = requestCaptor.getValue();

        boolean isRequestMappedAndReturned = response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null && response.getBody().size() == 1
                && "LOT-100".equals(response.getBody().get(0).getLotIdentifier())
                && "lot100".equals(capturedRequest.getLotId())
                && startDate.equals(capturedRequest.getStartDate())
                && endDate.equals(capturedRequest.getEndDate());
        assertTrue(isRequestMappedAndReturned,
                "Expected controller to map query params into LotSearchRequest and return service data");
    }

    @Test
    void getConsolidatedView_shouldDelegateToLotLookupService() {
        ConsolidatedLotView consolidatedLotView = new ConsolidatedLotView();
        consolidatedLotView.setLotIdentifier("LOT-900");
        when(lotLookupService.getConsolidatedView(9L)).thenReturn(consolidatedLotView);

        ResponseEntity<ConsolidatedLotView> response = lotLookupController.getConsolidatedView(9L);
        verify(lotLookupService).getConsolidatedView(9L);

        boolean isDelegatedAndReturned = response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null
                && "LOT-900".equals(response.getBody().getLotIdentifier());
        assertTrue(isDelegatedAndReturned,
                "Expected controller to delegate consolidated lookup and return response body");
    }

    @Test
    void getOrphanedRecords_shouldDelegateToLotLookupService() {
        OrphanedRecordDTO orphanedRecord = new OrphanedRecordDTO();
        orphanedRecord.setLotIdentifier("LOT-404");
        orphanedRecord.setReason("Missing in shipping.");
        when(lotLookupService.findOrphanedRecords()).thenReturn(List.of(orphanedRecord));

        ResponseEntity<List<OrphanedRecordDTO>> response = lotLookupController.getOrphanedRecords();
        verify(lotLookupService).findOrphanedRecords();

        boolean isDelegatedAndReturned = response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null && response.getBody().size() == 1
                && "LOT-404".equals(response.getBody().get(0).getLotIdentifier());
        assertTrue(isDelegatedAndReturned,
                "Expected orphaned endpoint to delegate and return one orphaned record");
    }

    @Test
    void getDataConflicts_shouldDelegateToDataIntegrityService() {
        DataConflictDTO dataConflict = new DataConflictDTO();
        dataConflict.setLotIdentifier("LOT-777");
        when(dataIntegrityService.detectDataConflicts()).thenReturn(List.of(dataConflict));

        ResponseEntity<List<DataConflictDTO>> response = lotLookupController.getDataConflicts();
        verify(dataIntegrityService).detectDataConflicts();

        boolean isDelegatedAndReturned = response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null && response.getBody().size() == 1
                && "LOT-777".equals(response.getBody().get(0).getLotIdentifier());
        assertTrue(isDelegatedAndReturned,
                "Expected data conflict endpoint to delegate and return one conflict record");
    }
}
