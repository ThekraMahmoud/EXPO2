
package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "user_health_card")
public class UserHealthCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @Column(name = "public_token", unique = true, length = 64)
    private String publicToken;

    // Basic information
    @Pattern(regexp = "^(A|B|AB|O)[+-]$")
    @Column(length = 3)
    private String bloodType;

    private LocalDate dateOfBirth;

    @Size(max = 30)
    @Column(length = 30)
    private String biologicalSex;

    @Size(max = 10)
    @Column(length = 10)
    private String preferredLanguage;

    @Size(max = 2)
    @Column(length = 2)
    private String countryCode = "SA";

    // Allergies - existing fields preserved
    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String foodAllergies;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String medicationAllergies;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String otherSensitivities;

    // Multiple allergy selections and custom values
    @ElementCollection
    @CollectionTable(
            name = "health_card_allergy_items",
            joinColumns = @JoinColumn(name = "health_card_id")
    )
    @Column(name = "allergy", nullable = false, length = 200)
    private Set<@Size(max = 200) String> allergyItems = new LinkedHashSet<>();

    // Medical history - existing fields preserved
    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String chronicDiseases;

    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String previousDiseases;

    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String surgeries;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String hospitalizations;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String familyMedicalHistory;

    // Multiple chronic disease selections and custom values
    @ElementCollection
    @CollectionTable(
            name = "health_card_chronic_diseases",
            joinColumns = @JoinColumn(name = "health_card_id")
    )
    @Column(name = "disease", nullable = false, length = 200)
    private Set<@Size(max = 200) String> chronicDiseaseItems = new LinkedHashSet<>();

    // Medications and devices
    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String currentMedications;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String medicationDosages;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String medicalDevicesAndImplants;

    // Phobias - existing field preserved
    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String phobias;

    // Multiple phobia selections and custom values
    @ElementCollection
    @CollectionTable(
            name = "health_card_phobias",
            joinColumns = @JoinColumn(name = "health_card_id")
    )
    @Column(name = "phobia", nullable = false, length = 200)
    private Set<@Size(max = 200) String> phobiaItems = new LinkedHashSet<>();

    // Special needs
    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String disabilities;

    @Size(max = 1500)
    @Column(columnDefinition = "TEXT")
    private String communicationNeeds;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String dietaryRestrictions;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String mobilityNeeds;

    // Emergency and physician information
    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String emergencyInstructions;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String treatingPhysician;

    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String additionalMedicalNotes;

    // Emergency contact
    @Size(max = 100)
    @Column(length = 100)
    private String emergencyContactName;

    @Pattern(
            regexp = "^(05\\d{8}|9665\\d{8})$",
            message = "Enter a valid Saudi mobile number"
    )
    @Column(length = 12)
    private String emergencyContactPhone;

    @Size(max = 50)
    @Column(length = 50)
    private String emergencyContactRelationship;

    // نحافظ على notes القديمة دون إسقاط بياناتها أو تغيير مسار PUT الحالي.
    @Size(max = 1000)
    @Column(length = 1000)
    private String notes;

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // User relationship
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;
}
