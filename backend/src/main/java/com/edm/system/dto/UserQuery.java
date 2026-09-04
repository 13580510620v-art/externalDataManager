package com.edm.system.dto;

public record UserQuery(String keyword, Long groupId, Boolean enabled, long page, long size) {
}
