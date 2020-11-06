package net.ketone.accrptgen.repo;

import net.ketone.accrptgen.dto.AccountFileDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountFileDtoRepository extends MongoRepository<AccountFileDto, UUID> {

}
