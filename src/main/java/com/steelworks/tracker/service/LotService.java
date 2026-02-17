package com.steelworks.tracker.service;

import com.steelworks.tracker.dto.LotDetailDTO;
import com.steelworks.tracker.model.Lot;
import com.steelworks.tracker.model.ProductionLog;
import com.steelworks.tracker.model.ShippingLog;
import com.steelworks.tracker.repository.LotRepository;
import com.steelworks.tracker.repository.ProductionLogRepository;
import com.steelworks.tracker.repository.ShippingLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * LotService — Service layer for lot lookup, creation, and status derivation.
 *
 * <p>This class sits between the Controller (web layer) and the Repository
 * (data layer) in our Layered Architecture.  It contains all business
 * logic related to lots, including:</p>
 * <ul>
 *   <li>Fuzzy lot lookup (AC2) via {@link FuzzyMatchService}.</li>
 *   <li>Shipping status derivation: "Shipped" vs "In Inventory" (AC3).</li>
 *   <li>Line attribution for defects (AC4).</li>
 *   <li>Source transparency metadata (AC9).</li>
 * </ul>
 *
 * <h3>Why {@code @Transactional(readOnly = true)}?</h3>
 * <p>Read-only transactions hint to Hibernate that it doesn't need to track
 * dirty state, which reduces memory overhead and speeds up queries.</p>
 */
@Service
@Transactional(readOnly = true) // Default: all public methods are read-only transactions.
public class LotService {

    // ── Injected dependencies (constructor injection — preferred over @Autowired) ──
    private final LotRepository lotRepository;
    private final ProductionLogRepository productionLogRepository;
    private final ShippingLogRepository shippingLogRepository;
    private final FuzzyMatchService fuzzyMatchService;

    /**
     * Constructor injection: Spring automatically wires the repository beans.
     * No {@code @Autowired} annotation needed when there's only one constructor.
     */
    public LotService(LotRepository lotRepository,
                      ProductionLogRepository productionLogRepository,
                      ShippingLogRepository shippingLogRepository,
                      FuzzyMatchService fuzzyMatchService) {
        this.lotRepository = lotRepository;
        this.productionLogRepository = productionLogRepository;
        this.shippingLogRepository = shippingLogRepository;
        this.fuzzyMatchService = fuzzyMatchService;
    }

    // ========================================================================
    // Public API
    // ========================================================================

    /**
     * Look up a lot using fuzzy matching (AC2), then return its full detail DTO.
     *
     * <p><b>Algorithm:</b></p>
     * <ol>
     *   <li>Normalise the search term via {@link FuzzyMatchService#normalize(String)}.</li>
     *   <li>Query the {@code normalized_lot_id} column for an exact match.</li>
     *   <li>If found, derive shipping status (AC3), line attribution (AC4),
     *       defect summary, and source info (AC9).</li>
     * </ol>
     *
     * @param rawLotId the lot identifier as entered by the user (may contain formatting quirks)
     * @return Optional containing the detail DTO, or empty if no match
     *
     * <p><b>Time complexity:</b> O(log n) for the index lookup + O(k) for
     * iterating over related production/shipping logs (k = number of logs).</p>
     */
    public Optional<LotDetailDTO> findByFuzzyId(String rawLotId) {
        // Step 1: Normalise the user's input using the same rules that were
        //         applied when the lot was originally stored.
        String normalizedId = fuzzyMatchService.normalize(rawLotId);

        // Step 2: Look up against the normalised column.
        Optional<Lot> lotOpt = lotRepository.findByNormalizedLotId(normalizedId);

        // Step 3: If found, convert the entity + related data into a DTO.
        return lotOpt.map(this::buildLotDetail);
    }

    /**
     * Find a lot by its exact original identifier.
     *
     * @param lotIdentifier exact string (e.g., "LOT-20260112-001")
     * @return Optional containing the detail DTO
     */
    public Optional<LotDetailDTO> findByExactId(String lotIdentifier) {
        return lotRepository.findByLotIdentifier(lotIdentifier)
                .map(this::buildLotDetail);
    }

