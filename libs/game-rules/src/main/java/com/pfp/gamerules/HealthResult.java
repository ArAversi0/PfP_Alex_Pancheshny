package com.pfp.gamerules;

import java.math.BigDecimal;

public record HealthResult(BigDecimal globalHealthPercent, boolean dead) {
}

