package com.project.WebAloTra.repository.Specification;

import org.springframework.data.jpa.domain.Specification;

import com.project.WebAloTra.dto.Bill.SearchBillDto;
import com.project.WebAloTra.entity.Bill;
import com.project.WebAloTra.entity.Customer;
import com.project.WebAloTra.entity.enumClass.BillStatus;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BillSpecification implements Specification<Bill> {

    private final SearchBillDto searchBillDto;

    public BillSpecification(SearchBillDto searchBillDto) {
        this.searchBillDto = searchBillDto;
    }

    @Override
    public Predicate toPredicate(Root<Bill> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // ====================== 1️⃣ TÌM KIẾM THEO TỪ KHÓA ======================
        if (searchBillDto.getKeyword() != null && !searchBillDto.getKeyword().isEmpty()) {
            String keyword = "%" + searchBillDto.getKeyword().trim() + "%";

            // Join sang bảng Customer để tìm theo tên hoặc SĐT
            Join<Bill, Customer> customerJoin = root.join("customer", JoinType.LEFT);

            Predicate byBillCode = cb.like(root.get("code"), keyword);
            Predicate byCustomerName = cb.like(customerJoin.get("name"), keyword);
            Predicate byPhone = cb.like(customerJoin.get("phoneNumber"), keyword);

            predicates.add(cb.or(byBillCode, byCustomerName, byPhone));
        }

        // ====================== 2️⃣ LỌC THEO TRẠNG THÁI ======================
        if (searchBillDto.getBillStatus() != null) {
            predicates.add(cb.equal(root.get("status"), searchBillDto.getBillStatus()));
        }

        // ====================== 3️⃣ LỌC THEO KHOẢNG NGÀY ======================
        LocalDateTime fromDate = searchBillDto.getFromDate();
        LocalDateTime toDate = searchBillDto.getToDate();

        if (fromDate != null && toDate != null) {
            predicates.add(cb.between(root.get("createDate"), fromDate.with(LocalTime.MIN), toDate.with(LocalTime.MAX)));
        } else {
            // Nếu không truyền, mặc định lấy 7 ngày gần nhất
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7).with(LocalTime.MIN);
            LocalDateTime now = LocalDateTime.now().with(LocalTime.MAX);
            predicates.add(cb.between(root.get("createDate"), sevenDaysAgo, now));
        }

        // ====================== 4️⃣ TRẢ VỀ KẾT QUẢ ======================
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
