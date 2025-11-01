package com.example.users.service;

import com.example.users.entity.Admin;

public interface IAdminService {
    Admin register(Admin admin);
    String approveUser(String email);
    String rejectUser(String email);
}
