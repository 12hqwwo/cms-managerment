# Hướng dẫn cài đặt và chạy hệ thống Web AloTra (Spring Boot + Oracle)

Dự án này là hệ thống quản lý chuỗi cửa hàng trà sữa được phát triển bằng Spring Boot 3 và sử dụng cơ sở dữ liệu Oracle (với các tính năng bảo mật nâng cao như VPD, OLS). Dưới đây là hướng dẫn chi tiết để tải code về và chạy thành công trên máy.

## 1. Yêu cầu hệ thống (Prerequisites)
- **Java Development Kit (JDK):** Phiên bản 17 (hoặc mới hơn).
- **Maven:** Cài đặt Maven để quản lý thư viện và build project.
- **Oracle Database:** Oracle 19c hoặc 23ai (cần có CDB/PDB, ví dụ `FREEPDB1` hoặc `ORCLPDB1`).
- **Tài khoản sys as sysdba:** Để chạy script tạo user và phân quyền bảo mật.
- **IDE:** IntelliJ IDEA, Eclipse, hoặc VS Code.

## 2. Cài đặt Cơ Sở Dữ Liệu (Oracle Database)

Dự án sử dụng Oracle Database với các cấu hình bảo mật đặc thù (Oracle Label Security - OLS, và Virtual Private Database - VPD). Hãy làm theo các bước sau:

**Bước 2.1: Chạy file script tạo User và cấp quyền**
1. Mở SQL Developer hoặc DataGrip.
2. Kết nối vào PDB (ví dụ `FREEPDB1`) bằng tài khoản `sys as sysdba` (VD: `sys/Mật_khẩu_của_bạn@//localhost:1521/FREEPDB1 as sysdba`).
3. Mở và chạy file `Web/sql_test/01_create_user_trasua.sql`.
   *(Lưu ý: Phải chạy bằng `sys` để nó tạo user `TRASUA` và cấp các quyền quan trọng như `LBAC_DBA`, `EXECUTE ON SA_SYSDBA`, v.v.)*

**Bước 2.2: Khởi tạo bảng và cấu trúc dữ liệu chính (DDL & DML)**
1. Tạo một kết nối (Connection) mới với user vừa tạo:
   - Username: `TRASUA`
   - Password: `TraSua@2024`
   - Service Name / SID: `FREEPDB1`
2. Sử dụng connection `TRASUA`, chạy **toàn bộ** file `Web/sql_test/00_MASTER_TRASUA.sql`.
   *(Nhấn Run Script / F5 để nó chạy từ đầu đến cuối).*

**Bước 2.3: Chạy script fix lỗi VPD (Rất quan trọng!)**
Do có một số chính sách VPD (Virtual Private Database) cũ xung đột với hệ thống Oracle Label Security mới của Spring Boot, làm ẩn dữ liệu sản phẩm của Vendor và chặn dữ liệu thống kê, bạn bắt buộc phải xóa các VPD này đi.
1. Kết nối lại bằng quyền `sys as sysdba`.
2. Mở và chạy file `Web/sql_test/fix_vpd.sql` (hoặc bôi đen nội dung và chạy).
   *(Script này sẽ gỡ bỏ `POL_BRANCH_INVENTORY`, `POLICY_BILL_STATUS`, `VPD_POLICY_BILL_BRANCH` đang bị lỗi).*

## 3. Cấu hình Spring Boot (application.properties)

1. Mở file `Web/Web_FinalProject/src/main/resources/application.properties`.
2. Kiểm tra thông số kết nối cơ sở dữ liệu:
   ```properties
   spring.datasource.url=jdbc:oracle:thin:@localhost:1521/FREEPDB1
   spring.datasource.username=TRASUA
   spring.datasource.password=TraSua@2024
   ```
   *(Sửa lại `FREEPDB1` thành Tên PDB của máy bạn nếu dùng bản Oracle khác).*

3. (Tùy chọn) Cấu hình Cloudinary (ảnh), Email và VNPay nếu cần test chức năng thực tế. Các key hiện tại đã được cấp sẵn trong file properties.

## 4. Build và Chạy dự án

Bạn có thể chạy dự án thông qua Terminal hoặc IDE:

**Cách 1: Chạy bằng Maven command line**
1. Mở terminal và trỏ vào thư mục `Web/Web_FinalProject/`.
2. Chạy lệnh:
   ```bash
   mvn clean install -DskipTests
   mvn spring-boot:run
   ```

**Cách 2: Chạy bằng IntelliJ IDEA / VS Code**
1. Mở thư mục `Web_FinalProject` (nơi chứa `pom.xml`) bằng IDE của bạn.
2. Đợi IDE tải xong các thư viện Maven.
3. Chạy class `WebAloTraApplication.java` (File chính).

## 5. Truy cập hệ thống
Sau khi console báo Tomcat started trên port 8080:
- Trang chủ (Khách hàng): `http://localhost:8080/`
- Trang quản trị Admin / Vendor: `http://localhost:8080/admin/login`

**Các tài khoản dùng để test:**
- **Admin (Giám đốc):** `vohuutin1@gmail.com` / `123456`
- **Vendor (Quản lý chi nhánh):** `vohuutin5@gmail.com` / `123456` (Đây là quản lý của chi nhánh số 8).
- **Staff (Nhân viên bán hàng):** `vohuutin4@gmail.com` / `123456`

---
*Lưu ý: Nếu có lỗi "Transaction silently rolled back" khi duyệt đơn hoặc "Trống trơn" dữ liệu tại giao diện Vendor, tức là bước 2.3 (chạy fix_vpd.sql) chưa được thực hiện đúng cách bằng quyền sysdba.*
