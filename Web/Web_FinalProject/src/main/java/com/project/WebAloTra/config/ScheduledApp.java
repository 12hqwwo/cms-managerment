package com.project.WebAloTra.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.WebAloTra.entity.DiscountCode;
import com.project.WebAloTra.repository.DiscountCodeRepository;
import com.project.WebAloTra.service.DiscountCodeService;

import java.util.Date;
import java.util.List;

@Component
public class ScheduledApp {
    @Autowired
    private DiscountCodeService discountCodeService;

    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    @Scheduled(fixedRate = 60*60*100) // Run every 24 hours, adjust as needed
    public void checkAndSetExpiredStatus() {
        Date currentDate = new Date();
        List<DiscountCode> expiredDiscountCodes = null;
        if(!expiredDiscountCodes.isEmpty()) {
            for (DiscountCode discountCode : expiredDiscountCodes) {
                if (currentDate.after(discountCode.getEndDate())) {
                    discountCode.setStatus(3);
                    discountCodeRepository.save(discountCode);
                }
            }
        }

    }
}
