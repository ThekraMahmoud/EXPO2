package com.example.expo.DTO;
import jakarta.validation.constraints.DecimalMin; import jakarta.validation.constraints.NotNull; import java.math.BigDecimal; import lombok.Getter; import lombok.Setter;
@Getter @Setter public class TopUpRequest { @NotNull @DecimalMin("0.01") private BigDecimal amount; }
