package com.microdev.service.impl;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.context.ServiceContextHolder;
import com.microdev.common.exception.AuthenticationException;
import com.microdev.common.exception.AuthorizationException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.oss.ObjectStoreService;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.*;
import com.microdev.converter.UserConverter;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.service.*;
import com.microdev.type.PlatformType;
import com.microdev.type.SocialType;
import com.microdev.type.UserSex;
import com.microdev.type.UserType;
import io.swagger.client.model.NewPassword;
import io.swagger.client.model.RegisterUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private DictMapper dictMapper;
    @Autowired
    private ObjectStoreService objectStoreService;
    @Autowired
    DictService dictService;
    @Autowired
    FeedBackMapper feedBackMapper;
    @Autowired
    VersionMapper versionMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private JpushClient jpushClient;
    @Autowired
    private PropagandaMapper propagandaMapper;
    @Autowired
    private IMUserService iMUserService;
    @Autowired
    private IMOperateService iMOperateService;

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
    public ResultDO registerchild(accountParam userDTO) throws Exception {
        System.out.println (userDTO);
        if(userDTO.getAccountType ()==null || userDTO.getCompanyId () == null || userDTO.getMobile () == null || userDTO.getPassword () == null){
            throw new ParamsException ("参数错误");
        }
        Company company = companyMapper.findCompanyById (userDTO.getCompanyId ());
        if(company == null){
            throw new ParamsException ("companyId错误");
        }
        User u = userMapper.findByMobile (company.getLeaderMobile ());
        User nu = u;
        nu.setUsername (userDTO.getMobile ());
        nu.setMobile (userDTO.getMobile ());
        nu.setPassword (userDTO.getPassword ());
        nu.setSuperior (u.getPid ());
        nu.setNickname (userDTO.getMobile ());
        nu.setPid (null);
        nu.setCreateTime (null);
        nu.setModifyTime (null);
        userMapper.insert (nu);

        //注册IM用户
        io.swagger.client.model.User user = new io.swagger.client.model.User().username(nu.getPid()).password(nu.getPid());
        RegisterUsers users = new RegisterUsers();
        users.add(user);
        iMUserService.createNewIMUserSingle(users);
        return ResultDO.buildSuccess ("注册成功");
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
    public ResultDO login(UserDTO login) throws Exception {
        System.out.println (login);
        User user = userMapper.findByMobile(login.getMobile());
        if(user == null){
            return ResultDO.buildError ("用户不存在");
        }

        UserDTO userDTO = new UserDTO();
        if(login.getPlatform () == PlatformType.PC){
            if(user.getUserType () == UserType.worker){
                return ResultDO.buildError ("该用户为小时工，无权限登录");
            }
        }
        userDTO.setId(user.getPid());
        userDTO.setNickname(user.getNickname());
        userDTO.setRoleList(new ArrayList<>(user.getRoles()));
        userDTO.setUserType(user.getUserType());

        if (!PasswordHash.validatePassword(login.getPassword(), user.getPassword())){
            return ResultDO.buildError ("用户名或密码错误");
        }
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String value = operations.get (login.getMobile ());
        if(value != null && !value.equals (login.getUniqueId ())){
            if(user.getUserType () == UserType.worker){
                try {
                    jpushClient.jC.sendPush (JPushManage.buildPushObject_all_message (login.getMobile (),login.getUniqueId ()));
                } catch (APIConnectionException e) {
                    e.printStackTrace ( );
                } catch (APIRequestException e) {
                    e.printStackTrace ( );
                }
            }
        }

        if (user != null && PasswordHash.validatePassword(login.getPassword(), user.getPassword())) {
            operations.set(user.getMobile (), login.getUniqueId ());
            return ResultDO.buildSuccess (tokenService.accessToken(userDTO, login.getPlatform().name()));
        }
        return ResultDO.buildError ("用户不存在");

    }

    @Override
    public ResultDO register(UserDTO register) throws Exception{
        if (register.getUserType() == UserType.platform) {
            return ResultDO.buildError ("无权限注册该用户");
        }
        smsFacade.checkSmsCode(register.getMobile(), SmsType.register.name(), register.getSmsCode());
        if (userMapper.findByMobile(register.getMobile()) != null) {
            return ResultDO.buildError ("手机号码已经存在");
        }
        File file;
        String fileURI = null;
        String filePath;

        User newUser = new User();
        newUser.setUserType(register.getUserType());
        newUser.setMobile(register.getMobile());
        newUser.setNickname (register.getMobile());
        newUser.setUserCode(register.getUserType()
                .toString());
        newUser.setSex (UserSex.UNKNOW);
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
            worker.setStatus (0);
            worker.setQrCode (fileURI);
            worker.setBindCompanys (true);
            worker.setActiveCompanys (0);
            worker.setHandheldIdentity (register.getHandheldIdentity ());
            worker.setStature (register.getStature ());
            worker.setWeight (register.getWeight ());
            worker.setEducation (register.getEducation ());
            if(register.getTgCode ()!=null && !register.getTgCode ().equals ("")){
                worker.setPollCode (register.getTgCode ());
                Propaganda pa = propagandaMapper.selectById (register.getTgCode ());
                if(pa == null){
                    pa = new Propaganda ();
                    pa.setId (register.getTgCode ());
                    pa.setHr (0);
                    pa.setHotel (0);
                    pa.setWorker (1);
                    pa.setTotal (1);
                    try{
                        pa.setLeader (register.getTgCode ().substring (0,4));
                    }catch(Exception e){
                        e.printStackTrace ();
                        return ResultDO.buildError ("邀请码无效");
                    }
                    propagandaMapper.insert (pa);
                }else{
                    pa.setTotal (pa.getTotal ()+1);
                    pa.setWorker (pa.getWorker ()+1);
                    propagandaMapper.updateById (pa);
                }
            }
            workerMapper.updateById (worker);
            newUser.setWorkerId(worker.getPid());
        } else if(newUser.getUserType().name().equals("hotel")){
            Company company = new Company();
            company.setStatus(0);
            company.setLeader (newUser.getNickname ());
            company.setLeaderMobile(register.getMobile());
            company.setName ("wgb"+UUID.randomUUID ().toString ().toLowerCase ().substring (1,7));
            company.setCompanyType(1);
            company.setBindWorkers (true);
            company.setBindCompanys (true);
            company.setActiveWorkers (0);
            company.setActiveCompanys (0);
            companyMapper.insert(company);
            file = QRCodeUtil.createQRCode (company.getPid ()+"WGB"+register.getUserType());
            filePath = "QRCode".toLowerCase() + "/" + FileUtil.fileNameReplaceSHA1(file);
            //文件上传成功后返回的下载路径，比如: http://oss.xxx.com/avatar/3593964c85fd76f12971c82a411ef2a481c9c711.jpg
            fileURI = objectStoreService.uploadFile(filePath, file);
            company.setQrCode (fileURI);
            if(register.getTgCode ()!=null && !register.getTgCode ().equals ("")){
                company.setPollCode (register.getTgCode ());
                Propaganda pa = propagandaMapper.selectById (register.getTgCode ());
                if(pa == null){
                    pa = new Propaganda ();
                    pa.setId (register.getTgCode ());
                    pa.setHr (0);
                    pa.setHotel (1);
                    pa.setWorker (0);
                    pa.setTotal (1);
                    try{
                        pa.setLeader (register.getTgCode ().substring (0,4));
                    }catch(Exception e){
                        e.printStackTrace ();
                        return ResultDO.buildError ("邀请码无效");
                    }
                    propagandaMapper.insert (pa);
                }else{
                    pa.setTotal (pa.getTotal ()+1);
                    pa.setHotel (pa.getHotel ()+1);
                    propagandaMapper.updateById (pa);
                }
            }
            companyMapper.updateById (company);
        }else if(newUser.getUserType().name().equals("hr")){
            Company company = new Company();
            company.setStatus(0);
            company.setLeaderMobile(register.getMobile());
            company.setLeader (newUser.getNickname ());
            company.setCompanyType(2);
            company.setName ("wgb"+UUID.randomUUID ().toString ().toLowerCase ().substring (1,7));
            company.setBindWorkers (true);
            company.setBindCompanys (true);
            company.setActiveWorkers (0);
            company.setActiveCompanys (0);
            companyMapper.insert(company);
            file = QRCodeUtil.createQRCode (company.getPid ()+"WGB"+register.getUserType());
            filePath = "QRCode".toLowerCase() + "/" + FileUtil.fileNameReplaceSHA1(file);
            //文件上传成功后返回的下载路径，比如: http://oss.xxx.com/avatar/3593964c85fd76f12971c82a411ef2a481c9c711.jpg
            fileURI = objectStoreService.uploadFile(filePath, file);
            company.setQrCode (fileURI);
            if(register.getTgCode ()!=null && !register.getTgCode ().equals ("")){
                company.setPollCode (register.getTgCode ());
                Propaganda pa = propagandaMapper.selectById (register.getTgCode ());
                if(pa == null){
                    pa = new Propaganda ();
                    pa.setId (register.getTgCode ());
                    pa.setHr (1);
                    pa.setHotel (0);
                    pa.setWorker (0);
                    pa.setTotal (1);
                    try{
                        pa.setLeader (register.getTgCode ().substring (0,4));
                    }catch(Exception e){
                        e.printStackTrace ();
                        return ResultDO.buildError ("邀请码无效");
                    }
                    propagandaMapper.insert (pa);
                }else{
                    pa.setTotal (pa.getTotal ()+1);
                    pa.setHr (pa.getHr ()+1);
                    propagandaMapper.updateById (pa);
                }
            }
            companyMapper.updateById (company);
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
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set(register.getMobile (), register.getUniqueId ());
        TokenDTO token = tokenService.accessToken(userDTO, register.getPlatform().name());

        //注册IM用户
        io.swagger.client.model.User user = new io.swagger.client.model.User().username(newUser.getPid()).password(newUser.getPid());
        RegisterUsers users = new RegisterUsers();
        users.add(user);
        iMUserService.createNewIMUserSingle(users);
        return ResultDO.buildSuccess (token);
    }
    /**
     * 注销登录
     */
    @Override
    public ResultDO logout(String mobile) {
        HttpServletRequest request = ServiceContextHolder.getServiceContext().getHttpServletRequest();
        String token = TokenUtil.parseBearerToken(request);
        tokenService.deleteAccessToken(token);
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set(mobile, "");

        return ResultDO.buildSuccess ("用户退出成功");
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
            Worker worker = workerMapper.queryById (user.getWorkerId ());
            if(userDTO.getHealthCard ()!=null && userDTO.getHealthCard ()!="")worker.setHealthCard (userDTO.getHealthCard ());
            if(userDTO.getIdCardBack ()!=null && userDTO.getIdCardBack ()!="")worker.setIdcardBack (userDTO.getIdCardBack());
            if(userDTO.getIdCardFront ()!=null && userDTO.getIdCardFront ()!="")worker.setIdcardFront (userDTO.getIdCardFront ());
            if(userDTO.getIdCardNumber ()!=null && userDTO.getIdCardNumber ()!="")worker.setIdcardNumber (userDTO.getIdCardNumber ());
            if(userDTO.getHandheldIdentity ()!=null && userDTO.getHandheldIdentity ()!="")worker.setHandheldIdentity  (userDTO.getHandheldIdentity ());
            if(userDTO.getStature ()!=null)worker.setStature  (userDTO.getStature ());
            if(userDTO.getWeight ()!=null)worker.setWeight (userDTO.getWeight ());
            if(userDTO.getEducation ()!=null)worker.setEducation (userDTO.getEducation ());

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
                    company.setAddress (userDTO.getCompany ().getAddress ());
                    company.setArea (userDTO.getCompany ().getArea ());
                    company.setAddressCode (userDTO.getCompany ().getAddressCode ());
                    company.setLatitude (userDTO.getCompany ().getLatitude ());
                    company.setLongitude (userDTO.getCompany ().getLongitude ());
                }
            }

            companyMapper.updateById (company);
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
        //昵称
        String nickName = user1.getNickname();
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getString("username"));
        userDTO.setNickname(user.getString("nickName"));
        userDTO.setAvatar( user1.getAvatar());
        userDTO.setAge (user1.getAge ());
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

        //如果当前的是用人单位或者人力公司，那么就查询出他们的公司审核状态

        if (userType == UserType.hr || userType == UserType.hotel) {
            Company company = companyMapper.findFirstByLeaderMobile(mobile);
            if (company != null) {
                nickName = company.getName();
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
                if(userType == UserType.hr){
                    companyDTO.setGrade (companyMapper.queryGrade(company.getPid (),"hr")+"");
                }else if (userType == UserType.hotel){
                    companyDTO.setGrade (companyMapper.queryGrade(company.getPid (),"hotel")+"");
                }
                userDTO.setCompany(companyDTO);
                List l1 = dictMapper.queryTypeByUserId (company.getPid ());
                List l2 = dictService.findServiceArea(company.getPid ());
				userDTO.setServiceType (l1 == null?new ArrayList<>():l1);
				userDTO.setAreaCode (l2 == null?new ArrayList<>():l2);
                userDTO.setQrCode (company.getQrCode ());
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
            userDTO.setQrCode (worker.getQrCode ());
        }

        //判断用户是否注册IM用户
        Object obj = iMUserService.getIMUserByUserName(user1.getPid());
        if (obj == null) {
            io.swagger.client.model.User u = new io.swagger.client.model.User().username(user1.getPid()).password(user1.getPid());
            RegisterUsers users = new RegisterUsers();
            users.add(u);
            iMUserService.createNewIMUserSingle(users);
        }

        iMOperateService.modifyUserNickName(user1.getPid(), nickName);
        return userDTO;
    }

    @Override
    public ResultDO feedbackQuery(Paginator paginator, FeedbackQueryDTO feedbackQueryDTO) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<FeedBack> list = userMapper.queryFreeback(feedbackQueryDTO);
        PageInfo<FeedBack> pageInfo = new PageInfo<FeedBack>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",list);
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess(result);
    }

    @Override
    public ResultDO feedbackInsert(FeedBackParam request) throws Exception{
        User user = userMapper.queryByUserId (request.getUserId ());
        if(user == null){
            throw new Exception ("用户不存在");
        }
        FeedBack feedBack = new FeedBack ();
        feedBack.setUserId (request.getUserId ());
        feedBack.setContent (request.getContent ());
        feedBack.setUserType (user.getUserType ());
        feedBackMapper.insert (feedBack);
        return ResultDO.buildSuccess ("反馈成功");
    }

}
