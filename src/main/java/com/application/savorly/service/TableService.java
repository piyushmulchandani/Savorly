package com.application.savorly.service;

import com.application.savorly.config.exceptions.NotFoundException;
import com.application.savorly.domain.entity.QTable;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.domain.entity.Table;
import com.application.savorly.dto.create.TableCreationDto;
import com.application.savorly.dto.search.TableSearchDto;
import com.application.savorly.repository.TableRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
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
                .build();
        restaurant.addTable(table);

        return tableRepository.save(table);
    }

    public List<Table> getTablesFiltered(TableSearchDto tableSearchDto) {
        Predicate predicate = getWhere(tableSearchDto);

        return (List<Table>) tableRepository.findAll(predicate);
    }

    public void occupyTable(Table table) {
        table.setOccupied(true);
        tableRepository.save(table);
    }

    public void completeTableService(Table table) {
        table.setOccupied(false);
        table.setCurrentCost(BigDecimal.ZERO);
        table.getOrders().clear();

        tableRepository.save(table);
    }

    public void removeTable(Long restaurantId) {
        Integer maxTableNumber = tableRepository.findMaxTableNumberByRestaurantId(restaurantId);
        if (maxTableNumber != null) {
            Optional<Table> tableToDeleteOpt = tableRepository.findByTableNumberAndRestaurant_Id(maxTableNumber, restaurantId);
            if (tableToDeleteOpt.isPresent()) {
                Table tableToDelete = tableToDeleteOpt.get();

                Restaurant restaurant = tableToDelete.getRestaurant();
                restaurant.getTables().remove(tableToDelete);

                tableRepository.delete(tableToDelete);
            }
        }
    }

    public void addCost(Table table, BigDecimal orderCost) {
        table.setCurrentCost(table.getCurrentCost().add(orderCost));
        tableRepository.save(table);
    }

    private Predicate getWhere(TableSearchDto tableSearchDto){
        BooleanBuilder where = new BooleanBuilder();

        where.and(QTable.table.restaurant.id.eq(tableSearchDto.getRestaurantId()));

        if(tableSearchDto.getOccupied() != null) {
            where.and(QTable.table.occupied.eq(tableSearchDto.getOccupied()));
        }
        if(tableSearchDto.getTableNumber() != null) {
            where.and(QTable.table.tableNumber.eq(tableSearchDto.getTableNumber()));
        }
        if(tableSearchDto.getNumPeople() != null) {
            int numPeople = tableSearchDto.getNumPeople();
            where.and(QTable.table.minPeople.loe(numPeople).and(QTable.table.maxPeople.goe(numPeople)));
        }

        return where;
    }
}
