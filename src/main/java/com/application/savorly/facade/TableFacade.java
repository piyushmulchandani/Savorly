package com.application.savorly.facade;

import com.application.savorly.config.interfaces.hasRestaurantAdminRole;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.dto.create.TableCreationDto;
import com.application.savorly.dto.response.TableResponseDto;
import com.application.savorly.mapper.TableMapper;
import com.application.savorly.service.RestaurantService;
import com.application.savorly.service.TableService;
import org.springframework.stereotype.Component;

@Component
public class TableFacade {

    private final TableService tableService;
    private final RestaurantService restaurantService;
    private final TableMapper tableMapper;

    public TableFacade(TableService tableService, RestaurantService restaurantService, TableMapper tableMapper) {
        this.tableService = tableService;
        this.restaurantService = restaurantService;
        this.tableMapper = tableMapper;
    }

    @hasRestaurantAdminRole
    public TableResponseDto addTable(Long restaurantId, TableCreationDto tableCreationDto) {
        Restaurant restaurant = restaurantService.getRestaurant(restaurantId);

        return tableMapper.tableToTableResponseDto(tableService.createTable(restaurant, tableCreationDto));
    }
}
