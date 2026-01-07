package Esame.Back_End.Esame.Back_End.security;

import Esame.Back_End.Esame.Back_End.model.Admin;
import Esame.Back_End.Esame.Back_End.model.Customer;
import Esame.Back_End.Esame.Back_End.model.Manager;
import Esame.Back_End.Esame.Back_End.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserDetailsImpl implements UserDetails {
    
    private final User user;
    
    public UserDetailsImpl(User user) {
        this.user = user;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = getUserRole();
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }
    
    private String getUserRole() {
        if (user instanceof Admin) {
            return "ADMIN";
        } else if (user instanceof Manager) {
            return "MANAGER";
        } else if (user instanceof Customer) {
            return "CUSTOMER";
        }
        return "USER";
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        if (user instanceof Customer) {
            return ((Customer) user).getIsActive();
        }
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    public User getUser() {
        return user;
    }
    
    public Long getUserId() {
        return user.getId();
    }
}

