package com.application.savorly.controller;

import com.application.savorly.dto.create.TableCreationDto;
import com.application.savorly.dto.response.TableResponseDto;
import com.application.savorly.dto.search.TableSearchDto;
import com.application.savorly.facade.TableFacade;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
            @RequestBody @Valid TableCreationDto tableCreationDto
    ) {
        return tableFacade.addTable(tableCreationDto);
    }

    @GetMapping("/{tableId}")
    public TableResponseDto getTable(
            @PathVariable Long tableId
    ) {
        return tableFacade.getTable(tableId);
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
    public ResponseEntity<byte[]> completeTableService(
            @PathVariable Long tableId
    ) {
        //TODO make this better
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "receipt-table-" + tableId + ".pdf");

        return new ResponseEntity<>(tableFacade.completeTableService(tableId), headers, HttpStatus.OK);
    }

    @DeleteMapping("/{restaurantId}")
    public void removeTable(
            @PathVariable Long restaurantId
    ) {
        tableFacade.removeTable(restaurantId);
    }
}
