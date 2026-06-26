package com.project.WebAloTra.repository.Specification;

import org.springframework.data.jpa.domain.Specification;

import com.project.WebAloTra.dto.BillReturn.SearchBillReturnDto;
import com.project.WebAloTra.entity.BillReturn;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BillReturnSpecification implements Specification<BillReturn> {
    private SearchBillReturnDto searchBillReturnDto;

    public BillReturnSpecification(SearchBillReturnDto searchBillReturnDto) {
        this.searchBillReturnDto = searchBillReturnDto;
    }

    @Override
    public Predicate toPredicate(Root<BillReturn> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        if(searchBillReturnDto.getFromDate() != null && !searchBillReturnDto.getFromDate().isEmpty()) {

            // Parse the string into a LocalDateTime object
            LocalDateTime localDateTime = LocalDateTime.parse(searchBillReturnDto.getFromDate() + "T00:00:00");
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("returnDate"), localDateTime));
        }

        if(searchBillReturnDto.getToDate() != null && !searchBillReturnDto.getToDate().isEmpty()) {
            // Parse the string into a LocalDateTime object
            LocalDateTime localDateTime = LocalDateTime.parse(searchBillReturnDto.getToDate() + "T00:00:00");
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("returnDate"), localDateTime));

        }

        if(searchBillReturnDto.getReturnStatus() != "" && searchBillReturnDto.getReturnStatus() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("returnStatus"), searchBillReturnDto.getReturnStatus()));
        }


        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
