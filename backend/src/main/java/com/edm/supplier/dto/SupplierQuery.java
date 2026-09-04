package com.edm.supplier.dto;

public record SupplierQuery(String keyword, String fetcherType, Boolean enabled, long page, long size) {
}
