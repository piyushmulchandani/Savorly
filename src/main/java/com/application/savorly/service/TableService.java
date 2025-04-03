package com.application.savorly.service;

import com.application.savorly.repository.TableRepository;
import org.springframework.stereotype.Service;

@Service
public class TableService {

    private TableRepository tableRepository;

    public TableService(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }
}
