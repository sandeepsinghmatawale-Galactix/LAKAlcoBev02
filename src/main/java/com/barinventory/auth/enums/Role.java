package com.barinventory.auth.enums;

public enum Role {
    ADMIN,
    BUSINESS_OWNER,
    EMPLOYE;
    
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}