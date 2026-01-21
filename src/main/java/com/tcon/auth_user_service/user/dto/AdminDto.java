package com.tcon.auth_user_service.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDto {

    private String id;
    private String userId;
    private String roleDescription;
    private Boolean superAdmin;
    private List<String> permissions;
    private String department;
}
