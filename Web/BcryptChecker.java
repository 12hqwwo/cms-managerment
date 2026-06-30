import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptChecker {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out
                .println(encoder.matches("Admin@123", "$2a$10$qSYJ26q5Fk6XourrT0UuNOPe6TmlIelQL2CL2.g2DZ1xtR1dESMWm"));
        System.out.println(encoder.encode("Admin@123"));
    }
}
