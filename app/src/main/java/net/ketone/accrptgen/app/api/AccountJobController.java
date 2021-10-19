package net.ketone.accrptgen.app.api;

import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.repo.AccountJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job")
public class AccountJobController {

    @Autowired
    private AccountJobRepository repo;

    @GetMapping
    public List<AccountJob> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public AccountJob save(@RequestBody final AccountJob job) {
        return repo.save(job);
    }

}
