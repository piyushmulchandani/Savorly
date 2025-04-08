package com.application.savorly.service;

import com.application.savorly.config.exceptions.NotFoundException;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.domain.entity.Table;
import com.application.savorly.dto.create.TableCreationDto;
import com.application.savorly.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

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

    public Table createTable(Restaurant restaurant, TableCreationDto tableCreationDto) {
        int nextTableNumber = Optional.ofNullable(
                tableRepository.findMaxTableNumberByRestaurantId(restaurant.getId()))
                .map(n -> n + 1).orElse(0);

        Table table = Table.builder()
                .tableNumber(nextTableNumber)
                .minPeople(tableCreationDto.getMinPeople())
                .maxPeople(tableCreationDto.getMaxPeople())
                .restaurant(restaurant)
                .build();

        return tableRepository.save(table);
    }

    public void addCost(Table table, BigDecimal orderCost) {
        table.setCurrentCost(table.getCurrentCost().add(orderCost));
        tableRepository.save(table);
    }
}
