
package com.project.WebAloTra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@RestController
public class VulnerableTestController {

    @Autowired
    private EntityManager entityManager;

    /**
     * API NÀY CHỈ DÙNG ĐỂ TEST SQLMAP CHO BÀI LAB.
     * TUYỆT ĐỐI KHÔNG DÙNG TRONG THỰC TẾ!
     */
    @GetMapping("/api/test-sqli")
    public List<Object[]> testSqlInjection(@RequestParam String email) {

        // LỖI NGHIÊM TRỌNG: Nối chuỗi trực tiếp biến 'email' vào câu lệnh SQL
        String sql = "SELECT * FROM account WHERE email = '" + email + "'";

        // In ra console để bạn thấy câu lệnh bị biến đổi như thế nào khi SQLMap quét
        System.out.println("Executing SQL: " + sql);

        // Thực thi SQL thô bạo, bỏ qua mọi cơ chế bảo vệ của Hibernate
        Query query = entityManager.createNativeQuery(sql);

        return query.getResultList();
    }
}
