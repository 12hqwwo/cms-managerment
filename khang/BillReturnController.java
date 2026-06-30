package com.project.WebAloTra.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.WebAloTra.dto.BillReturn.*;
import com.project.WebAloTra.entity.Bill;
import com.project.WebAloTra.repository.BillDetailRepository;
import com.project.WebAloTra.repository.BillRepository;
import com.project.WebAloTra.service.BillReturnService;

import java.util.List;

@Controller
public class BillReturnController {
    private final BillReturnService billReturnService;
    private final BillDetailRepository billDetailRepository;

    public BillReturnController(BillReturnService billReturnService, BillRepository billRepository, BillDetailRepository billDetailRepository) {
        this.billReturnService = billReturnService;
        this.billDetailRepository = billDetailRepository;
    }

    @GetMapping("/admin-only/bill-return")
    public String viewBillReturnPage(SearchBillReturnDto searchBillReturnDto, Model model) {
        List<BillReturnDto> billReturnList = billReturnService.getAllBillReturns(searchBillReturnDto);
        model.addAttribute("returnList", billReturnList);
        return "admin/bill-return";
    }

    @GetMapping("/admin-only/bill-return-create")
    public String viewBillReturnCreatePage(Model model) {

        return "admin/bill-return-create";
    }

    @GetMapping("/admin-only/bill-return-detail/{id}")
    public String viewBillReturnDetailPage(Model model, @PathVariable Long id) {
       BillReturnDetailDto billReturnDetailDto = billReturnService.getBillReturnDetailById(id);

       Double total = Double.valueOf(0);

        for (RefundProductDto refundProductDto:
                billReturnDetailDto.getRefundProductDtos()) {
            total += refundProductDto.getMomentPriceRefund() * refundProductDto.getQuantityRefund();
        }

        Double totalReturn = Double.valueOf(0);

        for (ReturnProductDto returnProductDto:
                billReturnDetailDto.getReturnProductDtos()) {
            totalReturn += returnProductDto.getMomentPriceExchange() * returnProductDto.getQuantityReturn();
        }

       model.addAttribute("total", total);
        model.addAttribute("totalReturn", totalReturn);
       model.addAttribute("billReturnDetail", billReturnDetailDto);

        return "admin/bill-return-detail";
    }

    @GetMapping("/admin-only/bill-return-detail-code/{code}")
    public String viewBillReturnDetailPageByCode(Model model, @PathVariable String code) {
        BillReturnDetailDto billReturnDetailDto = billReturnService.getBillReturnDetailByCode(code);

        Double total = Double.valueOf(0);

        for (RefundProductDto refundProductDto:
                billReturnDetailDto.getRefundProductDtos()) {
            total += refundProductDto.getMomentPriceRefund() * refundProductDto.getQuantityRefund();
        }

        Double totalReturn = Double.valueOf(0);

        for (ReturnProductDto returnProductDto:
                billReturnDetailDto.getReturnProductDtos()) {
            totalReturn += returnProductDto.getMomentPriceExchange() * returnProductDto.getQuantityReturn();
        }

        model.addAttribute("total", total);
        model.addAttribute("totalReturn", totalReturn);
        model.addAttribute("billReturnDetail", billReturnDetailDto);

        return "admin/bill-return-detail";
    }


    @GetMapping("/admin/bill-return-detail-generate/{id}")
    public String generateHtmlPrint(Model model, @PathVariable Long id) {
        BillReturnDetailDto billReturnDetailDto = billReturnService.getBillReturnDetailById(id);

        Double total = Double.valueOf(0);

        for (RefundProductDto refundProductDto:
                billReturnDetailDto.getRefundProductDtos()) {
            total += refundProductDto.getMomentPriceRefund() * refundProductDto.getQuantityRefund();
        }

        Double totalReturn = Double.valueOf(0);

        for (ReturnProductDto returnProductDto:
                billReturnDetailDto.getReturnProductDtos()) {
            totalReturn += returnProductDto.getMomentPriceExchange() * returnProductDto.getQuantityReturn();
        }

        model.addAttribute("total", total);
        model.addAttribute("totalReturn", totalReturn);
        model.addAttribute("billReturnDetail", billReturnDetailDto);

        return "admin/invoice-return-print";
    }

    @PostMapping("/admin/update-bill-return-status")
    public String updateBillReturnStatus(@ModelAttribute("billReturnDto") BillReturnDto billReturnDto, Model model, RedirectAttributes redirectAttributes) {

        try {
            // ===== ÁP DỤNG ORACLE FGA (Fine-Grained Auditing) =====
            // Khi gọi hàm updateStatus(), Hibernate sẽ thực thi lệnh UPDATE trên bảng BILL_RETURN.
            // Oracle FGA đã được cài đặt (7_FGA_BILLRETURN.sql) sẽ tự động kích hoạt
            // bắt lấy thao tác UPDATE trên cột RETURN_STATUS và ghi log vào DBA_FGA_AUDIT_TRAIL.
            // Không cần phải viết thêm log lưu thủ công trong Java.
            BillReturnDto updatedBillReturn = billReturnService.updateStatus(billReturnDto.getId(), billReturnDto.getReturnStatus());
            redirectAttributes.addFlashAttribute("message", "Đơn đổi trả " + updatedBillReturn.getCode() + " cập nhật trạng thái thành công!");
        } catch (Exception e) {
            model.addAttribute("message", "Error updating status");
        }
        return "redirect:/admin-only/bill-return";
    }

    @ResponseBody
    @PostMapping("/api/bill-return")
    public BillReturnDto createBillReturn(@RequestBody BillReturnCreateDto billReturnCreateDto) {
        // ===== ÁP DỤNG ORACLE FGA (Fine-Grained Auditing) =====
        // Khi tạo phiếu trả hàng mới (INSERT vào bảng BILL_RETURN), FGA cũng sẽ bắt
        // lấy sự kiện này nếu cấu hình audit lệnh INSERT. Mọi thao tác thêm/sửa 
        // đều được tự động lưu dấu vết dưới CSDL.
        return billReturnService.createBillReturn(billReturnCreateDto);
    }
}
