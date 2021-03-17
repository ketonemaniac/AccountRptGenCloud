package net.ketone.accrptgen.repo;

import net.ketone.accrptgen.domain.dto.AccountJob;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountJobRepository extends MongoRepository<AccountJob, UUID> {

    List<AccountJob> findTop10BySubmittedByOrderByGenerationTimeDesc(
            final String submittedBy);
}
