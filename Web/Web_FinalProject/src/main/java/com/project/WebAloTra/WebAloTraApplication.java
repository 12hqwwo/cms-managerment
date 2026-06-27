package com.project.WebAloTra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import com.project.WebAloTra.repository.AccountRepository;
import com.project.WebAloTra.repository.RoleRepository;
import com.project.WebAloTra.repository.ProductRepository;
import com.project.WebAloTra.repository.CustomerRepository;
import com.project.WebAloTra.repository.BranchRepository;
import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.entity.Role;
import com.project.WebAloTra.entity.Product;
import com.project.WebAloTra.entity.Customer;
import com.project.WebAloTra.entity.Branch;
import com.project.WebAloTra.entity.enumClass.RoleName;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

@SpringBootApplication(scanBasePackageClasses = WebAloTraApplication.class )
public class WebAloTraApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebAloTraApplication.class, args);
	}

	@Bean
	public CommandLineRunner runDiagnostics(
			AccountRepository accountRepository,
			RoleRepository roleRepository,
			ProductRepository productRepository,
			CustomerRepository customerRepository,
			BranchRepository branchRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			System.out.println("==================================================");
			System.out.println("=== DIAGNOSTICS & PROGRAMMATIC DATA ALIGNMENT ===");
			System.out.println("==================================================");
			
			// 1. Ensure all Roles exist
			for (RoleName rn : RoleName.values()) {
				try {
					if (!roleRepository.findByName(rn).isPresent()) {
						Role r = new Role();
						r.setName(rn);
						r.setCreateDate(new java.util.Date());
						r.setUpdateDate(new java.util.Date());
						roleRepository.save(r);
						System.out.println("Programmatically created role: " + rn);
					}
				} catch (Exception e) {
					System.err.println("Error creating role " + rn + ": " + e.getMessage());
				}
			}

			// 2. Ensure Branches exist
			Branch branchHN = null;
			try {
				java.util.Optional<Branch> bhn = branchRepository.findByBranchCode("CN001");
				if (!bhn.isPresent()) {
					Branch b = new Branch();
					b.setBranchCode("CN001");
					b.setBranchName("Chi nhánh Trà Sữa Hà Nội");
					b.setAddress("123 Phố Huế, Hai Bà Trưng, Hà Nội");
					b.setPhone("0901234567");
					b.setEmail("hanoi@trasua.vn");
					b.setActive(true);
					b.setCreateDate(java.time.LocalDateTime.now());
					b.setUpdateDate(java.time.LocalDateTime.now());
					branchHN = branchRepository.save(b);
					System.out.println("Programmatically created Branch HN");
				} else {
					branchHN = bhn.get();
				}
			} catch (Exception e) {
				System.err.println("Error creating Branch HN: " + e.getMessage());
			}

			// 3. Ensure Customers exist
			Customer cAdmin = null;
			Customer cStaff = null;
			Customer cUser = null;
			try {
				cAdmin = customerRepository.findByCode("KH0001");
				if (cAdmin == null) {
					cAdmin = new Customer();
					cAdmin.setCode("KH0001");
					cAdmin.setName("Quản Trị Viên");
					cAdmin.setEmail("admin@trasua.vn");
					cAdmin.setPhoneNumber("0900000000");
					cAdmin = customerRepository.save(cAdmin);
					System.out.println("Programmatically created Customer KH0001");
				}
				
				cStaff = customerRepository.findByCode("KH0002");
				if (cStaff == null) {
					cStaff = new Customer();
					cStaff.setCode("KH0002");
					cStaff.setName("Nguyễn Văn An");
					cStaff.setEmail("nguyenvanan@gmail.com");
					cStaff.setPhoneNumber("0911111111");
					cStaff = customerRepository.save(cStaff);
					System.out.println("Programmatically created Customer KH0002");
				}

				cUser = customerRepository.findByCode("KH0003");
				if (cUser == null) {
					cUser = new Customer();
					cUser.setCode("KH0003");
					cUser.setName("Trần Thị Bình");
					cUser.setEmail("tranthib@gmail.com");
					cUser.setPhoneNumber("0922222222");
					cUser = customerRepository.save(cUser);
					System.out.println("Programmatically created Customer KH0003");
				}
			} catch (Exception e) {
				System.err.println("Error creating Customers: " + e.getMessage());
			}

			// 4. Ensure Accounts exist and set/reset their passwords to "Admin@123"
			String targetHash = passwordEncoder.encode("Admin@123");
			
			// Admin
			try {
				Account adminAcc = accountRepository.findByEmail("admin@trasua.vn");
				if (adminAcc == null) {
					adminAcc = new Account();
					adminAcc.setCode("AC0001");
					adminAcc.setEmail("admin@trasua.vn");
				}
				adminAcc.setPassword(targetHash);
				adminAcc.setNonLocked(true);
				adminAcc.setCreateDate(java.time.LocalDateTime.now());
				adminAcc.setUpdateDate(java.time.LocalDateTime.now());
				adminAcc.setCustomer(cAdmin);
				adminAcc.setRole(roleRepository.findByName(RoleName.ROLE_ADMIN).orElse(null));
				accountRepository.save(adminAcc);
				System.out.println("Programmatically aligned admin@trasua.vn account");
			} catch (Exception e) {
				System.err.println("Error aligning admin account: " + e.getMessage());
			}

			// Staff
			try {
				Account staffAcc = accountRepository.findByEmail("staff@trasua.vn");
				if (staffAcc == null) {
					staffAcc = new Account();
					staffAcc.setCode("AC0002");
					staffAcc.setEmail("staff@trasua.vn");
				}
				staffAcc.setPassword(targetHash);
				staffAcc.setNonLocked(true);
				staffAcc.setCreateDate(java.time.LocalDateTime.now());
				staffAcc.setUpdateDate(java.time.LocalDateTime.now());
				staffAcc.setCustomer(cStaff);
				staffAcc.setBranch(branchHN);
				staffAcc.setRole(roleRepository.findByName(RoleName.ROLE_STAFF).orElse(null));
				accountRepository.save(staffAcc);
				System.out.println("Programmatically aligned staff@trasua.vn account");
			} catch (Exception e) {
				System.err.println("Error aligning staff account: " + e.getMessage());
			}

			// User
			try {
				Account userAcc = accountRepository.findByEmail("user@trasua.vn");
				if (userAcc == null) {
					userAcc = new Account();
					userAcc.setCode("AC0003");
					userAcc.setEmail("user@trasua.vn");
				}
				userAcc.setPassword(targetHash);
				userAcc.setNonLocked(true);
				userAcc.setCreateDate(java.time.LocalDateTime.now());
				userAcc.setUpdateDate(java.time.LocalDateTime.now());
				userAcc.setCustomer(cUser);
				userAcc.setRole(roleRepository.findByName(RoleName.ROLE_USER).orElse(null));
				accountRepository.save(userAcc);
				System.out.println("Programmatically aligned user@trasua.vn account");
			} catch (Exception e) {
				System.err.println("Error aligning user account: " + e.getMessage());
			}

			// Vendor
			try {
				Account vendorAcc = accountRepository.findByEmail("vendor@trasua.vn");
				if (vendorAcc == null) {
					vendorAcc = new Account();
					vendorAcc.setCode("AC0004");
					vendorAcc.setEmail("vendor@trasua.vn");
				}
				vendorAcc.setPassword(targetHash);
				vendorAcc.setNonLocked(true);
				vendorAcc.setCreateDate(java.time.LocalDateTime.now());
				vendorAcc.setUpdateDate(java.time.LocalDateTime.now());
				vendorAcc.setCustomer(cStaff);
				vendorAcc.setBranch(branchHN);
				vendorAcc.setRole(roleRepository.findByName(RoleName.ROLE_VENDOR).orElse(null));
				accountRepository.save(vendorAcc);
				System.out.println("Programmatically aligned vendor@trasua.vn account");
			} catch (Exception e) {
				System.err.println("Error aligning vendor account: " + e.getMessage());
			}

			// 5. Diagnostics Print
			try {
				System.out.println("Total Roles in DB: " + roleRepository.count());
				System.out.println("Total Accounts in DB: " + accountRepository.count());
				List<Account> accounts = accountRepository.findAll();
				for (Account a : accounts) {
					String roleName = (a.getRole() != null) ? a.getRole().getName().toString() : "NULL";
					System.out.println(" - Account ID: " + a.getId() 
						+ " | Email: [" + a.getEmail() + "]" 
						+ " | Role: " + roleName
						+ " | Password (hashed): " + a.getPassword()
						+ " | NonLocked: " + a.isNonLocked());
				}
				System.out.println("Total Products in DB: " + productRepository.count());
			} catch (Exception e) {
				System.err.println("Diagnostics print failed: " + e.getMessage());
			}

			System.out.println("==================================================");
		};
	}
}

