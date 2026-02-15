package com.epam.infrastructure.daos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "trainer_monthly_workload",
        uniqueConstraints = @UniqueConstraint(columnNames = {"trainer_id", "year_val", "month_val"}))
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class TrainerMonthlyWorkloadDao {
    @Id
    @UuidGenerator
    private UUID tmwId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerSummaryDao trainer;

    @Column(name = "year_val")
    private int year;

    @Column(name = "month_val")
    private int month;

    @Column(name = "total_duration")
    private int totalDuration;
}
