package org.exchange.modules.engine.infrastructure.rest;

import jakarta.validation.Valid;
import org.exchange.modules.core.domain.message.JobBusInterface;
import org.exchange.modules.engine.application.job.DepositJob;
import org.exchange.modules.engine.domain.BalanceManager;
import org.exchange.modules.engine.infrastructure.cache.AssetCache;
import org.exchange.modules.engine.infrastructure.cache.InstrumentCache;
import org.exchange.modules.engine.infrastructure.dto.DepositAssetRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
final public class WalletController {
    private final JobBusInterface jobBus;
    private final String queueUrl;
    private final AssetCache assetCache;
    private final BalanceManager balanceManager;

    public WalletController(
            JobBusInterface jobBus,
            @Value("${app.sqs.queue-deposit-url}") String queueUrl,
            AssetCache assetCache,
            BalanceManager balanceManager
    ) {
        this.jobBus = jobBus;
        this.queueUrl = queueUrl;
        this.assetCache = assetCache;
        this.balanceManager = balanceManager;
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> createDeposit(@Valid @RequestBody DepositAssetRequest depositRequest)
    {
        Long assetId = assetCache.getAssetId(depositRequest.asset());

        if (assetId == null) {
            return ResponseEntity.badRequest().body("Asset not found");
        }

        DepositJob job = new DepositJob(
                depositRequest.userId(),
                assetId,
                depositRequest.amount()
        );
        jobBus.send(queueUrl, job, "orders-group");

        return ResponseEntity.ok().build();
    }

    //TODO: remove only for testing
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {

        balanceManager.getBalances();
        return ResponseEntity.ok().build();
    }
}
