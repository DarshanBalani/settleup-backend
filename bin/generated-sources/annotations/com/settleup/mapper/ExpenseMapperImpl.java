package com.settleup.mapper;

import com.settleup.dto.expense.ExpenseDto;
import com.settleup.dto.expense.ExpenseSplitDto;
import com.settleup.entity.ExpenseEntity;
import com.settleup.entity.ExpenseSplitEntity;
import com.settleup.entity.GroupEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-25T17:09:30+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ExpenseMapperImpl implements ExpenseMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ExpenseDto toDto(ExpenseEntity expense) {
        if ( expense == null ) {
            return null;
        }

        ExpenseDto.ExpenseDtoBuilder expenseDto = ExpenseDto.builder();

        expenseDto.groupId( expenseGroupId( expense ) );
        expenseDto.category( expense.getCategory() );
        expenseDto.createdAt( expense.getCreatedAt() );
        expenseDto.createdBy( userMapper.toDto( expense.getCreatedBy() ) );
        expenseDto.date( expense.getDate() );
        expenseDto.description( expense.getDescription() );
        expenseDto.id( expense.getId() );
        expenseDto.isDeleted( expense.getIsDeleted() );
        expenseDto.notes( expense.getNotes() );
        expenseDto.paidBy( userMapper.toDto( expense.getPaidBy() ) );
        expenseDto.totalAmount( expense.getTotalAmount() );
        expenseDto.updatedAt( expense.getUpdatedAt() );

        return expenseDto.build();
    }

    @Override
    public ExpenseSplitDto toSplitDto(ExpenseSplitEntity split) {
        if ( split == null ) {
            return null;
        }

        ExpenseSplitDto.ExpenseSplitDtoBuilder expenseSplitDto = ExpenseSplitDto.builder();

        expenseSplitDto.amountOwed( split.getAmountOwed() );
        expenseSplitDto.id( split.getId() );
        expenseSplitDto.splitType( split.getSplitType() );
        expenseSplitDto.splitValue( split.getSplitValue() );
        expenseSplitDto.user( userMapper.toDto( split.getUser() ) );

        return expenseSplitDto.build();
    }

    @Override
    public List<ExpenseSplitDto> toSplitDtoList(List<ExpenseSplitEntity> splits) {
        if ( splits == null ) {
            return null;
        }

        List<ExpenseSplitDto> list = new ArrayList<ExpenseSplitDto>( splits.size() );
        for ( ExpenseSplitEntity expenseSplitEntity : splits ) {
            list.add( toSplitDto( expenseSplitEntity ) );
        }

        return list;
    }

    private Long expenseGroupId(ExpenseEntity expenseEntity) {
        if ( expenseEntity == null ) {
            return null;
        }
        GroupEntity group = expenseEntity.getGroup();
        if ( group == null ) {
            return null;
        }
        Long id = group.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
