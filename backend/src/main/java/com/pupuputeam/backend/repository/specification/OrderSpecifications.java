package com.pupuputeam.backend.repository.specification;

import com.pupuputeam.backend.model.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecifications {
    public static Specification<Order> amountInRange(BigDecimal min, BigDecimal max) {
        return (root, query, criteriaBuilder) ->{
            List<Predicate> predicates = new ArrayList<>();
            if(min != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), min));
            }
            if(max != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), max));
            }
            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    public static Specification<Order> timeBetween(Instant start, Instant end) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (start != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), start));
            }
            if (end != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), end));
            }
            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    public static Specification<Order> countyNameEquals(String countyName) {
        return (root, query, criteriaBuilder) ->{
            if (countyName == null || countyName.isBlank()) {
                return null;
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("countyName")), "%" + countyName.toLowerCase() + "%");
        };
    }
    public static Specification<Order> muniNameEquals(String muniName) {
        return (root, query, criteriaBuilder) ->{
            if (muniName == null || muniName.isBlank()) {
                return null;
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("muniName")), "%" + muniName.toLowerCase() + "%");
        };
    }
    public static Specification<Order> compositeTaxRateInRange(BigDecimal min, BigDecimal max) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (min != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("compositeTaxRate"), min));
            }
            if (max != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("compositeTaxRate"), max));
            }
            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
