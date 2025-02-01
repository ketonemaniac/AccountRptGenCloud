package net.ketone.accrptgen.common.repo;

import net.ketone.accrptgen.common.model.AccountJob;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountJobRepository extends MongoRepository<AccountJob, UUID> {

    List<AccountJob> findTop10BySubmittedByAndDocTypeOrderByGenerationTimeDesc(
            final String submittedBy, final String docType);

    AccountJob getByClientRandInt(Integer clientRandInt);
}
