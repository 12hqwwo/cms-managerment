package com.project.WebAloTra.controller.admin;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.WebAloTra.dto.Statistic.BestSellerProduct;
import com.project.WebAloTra.repository.BillRepository;
import com.project.WebAloTra.repository.BranchRepository;
import com.project.WebAloTra.service.AccountService;
import com.project.WebAloTra.service.StatisticService;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class StatisticController {

    private final AccountService accountService;
    private final BillRepository billRepository;
    private final StatisticService statisticService;
    private final BranchRepository branchRepository;

    public StatisticController(AccountService accountService, BillRepository billRepository, StatisticService statisticService, BranchRepository branchRepository) {
        this.accountService = accountService;
        this.billRepository = billRepository;
        this.statisticService = statisticService;
        this.branchRepository = branchRepository;
    }

    @GetMapping("/admin/thong-ke-doanh-thu")
    public String viewStatisticRevenuePage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean hasAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        var account = accountService.findByEmail(auth.getName());
        Long branchId = (account != null && account.getBranch() != null)
                ? account.getBranch().getId()
                : null;

        model.addAttribute("branches", branchRepository.findAll());
        model.addAttribute("currentBranchId", branchId);
        model.addAttribute("isAdmin", hasAdmin);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime firstDayOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        LocalDateTime lastDayOfWeek = firstDayOfWeek.plusDays(6).with(LocalTime.MAX);
        LocalDateTime firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        LocalDateTime lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // ✅ Doanh thu tổng
        Double revenueAll = hasAdmin
                ? safeValue(billRepository.calculateTotalRevenue())
                : safeValue(billRepository.calculateTotalRevenueByBranch(branchId));

        Double revenueToday = hasAdmin
                ? safeValue(billRepository.calculateTotalRevenueFromDate(startOfDay.format(formatter), now.format(formatter)))
                : safeValue(billRepository.calculateTotalRevenueFromDateByBranch(startOfDay, now, branchId));

        Double revenueWeek = hasAdmin
                ? safeValue(billRepository.calculateTotalRevenueFromDate(firstDayOfWeek.format(formatter), lastDayOfWeek.format(formatter)))
                : safeValue(billRepository.calculateTotalRevenueFromDateByBranch(firstDayOfWeek, lastDayOfWeek, branchId));

        Double revenueMonth = hasAdmin
                ? safeValue(billRepository.calculateTotalRevenueFromDate(firstDayOfMonth.format(formatter), lastDayOfMonth.format(formatter)))
                : safeValue(billRepository.calculateTotalRevenueFromDateByBranch(firstDayOfMonth, lastDayOfMonth, branchId));

        model.addAttribute("revenueAll", revenueAll);
        model.addAttribute("revenueToday", revenueToday);
        model.addAttribute("revenueWeek", revenueWeek);
        model.addAttribute("revenueMonth", revenueMonth);

        // ✅ Doanh thu quá khứ
        LocalDateTime yesterdayStart = now.minusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime yesterdayEnd = yesterdayStart.plusDays(1).minusSeconds(1);
        LocalDateTime lastWeekStart = now.minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        LocalDateTime lastWeekEnd = lastWeekStart.plusDays(6).with(LocalTime.MAX);
        LocalDateTime lastMonthStart = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        LocalDateTime lastMonthEnd = lastMonthStart.plusMonths(1).minusDays(1).with(LocalTime.MAX);

        Double revenueYesterday = hasAdmin
                ? safeValue(billRepository.calculateTotalRevenueFromDate(yesterdayStart.format(formatter), yesterdayEnd.format(formatter)))
                : safeValue(billRepository.calculateTotalRevenueFromDateByBranch(yesterdayStart, yesterdayEnd, branchId));

        Double revenueLastWeek = hasAdmin
                ? safeValue(billRepository.calculateTotalRevenueFromDate(lastWeekStart.format(formatter), lastWeekEnd.format(formatter)))
                : safeValue(billRepository.calculateTotalRevenueFromDateByBranch(lastWeekStart, lastWeekEnd, branchId));

        Double revenueLastMonth = hasAdmin
                ? safeValue(billRepository.calculateTotalRevenueFromDate(lastMonthStart.format(formatter), lastMonthEnd.format(formatter)))
                : safeValue(billRepository.calculateTotalRevenueFromDateByBranch(lastMonthStart, lastMonthEnd, branchId));

        // ✅ Tính % thay đổi
        model.addAttribute("percentageYesterday", calculatePercentage(revenueYesterday, revenueToday));
        model.addAttribute("percentageLastWeek", calculatePercentage(revenueLastWeek, revenueWeek));
        model.addAttribute("percentageLastMonth", calculatePercentage(revenueLastMonth, revenueMonth));

        // ✅ Top sản phẩm bán chạy
        List<BestSellerProduct> bestSellers = hasAdmin
                ? statisticService.getBestSellerProductAll()
                : statisticService.getBestSellerProductByBranch(branchId);
        model.addAttribute("bestSellers", bestSellers);

        return "/admin/thong-ke-doanh-thu";
    }

    @GetMapping("/admin/thong-ke-san-pham")
    public String viewStatisticProductPage(Model model) {
        return "/admin/thong-ke-san-pham";
    }

    private double calculatePercentage(double baseValue, double comparedValue) {
        if (baseValue == 0 && comparedValue > 0) return 99;
        if (baseValue == 0) return 0;
        return ((comparedValue - baseValue) / baseValue) * 100;
    }

    @GetMapping("/admin/api/thong-ke-doanh-thu")
    @ResponseBody
    public Object getRevenueByBranch(@RequestParam(required = false) Long branchId) {
        try {
            if (branchId == null) {
                return Map.of(
                        "revenueAll", safeValue(billRepository.calculateTotalRevenue()),
                        "revenueToday", safeValue(billRepository.calculateTotalRevenueFromDate(
                                LocalDate.now().atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        )),
                        "revenueWeek", safeValue(billRepository.calculateTotalRevenueFromDate(
                                LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                LocalDate.now().with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        )),
                        "revenueMonth", safeValue(billRepository.calculateTotalRevenueFromDate(
                                LocalDate.now().withDayOfMonth(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        ))
                );
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime firstDayOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
            LocalDateTime lastDayOfWeek = firstDayOfWeek.plusDays(6).with(LocalTime.MAX);
            LocalDateTime firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
            LocalDateTime lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

            Map<String, Object> result = new HashMap<>();
            result.put("revenueAll", safeValue(billRepository.calculateTotalRevenueByBranch(branchId)));
            result.put("revenueToday", safeValue(billRepository.calculateTotalRevenueFromDateByBranch(startOfDay, now, branchId)));
            result.put("revenueWeek", safeValue(billRepository.calculateTotalRevenueFromDateByBranch(firstDayOfWeek, lastDayOfWeek, branchId)));
            result.put("revenueMonth", safeValue(billRepository.calculateTotalRevenueFromDateByBranch(firstDayOfMonth, lastDayOfMonth, branchId)));

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Lỗi khi lấy thống kê: " + (e.getMessage() != null ? e.getMessage() : "Không rõ nguyên nhân"));
            return error;
        }
    }

    private Double safeValue(Double value) {
        return value == null ? 0.0 : value;
    }
}
