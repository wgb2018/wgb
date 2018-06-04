package com.microdev.converter;

import com.microdev.common.exception.ParamsException;
import com.microdev.common.utils.DateUtil;
import com.microdev.common.utils.PasswordHash;
import com.microdev.model.User;
import com.microdev.param.UserDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;

/**
 * @author liutf
 */
@Component
public class UserConverter {
    public void modifyBaseInfo(UserDTO userDTO, User user) {
        if (userDTO.getNickname() != null) user.setNickname(userDTO.getNickname());
        if (userDTO.getAvatar() != null) user.setAvatar(userDTO.getAvatar());
        if (userDTO.getEmail() != null) user.setEmail(userDTO.getEmail());
    }

    public UserDTO getUserInfo(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getPid());
        userDTO.setNickname(user.getNickname());
        userDTO.setUsername(user.getUsername());
        userDTO.setMobile(user.getMobile());
        userDTO.setEmail(user.getEmail());
        userDTO.setAvatar(user.getAvatar());
        userDTO.setUserType(user.getUserType());
        userDTO.setActivated(user.isActivated());
        userDTO.setRoleList(new ArrayList<>(user.getRoles()));
        userDTO.setSex(user.getSex());
        userDTO.setCreateTime(user.getCreateTime());
        userDTO.setModifyTime(user.getModifyTime());
        return userDTO;
    }

    public User createUser(UserDTO userDTO) throws Exception {
        if (!StringUtils.hasText(userDTO.getPassword())) {
            throw new ParamsException("密码不能为空");
        }
        userDTO.setPassword(PasswordHash.createHash(userDTO.getPassword()));
        userDTO.setId(null);
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        return user;
    }

    public void update(UserDTO userDTO, User user) throws Exception {
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(PasswordHash.createHash(userDTO.getPassword()));
        }
        if (userDTO.getNickname() != null) user.setNickname(userDTO.getNickname());
        if (userDTO.getUsername() != null) user.setUsername(userDTO.getUsername());
        if (userDTO.getMobile() != null) user.setMobile(userDTO.getMobile());
        if (userDTO.getEmail() != null) user.setEmail(userDTO.getEmail());
        if (userDTO.getAvatar() != null) user.setAvatar(userDTO.getAvatar());
        if (userDTO.getUserType() != null) user
                .setUserType(userDTO.getUserType());
        if (userDTO.getActivated() != null) user.setActivated(userDTO.getActivated());
        if (userDTO.getSex() != null) user.setSex(userDTO.getSex());
		if (userDTO.getBirthday() != null){
            user.setBirthday(userDTO.getBirthday());
            user.setAge(DateUtil.CaculateAge(userDTO.getBirthday()));
        }
        //if (userDTO.getLaborDispatchCard () != null) user.setLaborDispatchCard (userDTO.getLaborDispatchCard ());
    }
}
