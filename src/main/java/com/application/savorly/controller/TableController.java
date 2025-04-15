package com.application.savorly.controller;

import com.application.savorly.dto.create.TableCreationDto;
import com.application.savorly.dto.response.TableResponseDto;
import com.application.savorly.dto.search.TableSearchDto;
import com.application.savorly.facade.TableFacade;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/restaurants/tables")
public class TableController {

    private final TableFacade tableFacade;

    public TableController(TableFacade tableFacade) {
        this.tableFacade = tableFacade;
    }

    @PostMapping
    public TableResponseDto addTable(
            @RequestBody TableCreationDto tableCreationDto
            ) {
        return tableFacade.addTable(tableCreationDto);
    }

    @GetMapping
    public List<TableResponseDto> getTables(
            @ParameterObject TableSearchDto tableSearchDto
    ) {
        return tableFacade.getTables(tableSearchDto);
    }

    @PatchMapping("/occupy/{tableId}")
    public void occupyTable(
            @PathVariable Long tableId
    ) {
        tableFacade.occupyTable(tableId);
    }

    @PatchMapping("/complete/{tableId}")
    public void completeTableService(
            @PathVariable Long tableId
    ) {
        tableFacade.completeTableService(tableId);
    }

    @DeleteMapping("/{restaurantId}")
    public void removeTable(
            @PathVariable Long restaurantId
    ) {
        tableFacade.removeTable(restaurantId);
    }
}
