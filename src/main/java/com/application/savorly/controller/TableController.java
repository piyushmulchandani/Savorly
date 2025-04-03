package com.application.savorly.controller;

import com.application.savorly.facade.TableFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/tables")
public class TableController {

    private TableFacade tableFacade;

    public TableController(TableFacade tableFacade) {
        this.tableFacade = tableFacade;
    }
}
