package com.tilldock.auth.service;

import com.tilldock.auth.dto.BusinessDto;
import com.tilldock.auth.dto.BusinessRequest;
import com.tilldock.auth.entity.Business;
import com.tilldock.auth.repository.BusinessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BusinessService {

    private final BusinessRepository businesses;

    public BusinessService(BusinessRepository businesses) {
        this.businesses = businesses;
    }

    @Transactional(readOnly = true)
    public BusinessDto getForMerchant(UUID merchantId) {
        Business business = businesses.findByMerchantId(merchantId)
                .orElseThrow(AuthExceptions.BusinessNotFoundException::new);
        return BusinessDto.from(business);
    }

    @Transactional
    public BusinessDto upsertForMerchant(UUID merchantId, BusinessRequest req) {
        Business business = businesses.findByMerchantId(merchantId)
                .orElseGet(() -> {
                    Business created = new Business();
                    created.setMerchantId(merchantId);
                    return created;
                });
        business.setBusinessName(req.getBusinessName().trim());
        business.setAddress(trimToNull(req.getAddress()));
        business.setPhone(trimToNull(req.getPhone()));
        business.setEmail(trimToNull(req.getEmail()));
        business.setLogoUrl(trimToNull(req.getLogoUrl()));
        if (req.getCurrency() != null && !req.getCurrency().isBlank()) {
            business.setCurrency(req.getCurrency().trim().toUpperCase());
        }
        if (req.getTaxRate() != null) {
            business.setTaxRate(req.getTaxRate());
        }
        Business saved = businesses.save(business);
        return BusinessDto.from(saved);
    }

    @Transactional
    public void deleteForMerchant(UUID merchantId) {
        Business business = businesses.findByMerchantId(merchantId)
                .orElseThrow(AuthExceptions.BusinessNotFoundException::new);
        businesses.delete(business);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
