package com.project.WebAloTra.service.serviceImpl;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.project.WebAloTra.dto.Bill.*;
import com.project.WebAloTra.dto.CustomerDto.CustomerDto;
import com.project.WebAloTra.entity.*;
import com.project.WebAloTra.entity.enumClass.BillStatus;
import com.project.WebAloTra.entity.enumClass.InvoiceType;
import com.project.WebAloTra.exception.NotFoundException;
import com.project.WebAloTra.repository.BillDetailToppingRepository;
import com.project.WebAloTra.repository.BillRepository;
import com.project.WebAloTra.repository.ProductDetailRepository;
import com.project.WebAloTra.repository.ProductRepository;
import com.project.WebAloTra.repository.Specification.BillSpecification;
import com.project.WebAloTra.repository.Specification.ProductSpecification;
import com.project.WebAloTra.service.BillService;
import com.project.WebAloTra.utils.UserLoginUtil;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.servlet.http.HttpServletResponse;
import javax.swing.text.DateFormatter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class BillServiceImpl implements BillService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ProductDetailRepository productDetailRepository;
    @Autowired
    private BillDetailToppingRepository billDetailToppingRepository;
    
    @Override
    public Page<BillDtoInterface> findAll(Pageable pageable) {
        return billRepository.listBill(pageable);
    }
    
    
    @Override
    public List<BillDtoInterface> findAll() {
        return billRepository.listBill();
    }

    @Override
    public Page<BillDtoInterface> searchListBill(String maDinhDanh,
                                                 LocalDateTime ngayTaoStart,
                                                 LocalDateTime ngayTaoEnd,
                                                 String trangThai,
                                                 String loaiDon,
                                                 String soDienThoai,
                                                 String hoVaTen,
                                                 Pageable pageable) {
        BillStatus status = null;
        InvoiceType invoiceType = null;

        try {
            status = BillStatus.valueOf(trangThai);
        } catch (IllegalArgumentException e) {

        }
        try {
            invoiceType = InvoiceType.valueOf(loaiDon);
        } catch (IllegalArgumentException e) {

        }
        return billRepository.listSearchBill( maDinhDanh,
                 ngayTaoStart,
                 ngayTaoEnd,
                status,
                 invoiceType,
                 soDienThoai,
                 hoVaTen,
                pageable);
    }

    @Override
    public List<BillDtoInterface> searchListBill(
            String maDinhDanh,
            LocalDateTime ngayTaoStart,
            LocalDateTime ngayTaoEnd,
            String trangThai,
            String loaiDon,
            String soDienThoai,
            String hoVaTen) {

        BillStatus status = null;
        InvoiceType invoiceType = null;

        // ✅ Parse enum trạng thái
        if (trangThai != null && !trangThai.isBlank()) {
            try {
                status = BillStatus.valueOf(trangThai);
            } catch (IllegalArgumentException ignored) {
                // giá trị không hợp lệ -> để null
            }
        }

        // ✅ Parse enum loại đơn
        if (loaiDon != null && !loaiDon.isBlank()) {
            try {
                invoiceType = InvoiceType.valueOf(loaiDon);
            } catch (IllegalArgumentException ignored) {
                // giá trị không hợp lệ -> để null
            }
        }

        // ✅ Gọi repository KHÔNG PHÂN TRANG
        return billRepository.listSearchBill(
                maDinhDanh,
                ngayTaoStart,
                ngayTaoEnd,
                status,
                invoiceType,
                soDienThoai,
                hoVaTen
        );
    }

    @Override
    public Bill updateStatus(String status, Long id) {

        // Nếu hủy thì cộng lại số lượng tồn
        if(status.equals("HUY")) {
            List<BillDetailProduct> billDetailProducts = billRepository. getbill_detailProduct(id);
            billDetailProducts.forEach(item -> {
            	ProductDetail productDetail = productDetailRepository.findById(item.getProductDetailId())
            		    .orElseThrow(() -> new NotFoundException("Không tìm thấy ProductDetail có id " + item.getProductDetailId()));
                int quantityBefore = productDetail.getQuantity();
                productDetail.setQuantity(quantityBefore + item.getSoLuong());
                productDetailRepository.save(productDetail);
            });
        }

        Bill bill = billRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy bill có mã" + id));
        bill.setStatus(BillStatus.valueOf(status));
        bill.setUpdateDate(LocalDateTime.now());
        return billRepository.save(bill);
    }

    @Override
    public BillDetailDtoInterface getBillDetail(Long maHoaDon) {
        return billRepository.getbill_detail(maHoaDon);
    }

    @Override
    public Page<Bill> getBillByStatus(String status, Pageable pageable) {
        Account account = UserLoginUtil.getCurrentLogin();
        if (account == null || account.getCustomer() == null) {
            throw new RuntimeException("Không tìm thấy khách hàng cho tài khoản hiện tại");
        }
        Long customerId = account.getCustomer().getId();
        return billRepository.getBillByStatus(customerId, status, pageable);
    }


    @Override
    public List<BillDetailProduct> getBillDetailProduct(Long maHoaDon) {
        return billRepository. getbill_detailProduct(maHoaDon);
    }

    public void exportToExcel(HttpServletResponse response, Page<BillDtoInterface> bills, String exportUrl) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=bills.xlsx");

        // Tạo workbook Excel và các sheet, row, cell tương ứng
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Get the current date in the "dd-MM-yyyy" format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String currentDate = dateFormat.format(new Date());

        // Create a sheet with the name "bill_dd-mm-yyyy"
        XSSFSheet sheet = workbook.createSheet("bill_" + currentDate);

        // Tạo tiêu đề cột
        XSSFRow headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Mã Hóa Đơn");
        headerRow.createCell(1).setCellValue("Họ và Tên");
        headerRow.createCell(2).setCellValue("Số điện thoại");
        headerRow.createCell(3).setCellValue("Ngày đặt");
        headerRow.createCell(4).setCellValue("Tổng tiền");
        headerRow.createCell(5).setCellValue("Trạng thái");
        headerRow.createCell(6).setCellValue("Loại đơn");
        headerRow.createCell(7).setCellValue("Hình thực thanh toán");

        int rowNum = 1;
        for (BillDtoInterface bill : bills) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(bill.getMaDinhDanh());
            row.createCell(1).setCellValue(bill.getHoVaTen());
            row.createCell(2).setCellValue(bill.getSoDienThoai());
            XSSFCell dateCell = row.createCell(3);
            XSSFCellStyle dateCellStyle = workbook.createCellStyle();
            XSSFDataFormat dataFormat = workbook.createDataFormat();
            dateCellStyle.setDataFormat(dataFormat.getFormat("dd/MM/yyyy"));
            dateCell.setCellStyle(dateCellStyle);
            dateCell.setCellValue(bill.getNgayTao());
            XSSFCell totalCell = row.createCell(4);
            totalCell.setCellValue(bill.getTongTien());

            XSSFCellStyle totalCellStyle = workbook.createCellStyle();
            XSSFDataFormat dataFormat1 = workbook.createDataFormat();
            totalCellStyle.setDataFormat(dataFormat1.getFormat("#,###"));
            totalCell.setCellStyle(totalCellStyle);
            String trangThaiText = "";
            switch (bill.getTrangThai()) {
                case CHO_XAC_NHAN:
                    trangThaiText = "Chờ xác nhận";
                    break;
                case CHO_LAY_HANG:
                    trangThaiText = "Đang xử lý";
                    break;
                case CHO_GIAO_HANG:
                    trangThaiText = "Chờ giao hàng";
                    break;
                case HOAN_THANH:
                    trangThaiText = "Hoàn thành";
                    break;
                case HUY:
                    trangThaiText = "Hủy";
                    break;
                default:
                    trangThaiText = "  ";
            }
            String loaiDonText = "";
            switch (bill.getLoaiDon()) {
                case OFFLINE:
                    loaiDonText = "Tại quầy";
                    break;
                case ONLINE:
                    loaiDonText = "Trực tuyến";
                    break;
                default:
                    loaiDonText = "  ";
            }
            row.createCell(5).setCellValue(trangThaiText);
            row.createCell(6).setCellValue(loaiDonText);
            row.createCell(7).setCellValue(bill.getHinhThucThanhToan());

            XSSFCell linkCell = row.createCell(8);
            XSSFRichTextString linkText = new XSSFRichTextString(" ");
            CreationHelper createHelper = workbook.getCreationHelper();
            XSSFHyperlink hyperlink = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
            hyperlink.setAddress(exportUrl);
            linkCell.setHyperlink(hyperlink);
            linkCell.setCellValue(linkText);
        }

        try (OutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
            workbook.close();
            outputStream.flush();
        }
    }

    @Override
    public String exportPdf(HttpServletResponse response, Long maHoaDon) throws DocumentException, IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/pdf");

        ITextRenderer renderer = new ITextRenderer();
        renderer.getFontResolver().addFont("/static/fonts/time-new-roman/SVN-Times New Roman.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        String htmlContent = getHtmlContent(maHoaDon);
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        try (OutputStream outputStream = response.getOutputStream()) {
            renderer.createPDF(outputStream, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHtmlContent(Long maHoaDon) {
        BillDetailDtoInterface billDetailDtoInterface = billRepository.getbill_detail(maHoaDon);
        List<BillDetailProduct> billDetailProduct = billRepository.getbill_detailProduct(maHoaDon);
        String email = billDetailDtoInterface.getEmail();
        if (email == null) {
            email = "";
        }
        String address = billDetailDtoInterface.getDiaChi();
        if (address == null) {
            address = "";
        }

        String customerName = billDetailDtoInterface.getTenKhachHang();
        if (customerName == null) {
            customerName = "";
        }

        String customerPhone = billDetailDtoInterface.getSoDienThoai();
        if (customerPhone == null) {
            customerPhone = "";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String htmlContent = "<html xmlns:th=\"http://www.thymeleaf.org\">\n" +
                "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></meta>\n" +
                "    <title>Hóa đơn bán hàng</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: SVN-Times New Roman;\">\n" +
                "<h1 style=\"text-align: center\">HÓA ĐƠN BÁN HÀNG</h1>\n" +
                "<h3 style=\"text-align: center\"> VÕ VĂN NGÂN</h3>\n" +
                "<h3 style=\"text-align: center\">SỐ 1</h3>\n\n" +
                "<h5> Mã hóa đơn: " + billDetailDtoInterface.getMaDinhDanh() + "</h5>\n" +
                "<h5> Họ và tên: " + customerName + "</h5>\n" +
                "<h5> Số điện thoại :" + customerPhone + "</h5>\n" +
                "<h5> Email: " + email + "</h5>\n" +
                "<h5> Địa chỉ:" + address + "</h5>\n" +
                "<h5> Ngày thanh toán: " + billDetailDtoInterface.getCreatedDate().format(formatter) + "</h5>\n" +
                "<h3>Danh sách sản phẩm:</h3>\n" +
                "<table border=\"1\" style=\"border-collapse: collapse;\">\n" +
                "<tr>\n" +
                "<th>Tên sản phẩm</th>\n" +
                "<th>thể tích đường</th>\n" +
                "<th>Size</th>\n" +
                "<th>Giá tiền</th>\n" +
                "<th>Số lượng</th>\n" +
                "<th>Tổng tiền</th>\n" +
                "</tr>\n";
        Double totalMoney = 0.0;
        // ✅ Tính đúng tổng: (đơn giá + tổng topping/1sp) * số lượng
        for (BillDetailProduct item : billDetailProduct) {
            double toppingTotalPerUnit = billDetailToppingRepository.findAllByBillDetail_Id(item.getBillDetailId())
                    .stream()
                    .mapToDouble(BillDetailTopping::getToppingPrice)
                    .sum();
            double unitPriceWithTopping = item.getGiaTien() + toppingTotalPerUnit;
            double lineTotal = unitPriceWithTopping * item.getSoLuong();
            totalMoney += lineTotal;
        }
        Locale locale = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        for (BillDetailProduct item : billDetailProduct) {
            String productName = item.getTenSanPham();
            String color = item.getTenMau();
            String size = item.getKichCo();
            double toppingTotalPerUnit = billDetailToppingRepository.findAllByBillDetail_Id(item.getBillDetailId())
                    .stream()
                    .mapToDouble(BillDetailTopping::getToppingPrice)
                    .sum();
            double unitPriceWithTopping = item.getGiaTien() + toppingTotalPerUnit;
            double lineTotal = unitPriceWithTopping * item.getSoLuong();

            String price = currencyFormatter.format(unitPriceWithTopping);
            String quantity = String.valueOf(item.getSoLuong());
            String total = currencyFormatter.format(lineTotal);

            htmlContent += "<tr>\n" +
                    "<td>" + productName + "</td>\n" +
                    "<td>" + color + "</td>\n" +
                    "<td>" + size + "</td>\n" +
                    "<td>" + price + "</td>\n" +
                    "<td>" + quantity + "</td>\n" +
                    "<td>" + total + "</td>\n" +
                    "</tr>\n";
        }
        htmlContent += "</table>\n" +
                "<h5>Tổng tiền: " + currencyFormatter.format(totalMoney) + "</h5>\n" +
                "<h5>Tiền giảm giá: " + currencyFormatter.format(billDetailDtoInterface.getTienKhuyenMai()) + "</h5>\n" +
                "<h4>Tổng tiền thanh toán: " + currencyFormatter.format(totalMoney - billDetailDtoInterface.getTienKhuyenMai()) + "</h4>\n" +
                "</body>\n" +
                "</html>";
        return htmlContent;
    }

    @Override
    public Page<Bill> getBillByAccount(Pageable pageable) {
        Account account = UserLoginUtil.getCurrentLogin();
        if (account == null || account.getCustomer() == null) {
            throw new RuntimeException("Không tìm thấy tài khoản hoặc khách hàng tương ứng");
        }

        Long customerId = account.getCustomer().getId();
        return billRepository.getBillByAccount(customerId, pageable);
    }
    
    @Override
    public void deleteById(Long id) {
        billRepository.deleteById(id);
    }

    @Override
    public Page<BillDto> searchBillJson(SearchBillDto searchBillDto, Pageable pageable) {
        Specification<Bill> spec = new BillSpecification(searchBillDto);
        Page<Bill> bills = billRepository.findAll(spec, pageable);
        return bills.map(this::convertToDto);
    }

    @Override
    public Page<BillDto> getAllValidBillToReturn(Pageable pageable) {
        return billRepository.findValidBillToReturn(pageable).map(this::convertToDto);
    }

    private BillDto convertToDto(Bill bill) {
        BillDto billDto = new BillDto();
        billDto.setId(bill.getId());
        billDto.setCode(bill.getCode());
        billDto.setCreateDate(bill.getCreateDate());
        billDto.setStatus(bill.getStatus());
        billDto.setUpdateDate(bill.getUpdateDate());
        CustomerDto customer = new CustomerDto();
        if(bill.getCustomer() != null) {
            customer.setName(bill.getCustomer().getName());
            customer.setId(bill.getCustomer().getId());
            customer.setCode(bill.getCustomer().getCode());
            customer.setCode(bill.getCustomer().getCode());
        }
        billDto.setCustomer(customer);
        Double total = 0.0;

        for (BillDetail billDetail : bill.getBillDetail()) {
            double toppingTotal = billDetailToppingRepository.findAllByBillDetail_Id(billDetail.getId())
                    .stream()
                    .mapToDouble(BillDetailTopping::getToppingPrice)
                    .sum();
            total += billDetail.getQuantity() * (billDetail.getMomentPrice() + toppingTotal);
        }

        billDto.setTotalAmount(total);
        return billDto;
    }
    @Override
    public Page<BillDtoInterface> findByBranchId(Long branchId, Pageable pageable) {
        return billRepository.findByBranchId(branchId, pageable);
    }

}
