package com.microdev.Controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.microdev.Constant;
import com.microdev.common.ResultDO;
import com.microdev.common.oss.ObjectStoreService;
import com.microdev.common.utils.FileUtil;
import com.microdev.mapper.DictMapper;
import com.microdev.mapper.UserMapper;
import com.microdev.model.User;
import com.microdev.param.ChangePwdRequest;
import com.microdev.param.SmsType;
import com.microdev.param.UserDTO;
import com.microdev.param.WeixinUserInfo;
import com.microdev.service.SmsFacade;
import com.microdev.service.TokenService;
import com.microdev.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户前台相关Api
 */
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private SmsFacade smsService;
    @Autowired
    private ObjectStoreService objectStoreService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    public UserMapper userMapper;
    @Autowired
    DictMapper dictMapper;
    /**
     * 创建用户
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/user/create")
    public ResultDO create(@RequestBody User User) throws Exception {
        return ResultDO.buildSuccess(userService.create(User));
    }
    /**
     * 用户名+密码登录
     */
    @PostMapping("/login")
    public ResultDO login(@RequestBody UserDTO user) throws Exception {
        return ResultDO.buildSuccess(userService.login(user));
    }
    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public ResultDO logout() throws Exception {
        userService.logout();
        return ResultDO.buildSuccess("用户退出成功");
    }
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResultDO changePwd(@RequestBody ChangePwdRequest request) throws Exception {
        return ResultDO.buildSuccess(userService.changePwd(request));
    }
    /**
     * 手机号+短信登录
     */
    @PostMapping("/login-sms")
    public ResultDO sms(@RequestBody UserDTO userDTO) {
        return ResultDO.buildSuccess(userService.smslogin(userDTO));
    }
    /**
     * 用户名注册-手机号+验证码注册
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public ResultDO register(@RequestBody UserDTO userDTO) throws Exception {
        return ResultDO.buildSuccess(userService.register(userDTO));
    }
    /**
     * 微信小程序用户注册
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register-weixin")
    public ResultDO register_weixin(@RequestBody WeixinUserInfo weixinUserInfo) throws Exception {
        return ResultDO.buildSuccess(userService.register_weixin(weixinUserInfo));
    }
    /**
     * 重置密码
     * 用户通过校验短信验证码重置密码
     */
    @PostMapping("/reset-pwd")
    public ResultDO resetPwd(@RequestBody UserDTO userDTO) throws Exception {
        userService.resetPwd(userDTO);
        return ResultDO.buildSuccess("重置成功");
    }
    /**
     * 修改基础信息
     * 昵称、邮箱
     */
    @PatchMapping("/base-info")
    public ResultDO modifyBaseInfo(@RequestBody UserDTO userDTO) {
        userService.modifyBaseInfo(userDTO);
        return ResultDO.buildSuccess("修改成功");
    }
    /**
     * 修改头像
     */
    @PatchMapping("/avatar")
    public ResultDO avatar(@RequestParam("file") MultipartFile file) throws Exception {

        //拼接存储到 OSS 中的路径， 比如：avatar/3593964c85fd76f12971c82a411ef2a481c9c711.jpg
        String filePath = Constant.oss_dir_avatar + FileUtil.fileNameReplaceSHA1(file);

        //文件上传成功后返回的下载路径，比如: http://oss.xxx.com/avatar/3593964c85fd76f12971c82a411ef2a481c9c711.jpg
        String fileURI = objectStoreService.uploadObject(filePath, file.getBytes());

        //修改用户头像地址
        UserDTO userDTO = new UserDTO();
        userDTO.setAvatar(fileURI);
        userService.modifyBaseInfo(userDTO);

        //返回新的头像地址给前端
        return ResultDO.buildSuccess(fileURI);
    }
    //发送短信验证码 用于登录、注册、找回密码等短信验证码的发送请求
    @GetMapping("/sms-codes/{smsType}/{mobile}")
    public ResultDO send(@PathVariable SmsType smsType, @PathVariable String mobile) {
        smsService.sendSmsCode(mobile, smsType);
        return ResultDO.buildSuccess("发送成功");
    }
    /**
     * 修改手机号
     */
    @PatchMapping("/mobiles/{mobile}/{smsCode}")
    public ResultDO modifyBaseInfo(@PathVariable String mobile, @PathVariable String smsCode) {
        userService.modifyMobile(mobile, smsCode);
        return ResultDO.buildSuccess("修改成功");
    }
    /**
     * 刷新token
     * 客户端 token 到期时，在不打扰用户的情况下重新获取 token
     */
    @GetMapping("/oauth/refresh-token/{refreshToken}")
    public ResultDO refreshTokenPost(@PathVariable String refreshToken) {
        return ResultDO.buildSuccess(tokenService.refreshToken(refreshToken));
    }
    /**
     * 返回用户账号信息
     * 该接口返回用户账号信息，但各种不同身份的特有信息需要补充调用其他接口
     */
    @GetMapping("/me")
    public ResultDO me() {
        return ResultDO.buildSuccess(userService.me());
    }
    /**
     * 上传文件
     * 上传文件到服务器，比如：用户图像、营业执照等等
     */
    @PostMapping("/files/{fileType}")
    public ResultDO uploadFile(@PathVariable String fileType, @RequestParam("file") MultipartFile file) throws Exception {

        String filePath = fileType.toLowerCase() + "/" + FileUtil.fileNameReplaceSHA1(file);

        //文件上传成功后返回的下载路径，比如: http://oss.xxx.com/avatar/3593964c85fd76f12971c82a411ef2a481c9c711.jpg
        String fileURI = objectStoreService.uploadObject(filePath, file.getBytes());


        //返回地址给前端
        return ResultDO.buildSuccess("上传文件成功", fileURI);
    }
    @GetMapping("/user/test")
    public ResultDO test(){
        User user = userService.selectById("0ffe5af3-21f2-4f87-bdec-c7bd1106db6f");
        //return ResultDO.buildSuccess(userService.selectList(new EntityWrapper<>(user)));
//        Map map = new HashMap<>();
//        map.put("id","0ffe5af3-21f2-4f87-bdec-c7bd1106db6f");
//        List<User> list = userService.selectByMap(map);
//        User User2 = new User();
//        User2.setPid("0ffe5af3-21f2-4f87-bdec-c7bd1106db6f");
//        Wrapper<User> et = new EntityWrapper<User>().where("id={0}","0ffe5af3-21f2-4f87-bdec-c7bd1106db6f");
//        //User user1 = userService.selectOne(User2);
//        User user1 = userMapper.selectOne(User2);
//        User test = new User();
//        test.setUsername("Test");
//        userMapper.insert(test);
//        test = userMapper.selectById("f1f33e09884c4b06b8fbe77465bd208d");
//        test.setUsername("TestNew");
//        userMapper.update(test);
//        test = userMapper.selectById("f1f33e09884c4b06b8fbe77465bd208d");
        User test = userMapper.findByMobile("15601173951");
        test.setDeleted(false);
        userMapper.updateById(test);
        return ResultDO.buildSuccess(dictMapper.findArea("110100"));
    }




}
