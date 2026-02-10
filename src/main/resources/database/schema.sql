CREATE TABLE IF NOT EXISTS trainer_summary (
    trainer_id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    status VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_trainer_summary_username
    ON trainer_summary (username);

CREATE TABLE IF NOT EXISTS trainer_monthly_workload (
    tmw_id UUID PRIMARY KEY,
    trainer_id UUID NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    total_duration INT NOT NULL,
    CONSTRAINT fk_trainer_monthly_workload_trainer
    FOREIGN KEY (trainer_id)
    REFERENCES trainer_summary (trainer_id)
    ON DELETE CASCADE,
    CONSTRAINT uq_trainer_monthly_workload
    UNIQUE (trainer_id, year, month)
    );

CREATE INDEX IF NOT EXISTS idx_trainer_monthly_workload_trainer
    ON trainer_monthly_workload (trainer_id);

CREATE INDEX IF NOT EXISTS idx_trainer_monthly_workload_year_month
    ON trainer_monthly_workload (year, month);