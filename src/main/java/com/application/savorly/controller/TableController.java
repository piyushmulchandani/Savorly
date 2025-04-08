package com.application.savorly.controller;

import com.application.savorly.dto.create.TableCreationDto;
import com.application.savorly.dto.response.TableResponseDto;
import com.application.savorly.facade.TableFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/tables")
public class TableController {

    private final TableFacade tableFacade;

    public TableController(TableFacade tableFacade) {
        this.tableFacade = tableFacade;
    }

    @PostMapping("/{restaurantId}")
    public TableResponseDto addTable(
            @PathVariable Long restaurantId,
            @RequestBody TableCreationDto tableCreationDto
            ) {
        return tableFacade.addTable(restaurantId, tableCreationDto);
    }
}
