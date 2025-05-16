package com.application.savorly.mapper;

import com.application.savorly.domain.entity.Table;
import com.application.savorly.dto.response.TableResponseDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TableMapper {
    TableResponseDto tableToTableResponseDto(Table table);

    List<TableResponseDto> tablesToTableResponseDtoList(List<Table> tables);
}
