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
import com.application.savorly.service.ReceiptService;
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
    private final ReceiptService receiptService;

    public TableFacade(TableService tableService, RestaurantService restaurantService, TableMapper tableMapper, OrderService orderService, RestaurantFacade restaurantFacade, ReceiptService receiptService) {
        this.tableService = tableService;
        this.restaurantService = restaurantService;
        this.tableMapper = tableMapper;
        this.orderService = orderService;
        this.restaurantFacade = restaurantFacade;
        this.receiptService = receiptService;
    }

    @hasRestaurantAdminRole
    public TableResponseDto addTable(TableCreationDto tableCreationDto) {
        Restaurant restaurant = restaurantService.getRestaurant(tableCreationDto.getRestaurantId());
        restaurantFacade.checkRestaurantPermission(restaurant.getId());

        return tableMapper.tableToTableResponseDto(tableService.createTable(restaurant, tableCreationDto));
    }

    @hasRestaurantRole
    public TableResponseDto getTable(Long tableId) {
        Table table = tableService.findById(tableId);
        restaurantFacade.checkRestaurantPermission(table.getRestaurant().getId());

        return tableMapper.tableToTableResponseDto(table);
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
    public byte[] completeTableService(Long tableId) {
        Table table = tableService.findById(tableId);
        byte[] receipt = receiptService.generateReceipt(table);

        restaurantFacade.checkRestaurantPermission(table.getRestaurant().getId());

        List<Order> toDelete = new ArrayList<>(table.getOrders());

        tableService.completeTableService(table);
        toDelete.forEach(orderService::cancelOrder);

        return receipt;
    }

    @Transactional
    @hasRestaurantRole
    public void removeTable(Long restaurantId) {
        tableService.removeTable(restaurantId);
        restaurantFacade.checkRestaurantPermission(restaurantId);
    }
}