    /**
     * Return all lots in a simplified view.
     *
     * @return list of all lot detail DTOs
     */
    public List<LotDetailDTO> findAll() {
        return lotRepository.findAll().stream()
                .map(this::buildLotDetail)
                .collect(Collectors.toList());
    }

    /**
     * Create a new lot with automatic normalisation of the lot identifier (AC2).
     *
     * @param lotIdentifier the raw lot ID string
     * @param partNumber    the part/SKU number
     * @return the persisted Lot entity
     */
    @Transactional  // Override the class-level readOnly=true because this mutates data.
    public Lot createLot(String lotIdentifier, String partNumber) {
        Lot lot = new Lot();
        lot.setLotIdentifier(lotIdentifier);
        // Compute and store the normalised version for future fuzzy lookups.
        lot.setNormalizedLotId(fuzzyMatchService.normalize(lotIdentifier));
        lot.setPartNumber(partNumber);
        lot.setCreatedDate(LocalDate.now());
        return lotRepository.save(lot); // Hibernate issues an INSERT and returns the managed entity.
    }

    // ========================================================================
    // Private helpers
    // ========================================================================

    /**
     * Build a {@link LotDetailDTO} from a {@link Lot} entity by resolving
     * shipping status, production line, defect summary, and source metadata.
     *
     * @param lot the JPA entity (must be attached to a Hibernate session)
     * @return the fully populated DTO
     */
    private LotDetailDTO buildLotDetail(Lot lot) {
        // ── AC3: Derive shipping status ──────────────────────────────────
        // A lot is "Shipped" if there's at least one ShippingLog with status "Shipped".
        String shippingStatus = shippingLogRepository
                .existsByLotIdAndShipStatus(lot.getId(), "Shipped")
                ? "Shipped"
                : "In Inventory";

        // ── AC4: Line attribution ────────────────────────────────────────
        // Get all production logs for this lot and extract distinct line names.
        List<ProductionLog> prodLogs = productionLogRepository.findByLotId(lot.getId());
        List<String> lineNames = prodLogs.stream()
                .map(pl -> pl.getProductionLine().getLineName())
                .distinct()
                .collect(Collectors.toList());

        // If the lot appears on multiple lines, flag it as a conflict (AC11).
        String productionLine;
        if (lineNames.isEmpty()) {
            productionLine = "N/A";              // No production data
        } else if (lineNames.size() == 1) {
            productionLine = lineNames.get(0);   // Normal case
        } else {
            // AC11: Data Conflict — same lot on different lines
            productionLine = "Multiple (Conflict): " + String.join(", ", lineNames);
        }

        // ── Defect summary ───────────────────────────────────────────────
        // Collect all defect names from flagged production logs into a comma string.
        String defectSummary = prodLogs.stream()
                .filter(pl -> Boolean.TRUE.equals(pl.getIssueFlag()) && pl.getDefectType() != null)
                .map(pl -> pl.getDefectType().getDefectName())
                .distinct()
                .collect(Collectors.joining(", "));
        if (defectSummary.isEmpty()) {
            defectSummary = "None";
        }

        // ── AC9: Source transparency ─────────────────────────────────────
        // Use the first production log's source info (or the first shipping log's).
        String sourceFile = null;
        Integer sourceRow = null;
        if (!prodLogs.isEmpty()) {
            sourceFile = prodLogs.get(0).getSourceFile();
            sourceRow = prodLogs.get(0).getSourceRowNumber();
        } else {
            // Fallback: check shipping logs for source metadata.
            List<ShippingLog> shipLogs = shippingLogRepository.findByLotId(lot.getId());
            if (!shipLogs.isEmpty()) {
                sourceFile = shipLogs.get(0).getSourceFile();
                sourceRow = shipLogs.get(0).getSourceRowNumber();
            }
        }

        return new LotDetailDTO(
                lot.getLotIdentifier(),
                lot.getPartNumber(),
                shippingStatus,
                productionLine,
                defectSummary,
                sourceFile,
                sourceRow
        );
    }
}
