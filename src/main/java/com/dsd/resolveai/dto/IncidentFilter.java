package com.dsd.resolveai.dto;

import com.dsd.resolveai.enums.FilterOperator;

public record IncidentFilter(String field, FilterOperator operator, String value) {}
