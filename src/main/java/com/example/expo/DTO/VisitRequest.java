package com.example.expo.DTO;
import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.Size; import lombok.Getter; import lombok.Setter;
@Getter @Setter public class VisitRequest { @NotBlank @Size(max=100) private String placeCode; @NotBlank @Size(max=150) private String placeName; private Double latitude; private Double longitude; }
