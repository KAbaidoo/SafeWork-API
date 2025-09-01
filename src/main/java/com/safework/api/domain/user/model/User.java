package com.safework.api.domain.user.model;
import com.safework.api.domain.organization.model.Organization;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    // ... (id, name, email, role, etc.)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
}