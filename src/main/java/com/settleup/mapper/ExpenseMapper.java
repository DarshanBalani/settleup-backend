package com.settleup.mapper;

import com.settleup.dto.expense.ExpenseDto;
import com.settleup.dto.expense.ExpenseSplitDto;
import com.settleup.entity.ExpenseEntity;
import com.settleup.entity.ExpenseSplitEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExpenseMapper {

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "splits", ignore = true)
    ExpenseDto toDto(ExpenseEntity expense);

    ExpenseSplitDto toSplitDto(ExpenseSplitEntity split);

    List<ExpenseSplitDto> toSplitDtoList(List<ExpenseSplitEntity> splits);
}
