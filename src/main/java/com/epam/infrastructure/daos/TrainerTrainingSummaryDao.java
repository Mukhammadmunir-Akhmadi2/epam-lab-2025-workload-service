package com.epam.infrastructure.daos;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document(collection = "trainer_training_summary")
@CompoundIndex(
        name = "name_index",
        def = "{'firstName': 1, 'lastName': 1}"
)
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class TrainerTrainingSummaryDao {
    @Id
    private String trainerId;
    @Indexed(unique=true)
    private String username;

    private String firstName;
    private String lastName;

    private Boolean status;

    private Boolean active;

    private Set<TrainingYearSummaryDao> years = new HashSet<>();
}
