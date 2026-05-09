package com.booknest.service.strategy.lending;


import com.booknest.enums.LendingPolicyType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LendingStrategyFactory {

    private final Map<LendingPolicyType, LendingStrategy> strategyMap;

    public LendingStrategyFactory(
            @Qualifier("freeLendingStrategy") LendingStrategy freeStrategy,
            @Qualifier("depositBasedLendingStrategy") LendingStrategy depositStrategy,
            @Qualifier("subscriptionBasedLendingStrategy") LendingStrategy subscriptionStrategy) {
        this.strategyMap = Map.of(
                LendingPolicyType.FREE, freeStrategy,
                LendingPolicyType.DEPOSIT_BASED, depositStrategy,
                LendingPolicyType.SUBSCRIPTION_BASED, subscriptionStrategy
        );
    }

    public LendingStrategy getStrategy(LendingPolicyType policyType) {
        LendingStrategy strategy = strategyMap.get(policyType);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for policy: " + policyType);
        }
        return strategy;
    }
}