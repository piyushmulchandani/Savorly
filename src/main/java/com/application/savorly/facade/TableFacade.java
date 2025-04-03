package com.application.savorly.facade;

import com.application.savorly.mapper.TableMapper;
import com.application.savorly.service.TableService;
import org.springframework.stereotype.Component;

@Component
public class TableFacade {

    private TableService tableService;
    private TableMapper tableMapper;

    public TableFacade(TableService tableService, TableMapper tableMapper) {
        this.tableService = tableService;
        this.tableMapper = tableMapper;
    }
}
