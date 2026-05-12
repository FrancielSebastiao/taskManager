package com.fransebastiao.taskmanager.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fransebastiao.taskmanager.domain.user.Privilege;
import com.fransebastiao.taskmanager.domain.user.Role;
import com.fransebastiao.taskmanager.domain.user.User;

import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String name;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id          = user.getId();
        this.name        = user.getName();
        this.email       = user.getEmail();
        this.password    = user.getPasswordHash();
        this.authorities = getAuthorities(user.getRoles());
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override 
    public Collection<? extends GrantedAuthority> getAuthorities() { 
        return authorities; 
    }
    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return true; }

    private Collection<? extends GrantedAuthority> getAuthorities(final Collection<Role> roles) {
        return getGrantedAuthorities(getPrivileges(roles));
    }

    private List<String> getPrivileges(final Collection<Role> roles) {
        final List<String> privileges = new ArrayList<>();
        final List<Privilege> collection = new ArrayList<>();

        for (final Role role : roles) {
            privileges.add(role.getName());
            collection.addAll(role.getPrivileges());
        }

        for (final Privilege item : collection) {
            privileges.add(item.getName());
        }

        return privileges;                                                                         
    }

    private List<GrantedAuthority> getGrantedAuthorities(final List<String> privileges) {
        final List<GrantedAuthority> authorities = new ArrayList<>();

        for (final String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }

        return authorities;
    }
}
