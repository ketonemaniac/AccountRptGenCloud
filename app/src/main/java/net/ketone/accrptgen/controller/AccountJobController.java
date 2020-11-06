package net.ketone.accrptgen.controller;

import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.repo.AccountFileDtoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job")
public class AccountJobController {

    @Autowired
    private AccountFileDtoRepository repo;

    @GetMapping
    public List<AccountFileDto> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public AccountFileDto save(@RequestBody final AccountFileDto job) {
        return repo.save(job);
    }

}
