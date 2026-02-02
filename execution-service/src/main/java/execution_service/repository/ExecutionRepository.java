package execution_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import execution_service.entity.ExecutionEntity;

import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<ExecutionEntity, UUID> {
}