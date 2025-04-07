package com.application.savorly.service;

import com.application.savorly.config.exceptions.NotFoundException;
import com.application.savorly.domain.entity.Table;
import com.application.savorly.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TableService {

    private final TableRepository tableRepository;

    public TableService(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    public Table findById(Long tableId) {
        return tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Table not found with id: " + tableId));
    }

    public void addCost(Table table, BigDecimal orderCost) {
        table.setCurrentCost(table.getCurrentCost().add(orderCost));
        tableRepository.save(table);
    }
}
