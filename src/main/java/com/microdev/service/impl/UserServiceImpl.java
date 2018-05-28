package com.microdev.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.common.context.ServiceContextHolder;
import com.microdev.common.exception.AuthenticationException;
import com.microdev.common.exception.AuthorizationException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.utils.PasswordHash;
import com.microdev.common.utils.TokenUtil;
import com.microdev.converter.UserConverter;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.service.*;
import com.microdev.type.PlatformType;
import com.microdev.type.SocialType;
import com.microdev.type.UserSex;
import com.microdev.type.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.*;

@Transactional
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService{
    @Autowired
    public UserMapper userMapper;
    @Autowired
    public TokenService tokenService;
    @Autowired
    public RoleMapper roleMapper;
    @Autowired
    private SmsFacade smsFacade;
    @Autowired
    private WorkerMapper workerMapper;
    @Autowired
    private SocialMapper socialMapper;
    @Autowired
    private UserConverter userConverter;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskWorkerService taskWorkerService;
    @Autowired
    private TaskHrCompanyService taskHrCompanyService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private DictMapper dictMapper;

    @Override
    public User create(User user) throws Exception{
        try{
            userMapper.insert(user);
        }catch (Exception e){
            e.printStackTrace ();
            throw new Exception ("用户添加失败");
        }
        return user;
    }

    @Override
    public List<User> query(UserDTO user) throws Exception {
        List<User> userList= null;
        try{
            userList = userMapper.query(user);
        }catch (Exception e){
            e.printStackTrace ();
            throw new Exception ("用户查询失败");
        }
        return userList;
    }

    @Override
    public TokenDTO login(UserDTO login) throws Exception {
        User user = userMapper.findByMobile(login.getMobile());
        UserDTO userDTO = new UserDTO();
        String userId = user.getPid();
        userDTO.setId(user.getPid());
        userDTO.setNickname(user.getNickname());
        userDTO.setRoleList(new ArrayList<>(user.getRoles()));
        userDTO.setUserType(user.getUserType());
        if (user != null && PasswordHash.validatePassword(login.getPassword(), user.getPassword())) {
            return tokenService.accessToken(userDTO, login.getPlatform().name());
        }
        throw new ParamsException("用户名或密码错误");
    }

    @Override
    public TokenDTO register(UserDTO register) throws Exception{
        if (register.getUserType() == UserType.platform) {
            throw new AuthorizationException("无权限注册该用户");
        }
        //smsService.checkSmsCode(register.getMobile(), SmsType.register.name(), register.getSmsCode());
        if (userMapper.findByMobile(register.getMobile()) != null) {
            throw new ParamsException("手机号码已经存在");
        }
        User newUser = new User();
        newUser.setUserType(register.getUserType());
        newUser.setMobile(register.getMobile());
        newUser.setUserCode(register.getUserType()
                .toString());
        if (StringUtils.hasText(register.getPassword())) {
            newUser.setPassword(PasswordHash.createHash(register.getPassword()));
        }
        newUser.setUsername(newUser.getMobile());
        if(newUser.getUserType().name().equals("worker")){
            Worker worker = new Worker();
            workerMapper.insert(worker);
            newUser.setWorkerId(worker.getPid());
        } else if(newUser.getUserType().name().equals("hotel")){
            Company company = new Company();
            company.setStatus(0);
            company.setLeaderMobile(register.getMobile());
            company.setCompanyType(1);
            companyMapper.insert(company);
        }else if(newUser.getUserType().name().equals("hr")){
            Company company = new Company();
            company.setStatus(0);
            company.setLeaderMobile(register.getMobile());
            company.setCompanyType(2);
            companyMapper.insert(company);
        }
        //存入用户
        userMapper.insert(newUser);
        //存入用户角色关系
        roleMapper.insertRoleAndUserRelation(newUser
                .getPid(),newUser.getUserCode());
        List<Role> roleList = roleMapper.queryAllRolesByUserId(newUser.getPid());
        UserDTO userDTO = new UserDTO();
        userDTO.setId(newUser.getPid());
        userDTO.setMobile(newUser.getMobile());
        userDTO.setUsername(newUser.getUsername());
        userDTO.setUserType(newUser.getUserType());
        userDTO.setRoleList(roleList);
        return tokenService.accessToken(userDTO, register.getPlatform().name());
    }
    /**
     * 注销登录
     */
    @Override
    public void logout() {
        HttpServletRequest request = ServiceContextHolder.getServiceContext().getHttpServletRequest();
        String token = TokenUtil.parseBearerToken(request);
        tokenService.deleteAccessToken(token);
    }
    /**
     * 修改密码
     */
    @Override
    public TokenDTO changePwd(ChangePwdRequest request) throws Exception {
        if (!StringUtils.hasText(request.getUserId())) {
            throw new ParamsException("用ID不能为空");
        }
        if (!StringUtils.hasText(request.getOldPwd())) {
            throw new ParamsException("密码不能为空");
        }
        if (!StringUtils.hasText(request.getNewPwd())) {
            throw new ParamsException("新密码不能为空");
        }
        User user = userMapper.queryByUserId(request.getUserId());
        if(user==null){
            throw new ParamsException("用户名不存在");
        }
        if (PasswordHash.validatePassword(request.getOldPwd(), user.getPassword())) {
            user.setPassword(PasswordHash.createHash(request.getNewPwd()));
            userMapper.updateById(user);
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getPid());
            userDTO.setMobile(user.getMobile());
            userDTO.setUsername(user.getUsername());
            userDTO.setUserType(user.getUserType());
            return tokenService.accessToken(userDTO, request.getPlatform().name());
        }else{
            throw new ParamsException("原密码错误");
        }
    }

    /**
     * 手机号+短信验证码 登录
     */
    @Override
    public TokenDTO smslogin(UserDTO login) {
        User user = userMapper.findByMobile(login.getMobile());
        if (user != null) {
            smsFacade.checkSmsCode(login.getMobile(), SmsType.login.name(), login.getSmsCode());
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getPid());
            userDTO.setMobile(user.getMobile());
            userDTO.setUsername(user.getUsername());
            userDTO.setUserType(user.getUserType());
            return tokenService.accessToken(userDTO, login.getPlatform().name());
        }
        throw new ParamsException("该用户不存在");
    }

    /**
     * 微信注册
     */
    @Override
    public TokenDTO register_weixin(WeixinUserInfo weixinUserInfo) throws Exception {
        if (weixinUserInfo.getUserType() == UserType.platform) {
            throw new AuthorizationException("无权限注册该用户");
        }
        smsFacade.checkSmsCode(weixinUserInfo.getMobile(), SmsType.register.name(), weixinUserInfo.getSmsCode());
        User user = userMapper.findByMobile(weixinUserInfo.getMobile());
        if (user == null) {
            user = new User();
            user.setUsername(weixinUserInfo.getMobile());
            user.setUserType(weixinUserInfo.getUserType());
            user.setMobile(weixinUserInfo.getMobile());
            if (StringUtils.hasText(weixinUserInfo.getPassword())) {
                user.setPassword(PasswordHash.createHash(weixinUserInfo.getPassword()));
            }
            user.setAvatar(weixinUserInfo.getHeadimgurl());
            user.setNickname(weixinUserInfo.getNickname());
            switch (weixinUserInfo.getSex()){
                case "1":
                    user.setSex(UserSex.MALE);
                    break;
                case "2":
                    user.setSex(UserSex.FEMALE);
                    break;
                default:
                    user.setSex(UserSex.UNKNOW);
            }

            Role currentRole = roleMapper.findByCode(weixinUserInfo.getUserType().name());

            if (currentRole != null && !weixinUserInfo.getUserType().equals(UserType.platform))
            {
                user.getRoles().add(currentRole);
            }

            if(user.getUserType() == UserType.worker) {
                Worker worker = new Worker();
                workerMapper.insert(worker);
                user.setWorkerId(worker.getPid());
            }
            //注册添加用户
            userMapper.insert(user);
        }

        //绑定 openId
        Social social = socialMapper.findByOpendIdAndType(weixinUserInfo.getOpenId(), SocialType.WEIXIN);
        if (social == null) {
            social = new Social();
            social.setType(SocialType.WEIXIN);
            social.setOpendId(weixinUserInfo.getOpenId());
            social.setDisplayName(weixinUserInfo.getNickname());
            social.setImageUrl(weixinUserInfo.getHeadimgurl());
            social.setSex(weixinUserInfo.getSex());
            social.setLanguage(weixinUserInfo.getLanguage());
            social.setProvince(weixinUserInfo.getProvince());
            social.setCity(weixinUserInfo.getCity());
            social.setCountry(weixinUserInfo.getCountry());
        }
        social.setUserId(user.getPid());
        socialMapper.insert(social);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getPid());
        userDTO.setMobile(user.getMobile());
        userDTO.setUsername(user.getUsername());
        userDTO.setUserType(user.getUserType());
        return tokenService.accessToken(userDTO, PlatformType.WEIXIN.name());
    }
    /**
     * 重置密码
     */
    @Override
    public void resetPwd(UserDTO userDTO) throws Exception {
        //TODO 新密码强度规则校验
        smsFacade.checkSmsCode(userDTO.getMobile(), SmsType.reset_password.name(), userDTO.getSmsCode());
        User user = userMapper.findByMobile(userDTO.getMobile());
        userMapper.selectOne(user);
        user.setPassword(PasswordHash.createHash(userDTO.getPassword()));
        userMapper.updateById(user);
    }
    /**
     * 修改基础信息
     */
    @Override
    public void modifyBaseInfo(UserDTO userDTO) {
        com.microdev.common.context.User loginUser = ServiceContextHolder.getServiceContext().getUser();
        User user = userMapper.queryByUserId(loginUser.getId());
        try {
            userConverter.update(userDTO, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        userMapper.updateById(user);
    }
    /**
     * 修改手机号
     */
    @Override
    public void modifyMobile(String newMobile, String newMobileSmsCode) {
        if (userMapper.findByMobile(newMobile) != null) {
            throw new AuthenticationException("该手机号码已被绑定");
        }
        smsFacade.checkSmsCode(newMobile, SmsType.identity_check.name(), newMobileSmsCode);
        com.microdev.common.context.User loginUser = ServiceContextHolder.getServiceContext().getUser();
        User user = userMapper.queryByUserId(loginUser.getId());
        user.setMobile(newMobile);
        userMapper.updateById(user);
    }
    /**
     * 获取个人信息
     */
    @Override
    public UserDTO me() {
        //上下文中可以获取到当前登录用户的信息
        com.microdev.common.context.User user = ServiceContextHolder.getServiceContext().getUser();

        UserType userType = user.getObj("userType", UserType.class);


        String mobile = user.getString("mobile");
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getString("username"));
        userDTO.setNickname(user.getString("nickName"));
        userDTO.setAvatar( userMapper.queryByUserId(user.getId()).getAvatar());
        try{
            switch (user.getString("sex")) {
                case "男":
                    userDTO.setSex(UserSex.MALE);
                    break;
                case "女":
                    userDTO.setSex(UserSex.FEMALE);
                    break;
                default:
                    userDTO.setSex(UserSex.UNKNOW);
            }
        }
        catch (Exception e) {
            userDTO.setSex(UserSex.UNKNOW);
        }
        userDTO.setMobile(mobile);
        userDTO.setUserType(userType);
        userDTO.setRoleList((ArrayList<Role>) user.get("roles"));
        //如果当前的是酒店或者人力公司，那么就查询出他们的公司审核状态
        Map<String, Integer> totalMap = new HashMap<>();
        if (userType == UserType.hr || userType == UserType.hotel) {
            Company company = companyMapper.findFirstByLeaderMobile(mobile);
            if (company != null) {
                CompanyViewModelDTO companyDTO = new CompanyViewModelDTO();
                companyDTO.setId(company.getPid());
                companyDTO.setName(company.getName());
                companyDTO.setCompanyType(company.getCompanyType());
                companyDTO.setStatus(company.getStatus());
                userDTO.setCompany(companyDTO);
				userDTO.setServiceType (dictMapper.queryTypeByUserId (company.getPid ()));
                //如果是酒店

                if (company.getCompanyType() == 1) {
                    totalMap.put("curTask", taskService.selectUnReadAmount(company.getPid()));
                    totalMap.put("completeTask", taskService.selectCompleteAmount(company.getPid()));
                    totalMap.put("pendingTask", messageService.selectUnHandleMessageAmount(company.getPid(), "3", 0));
                    totalMap.put("message", messageService.selectMessageCount(company.getPid(), "3", 0));
                } else {
                    totalMap.put("pendingTask", messageService.selectUnHandleMessageAmount(company.getPid(), "2", 0));
                    totalMap.put("message", messageService.selectMessageCount(company.getPid(), "2", 0));
                    totalMap.put("curTask", taskHrCompanyService.selectUnreadCount(company.getPid()));
                    totalMap.put("completeTask", taskHrCompanyService.selectCompleteCount(company.getPid()));
                }
            }
        } else if (userType == UserType.worker) {
            String workerId = user.getString("workerId");
            Worker worker = workerMapper.queryById(workerId);
            userDTO.setWorkerId(workerId);
            userDTO.setIdCardNumber(worker.getIdcardNumber());
            userDTO.setHealthCard(worker.getHealthCard());
            userDTO.setIdCardFront(worker.getIdcardFront());
            userDTO.setIdCardBack(worker.getIdcardBack());
            totalMap.put("pendingTask", messageService.selectUnHandleMessageAmount(workerId, "1", 0));
            totalMap.put("message", messageService.selectMessageCount(workerId, "1", 0));
            totalMap.put("curTask", taskWorkerService.selectUnreadCount(workerId));
            totalMap.put("completeTask", taskWorkerService.selectCompleteCount(workerId));
        }
        return userDTO;
    }

}
