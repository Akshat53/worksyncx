package com.worksyncx.hrms.dto.attendance;

import lombok.Data;

@Data
public class CheckInRequest {
    private String location;
    private String notes;
}
