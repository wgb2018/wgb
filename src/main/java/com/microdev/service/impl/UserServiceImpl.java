package com.microdev.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.common.context.ServiceContextHolder;
import com.microdev.common.exception.AuthenticationException;
import com.microdev.common.exception.AuthorizationException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.oss.ObjectStoreService;
import com.microdev.common.utils.FileUtil;
import com.microdev.common.utils.PasswordHash;
import com.microdev.common.utils.QRCodeUtil;
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
import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
    @Autowired
    private WorkerLogMapper workerLogMapper;
    @Autowired
    private ObjectStoreService objectStoreService;
    @Autowired
    DictService dictService;
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
        File file;
        String fileURI = null;
        String filePath;

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
            file = QRCodeUtil.createQRCode (worker.getPid ()+"WGB"+register.getUserType());
            filePath = "QRCode".toLowerCase() + "/" + FileUtil.fileNameReplaceSHA1(file);
            //文件上传成功后返回的下载路径，比如: http://oss.xxx.com/avatar/3593964c85fd76f12971c82a411ef2a481c9c711.jpg
            fileURI = objectStoreService.uploadFile(filePath, file);
            newUser.setWorkerId(worker.getPid());
        } else if(newUser.getUserType().name().equals("hotel")){
            Company company = new Company();
            company.setStatus(0);
            company.setLeaderMobile(register.getMobile());
            company.setCompanyType(1);
            companyMapper.insert(company);
            file = QRCodeUtil.createQRCode (company.getPid ()+"WGB"+register.getUserType());
            filePath = "QRCode".toLowerCase() + "/" + FileUtil.fileNameReplaceSHA1(file);
            //文件上传成功后返回的下载路径，比如: http://oss.xxx.com/avatar/3593964c85fd76f12971c82a411ef2a481c9c711.jpg
            fileURI = objectStoreService.uploadFile(filePath, file);
        }else if(newUser.getUserType().name().equals("hr")){
            Company company = new Company();
            company.setStatus(0);
            company.setLeaderMobile(register.getMobile());
            company.setCompanyType(2);
            companyMapper.insert(company);
            file = QRCodeUtil.createQRCode (company.getPid ()+"WGB"+register.getUserType());
            filePath = "QRCode".toLowerCase() + "/" + FileUtil.fileNameReplaceSHA1(file);
            //文件上传成功后返回的下载路径，比如: http://oss.xxx.com/avatar/3593964c85fd76f12971c82a411ef2a481c9c711.jpg
            fileURI = objectStoreService.uploadFile(filePath, file);
        }
        //存入用户
        newUser.setQrCode (fileURI);
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
        if(userDTO.getBirthdayNew ()!=null){
            Integer year = Integer.parseInt (userDTO.getBirthdayNew ().split (" ")[0].split ("-")[0]);
            Integer mouth = Integer.parseInt (userDTO.getBirthdayNew ().split (" ")[0].split ("-")[1]);
            Integer day = Integer.parseInt (userDTO.getBirthdayNew ().split (" ")[0].split ("-")[2]);
            Integer h =  Integer.parseInt (userDTO.getBirthdayNew ().split (" ")[1].split (":")[0]);
            Integer m =  Integer.parseInt (userDTO.getBirthdayNew ().split (" ")[1].split (":")[1]);
            Integer s =  Integer.parseInt (userDTO.getBirthdayNew ().split (" ")[1].split (":")[2]);
            userDTO.setBirthday (OffsetDateTime.of (year,mouth,day,h,m,s,0, ZoneOffset.UTC));
            System.out.println ("brithday:"+userDTO.getBirthday ());
        }
        com.microdev.common.context.User loginUser = ServiceContextHolder.getServiceContext().getUser();
        User user = userMapper.queryByUserId(loginUser.getId());
        try {
            userConverter.update(userDTO, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        userMapper.updateById(user);
        if(user.getUserType () == UserType.worker){
            Worker worker = new Worker ();
            if(userDTO.getHealthCard ()!=null && userDTO.getHealthCard ()!="")worker.setHealthCard (userDTO.getHealthCard ());
            if(userDTO.getIdCardBack ()!=null && userDTO.getIdCardBack ()!="")worker.setIdcardBack (userDTO.getIdCardBack());
            if(userDTO.getIdCardFront ()!=null && userDTO.getIdCardFront ()!="")worker.setIdcardFront (userDTO.getIdCardFront ());
            if(userDTO.getIdCardNumber ()!=null && userDTO.getIdCardNumber ()!="")worker.setIdcardNumber (userDTO.getIdCardNumber ());
            worker.setPid (user.getWorkerId ());
            workerMapper.updateById (worker);
        }else if(user.getUserType () == UserType.hotel || user.getUserType () == UserType.hr) {
            Company company = companyMapper.findFirstByLeaderMobile (user.getMobile ());
            if(userDTO.getNickname ()!=null && userDTO.getNickname ()!="")company.setLeader (userDTO.getNickname ());
            if(userDTO.getCompany ()!=null){
                if(userDTO.getCompany ().getName ()!=null && userDTO.getCompany ().getName ()!="")company.setName (userDTO.getCompany ().getName ());
                if(userDTO.getCompany ().getBusinessLicense ()!=null && userDTO.getCompany ().getBusinessLicense ()!="")company.setBusinessLicense (userDTO.getCompany ().getBusinessLicense ());
                if(userDTO.getCompany ().getLogo ()!=null && userDTO.getCompany ().getLogo ()!="")company.setLogo (userDTO.getCompany ().getLogo ());
                if(userDTO.getCompany ().getLaborDispatchCard ()!=null && userDTO.getCompany ().getLaborDispatchCard ()!="")company.setLaborDispatchCard (userDTO.getCompany ().getLaborDispatchCard ());
                if(userDTO.getCompany ().getArea ()!=null && userDTO.getCompany ().getArea ()!=""){
                    System.out.println ("userDTO:"+userDTO);
                    company.setAddress (userDTO.getCompany ().getAddress ());
                    company.setArea (userDTO.getCompany ().getArea ());
                    company.setAddressCode (userDTO.getCompany ().getAddressCode ());
                    company.setLatitude (userDTO.getCompany ().getLatitude ());
                    company.setLongitude (userDTO.getCompany ().getLongitude ());
                }
            }
            companyMapper.updateAllColumnById (company);
        }
    }
    /**
     * 修改手机号
     */
    @Override
    public void modifyMobile(String newMobile, String newMobileSmsCode) {
        if (userMapper.findByMobile(newMobile) != null) {
            throw new AuthenticationException("该手机号码已被绑定");
        }
        smsFacade.checkSmsCode(newMobile, SmsType.reset_password.name(), newMobileSmsCode);
        com.microdev.common.context.User loginUser = ServiceContextHolder.getServiceContext().getUser();
        User user = userMapper.queryByUserId(loginUser.getId());
        if(user.getUserType ()==UserType.hotel || user.getUserType ()==UserType.hr){
            Company company = companyMapper.findFirstByLeaderMobile (user.getMobile ());
            company.setLeaderMobile (newMobile);
            companyMapper.updateById (company);
        }
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
        User user1 = userMapper.queryByUserId(user.getId());
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getString("username"));
        userDTO.setNickname(user.getString("nickName"));
        userDTO.setAvatar( user1.getAvatar());
        userDTO.setAge (user1.getAge ());
        userDTO.setQrCode (user1.getQrCode ());
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

        if (userType == UserType.hr || userType == UserType.hotel) {
            Company company = companyMapper.findFirstByLeaderMobile(mobile);
            if (company != null) {
                CompanyDTO companyDTO = new CompanyViewModelDTO();
                companyDTO.setId(company.getPid());
                companyDTO.setName(company.getName());
                companyDTO.setCompanyType(company.getCompanyType());
                companyDTO.setStatus(company.getStatus());
                companyDTO.setBusinessLicense (company.getBusinessLicense ());
                companyDTO.setLeader (company.getLeader ());
                companyDTO.setLeaderMobile (company.getLeaderMobile ());
                companyDTO.setAddress (company.getAddress ());
                companyDTO.setLogo (company.getLogo ());
                companyDTO.setLatitude (company.getLatitude ());
                companyDTO.setLongitude (companyDTO.getLongitude ());
                companyDTO.setLaborDispatchCard (company.getLaborDispatchCard ());
                companyDTO.setArea (company.getArea ());
                companyDTO.setAddressCode (company.getAddressCode ());
                userDTO.setCompany(companyDTO);
                List l1 = dictMapper.queryTypeByUserId (company.getPid ());
                List l2 = dictService.findServiceArea(company.getPid ());
				userDTO.setServiceType (l1 == null?new ArrayList<>():l1);
				userDTO.setAreaCode (l2 == null?new ArrayList<>():l2);

            }
        } else if (userType == UserType.worker) {
            String workerId = user.getString("workerId");
            Worker worker = workerMapper.queryById(workerId);
            userDTO.setWorkerId(workerId);
            userDTO.setIdCardNumber(worker.getIdcardNumber());
            userDTO.setHealthCard(worker.getHealthCard());
            userDTO.setIdCardFront(worker.getIdcardFront());
            userDTO.setIdCardBack(worker.getIdcardBack());
            List l1 = dictMapper.queryTypeByUserId (workerId);
            List l2 = dictService.findServiceArea(workerId);
            userDTO.setServiceType (l1==null?new ArrayList<>():l1);
            userDTO.setAreaCode (l2==null?new ArrayList<>():l2);
        }

        return userDTO;
    }

    /**
     * 查询未读的数量
     * @param id         用户id
     * @param type       用户类型worker，hotel，hr
     * @return
     */
    @Override
    public Map<String, Object> selectUnreadAmount(String id, String type) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(type)) {
            throw new ParamsException("参数错误");
        }
        Map<String, Object> totalMap = new HashMap<>();
        if ("hotel".equals(type)) {
            totalMap.put("curTask", taskService.selectUnReadAmount(id));
            totalMap.put("completeTask", taskService.selectCompleteAmount(id));
            totalMap.put("pendingTask", messageService.selectUnHandleMessageAmount(id, "3", 0));
            totalMap.put("message", messageService.selectMessageCount(id, "3", 0));
        } else if ("hr".equals(type)) {
            totalMap.put("pendingTask", messageService.selectUnHandleMessageAmount(id, "2", 0));
            totalMap.put("message", messageService.selectMessageCount(id, "2", 0));
            totalMap.put("curTask", taskHrCompanyService.selectUnreadCount(id));
            totalMap.put("completeTask", taskHrCompanyService.selectCompleteCount(id));
        } else if ("worker".equals(type)) {
            totalMap.put("pendingTask", messageService.selectUnHandleMessageAmount(id, "1", 0));
            totalMap.put("message", messageService.selectMessageCount(id, "1", 0));
            totalMap.put("curTask", taskWorkerService.selectUnreadCount(id));
            totalMap.put("completeTask", taskWorkerService.selectCompleteCount(id));
            List<Integer> list = workerLogMapper.selectUnreadPunchCount();
            if (list == null) {
                totalMap.put("supplement", 0);
            } else {
                totalMap.put("supplement", list.size());
            }

        } else {
            throw new ParamsException("用户类型错误");
        }
        return totalMap;
    }

}
