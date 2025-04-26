package com.application.savorly.facade;

import com.application.savorly.config.interfaces.hasRestaurantAdminRole;
import com.application.savorly.config.interfaces.hasRestaurantRole;
import com.application.savorly.domain.entity.Order;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.domain.entity.Table;
import com.application.savorly.dto.create.TableCreationDto;
import com.application.savorly.dto.response.TableResponseDto;
import com.application.savorly.dto.search.TableSearchDto;
import com.application.savorly.mapper.TableMapper;
import com.application.savorly.service.OrderService;
import com.application.savorly.service.RestaurantService;
import com.application.savorly.service.TableService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class TableFacade {

    private final TableService tableService;
    private final RestaurantService restaurantService;
    private final TableMapper tableMapper;
    private final OrderService orderService;
    private final RestaurantFacade restaurantFacade;

    public TableFacade(TableService tableService, RestaurantService restaurantService, TableMapper tableMapper, OrderService orderService, RestaurantFacade restaurantFacade) {
        this.tableService = tableService;
        this.restaurantService = restaurantService;
        this.tableMapper = tableMapper;
        this.orderService = orderService;
        this.restaurantFacade = restaurantFacade;
    }

    @hasRestaurantAdminRole
    public TableResponseDto addTable(TableCreationDto tableCreationDto) {
        Restaurant restaurant = restaurantService.getRestaurant(tableCreationDto.getRestaurantId());
        restaurantFacade.checkRestaurantPermission(restaurant.getId());

        return tableMapper.tableToTableResponseDto(tableService.createTable(restaurant, tableCreationDto));
    }

    @hasRestaurantRole
    public List<TableResponseDto> getTables(TableSearchDto tableSearchDto) {
        return tableMapper.tablesToTableResponseDtoList(tableService.getTablesFiltered(tableSearchDto));
    }

    @hasRestaurantRole
    public void occupyTable(Long tableId) {
        Table table = tableService.findById(tableId);
        restaurantFacade.checkRestaurantPermission(table.getRestaurant().getId());

        tableService.occupyTable(table);
    }

    @Transactional
    @hasRestaurantRole
    public void completeTableService(Long tableId) {
        Table table = tableService.findById(tableId);
        restaurantFacade.checkRestaurantPermission(table.getRestaurant().getId());

        List<Order> toDelete = new ArrayList<>(table.getOrders());

        tableService.completeTableService(table);
        toDelete.forEach(orderService::cancelOrder);
    }

    @Transactional
    @hasRestaurantRole
    public void removeTable(Long restaurantId) {
        tableService.removeTable(restaurantId);
        restaurantFacade.checkRestaurantPermission(restaurantId);
    }
}
