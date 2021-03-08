package com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user;

import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Component
public class User implements UserDetails {
    private UserBaseInfo userBaseInfo;
    private UserPrivate userPrivate;
    private String userID;
    private List<Role> userRoles;

    public String getUserID(){
        return userBaseInfo.getId();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> roles = new ArrayList<>();
        for(Role role:userRoles){
            roles.add(new SimpleGrantedAuthority(role.getBaseRole().getName()));
        }
        return roles;
    }



    @Override
    public String getPassword() {
        return userPrivate.getPassword();
    }

    @Override
    public String getUsername() {
        return userPrivate.getAccount();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return userPrivate.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
