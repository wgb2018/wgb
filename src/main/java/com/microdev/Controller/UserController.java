package com.microdev.Controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.Constant;
import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.oss.ObjectStoreService;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.FileUtil;
import com.microdev.common.utils.HtmlUtil;
import com.microdev.common.utils.QRCodeUtil;
import com.microdev.mapper.*;
import com.microdev.model.Bill;
import com.microdev.model.MyTimeTask;
import com.microdev.model.Task;
import com.microdev.model.User;
import com.microdev.param.*;
import com.microdev.service.SmsFacade;
import com.microdev.service.TokenService;
import com.microdev.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.*;

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
	@Autowired
    SmsFacade smsFacade;
	@Autowired
    TaskMapper taskMapper;
	@Autowired
    VersionMapper versionMapper;
	@Autowired
    WorkerMapper workerMapper;
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
    @GetMapping("/logout")
    public ResultDO logout() throws Exception {
        return userService.logout();
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
    @PostMapping ("/base-info")
    public ResultDO modifyBaseInfo(@RequestBody UserDTO userDTO) {
        System.out.println ("userDTO:"+userDTO);
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
    @PostMapping("/{mobile}/mobiles/{smsCode}")
    public ResultDO modifyBaseInfo(@PathVariable String mobile, @PathVariable String smsCode) {
        userService.modifyMobile(mobile, smsCode);
        return ResultDO.buildSuccess("修改成功");
    }
    /**
     * 刷新token
     * 客户端 token 到期时，在不打扰用户的情况下重新获取 token
     */
    @GetMapping("/oauth/refresh-token/{refreshToken}")
    public ResultDO refreshTokenPost(@PathVariable String refreshToken) throws Exception{
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
     * 上传文件到服务器，比如：用户图像、营业执照 等等
     */
    /*@GetMapping("/{fileType}/files/{fileAddress}")*/
    //@GetMapping("/files")
    @RequestMapping(value = "/files", method = RequestMethod.POST, consumes = "multipart/form-data",name="fileRequest")
    public ResultDO uploadFile1(MultipartFile file) throws Exception {
        //File file = new File("C:\\D盘\\test.txt");
       // System.out.println (file);
        String filePath = "aaa".toLowerCase() + "/" + FileUtil.fileNameReplaceSHA1(file);

        //文件上传成功后返回的下载路径，比如: http://oss.xxx.com/avatar/3593964c85fd76f12971c82a411ef2a481c9c711.jpg
        String fileURI = objectStoreService.uploadObject (filePath, file.getBytes ());


        //返回地址给前端
        return ResultDO.buildSuccess("上传文件成功", fileURI);
    }
    @PostMapping("/files/{fileType}")
    public ResultDO uploadFile(@RequestParam("file") MultipartFile file) throws Exception {

        String filePath = "avater".toLowerCase() + "/" + FileUtil.fileNameReplaceSHA1(file);

        //文件上传成功后返回的下载路径，比如: http://oss.xxx.com/avatar/3593964c85fd76f12971c82a411ef2a481c9c711.jpg
        String fileURI = objectStoreService.uploadObject(filePath, file.getBytes());


        //返回地址给前端
        return ResultDO.buildSuccess("上传文件成功", fileURI);
    }
    @GetMapping("/user/test")
    public ResultDO test() throws Exception{
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
       /* File file;
        String fileURI = null;
        String filePath;
        file = QRCodeUtil.createQRCode ("3a267b284a1641ed9fb143fb3ff2d6c5WGBhotel");
        filePath = "QRCode".toLowerCase() + "/" + FileUtil.fileNameReplaceSHA1(file);
        //文件上传成功后返回的下载路径，比如: http://oss.xxx.com/avatar/3593964c85fd76f12971c82a411ef2a481c9c711.jpg
        fileURI = objectStoreService.uploadFile(filePath, file);

        System.out.println ("fileURI:"+fileURI);*/
        /*OffsetDateTime of = OffsetDateTime. ofInstant (Instant.ofEpochMilli (new Date().getTime ()),ZoneOffset.systemDefault ());
        System.out.println (of);
        System.out.println (new Date().getTime ());
        User test = userMapper.findByMobile("15601173951");
        test.setDeleted(false);
        userMapper.updateById(test);
        OffsetDateTime of = OffsetDateTime.now ();
        System.out.println (OffsetDateTime.of (of.getYear (),of.getMonthValue (),of.getDayOfMonth (),0,0,0,0,ZoneOffset.ofHoursMinutes (0,0)).plusDays (1).getHour ());
        Task t = taskMapper.getFirstById ("0af4cbe6642b4d19a7d1b8de07b9a456");
        System.out.println ("task:"+t);*/


       /* MyTimeTask my = new MyTimeTask (OffsetDateTime.now()+"","");
        java.util.Timer timer = new Timer(true);
        timer.schedule(my, OffsetDateTime.now ().getLong (ChronoField.SECOND_OF_DAY));*/
        return ResultDO.buildSuccess("1");
    }
	@GetMapping("/{mobile}/verifyMobile/{smsCode}")
    public ResultDO verifyMobile(@PathVariable String mobile, @PathVariable String smsCode) {
        smsFacade.checkSmsCode(mobile, SmsType.register.name(), smsCode);
        return ResultDO.buildSuccess("验证通过");
    }

    /**
     * 文件下载
     */
    @GetMapping("/protocoldownload/{param}")
    public void fileDownload(@PathVariable String param, HttpServletResponse response) throws IOException {
        OutputStream out = response.getOutputStream();
        String  path = getClass().getResource("/").getFile();
        path = URLDecoder.decode(path,  "utf-8");
        File f = null;
        if ("1".equals(param)) {
            f = new File( path, File.separator + "static" + File.separator +  "UserProtocolAndPrivacyClause.html");
        } else if ("2".equals(param)) {
            f = new File(path, File.separator + "static" + File.separator + "IntroductionOfNewFunctions.txt");
        } else if ("3".equals(param)) {
            f = new File(path + File.separator + "static" + File.separator + "CooperationBetweenHumanCompaniesAndHotels.txt");
        } else if ("4".equals(param)) {
            f = new File(path + File.separator + "static" + File.separator + "BindingsOfHRAndHourlyWorkers.txt");
        } else {
            throw new ParamsException("参数错误");
        }
        FileInputStream file = new FileInputStream(f);
        response.setContentType("text/html;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        byte[] b = new byte[1024];
        int len = 0;
        while ((len = file.read(b)) != -1) {
            out.write(b,0,len);
            out.flush();
        }
        file.close();
        out.close();

    }


    /**
     * 邀请注册
     */
    @GetMapping("/apply/register")
    public ResultDO applyRegister(ApplyRequest applyRequest) {
        Map<String, String> map = new LinkedHashMap <> ();
        map.put ("name",applyRequest.getName ());
        map.put ("address",applyRequest.getUrl ());
        smsFacade.sendSmsNotice (applyRequest.getMobile (),SmsType.apply_register,map);
        return ResultDO.buildSuccess ("发送成功");
    }
    /**
     * 请求地图数据
     */
    @GetMapping("/apply/bdMap")
    public ResultDO bdMap( String url) {
        //HttpServletRequest.
        return ResultDO.buildSuccess (null);
    }
    /**
     * 分页查询意见反馈
     */
    @PostMapping("/feedback/query")
    public ResultDO feedbackQuery( @RequestBody PagingDO<FeedbackQueryDTO> paging) {
        return userService.feedbackQuery(paging.getPaginator (),paging.getSelector ());
    }
    /**
     * 添加意见反馈
     */
    @PostMapping("/feedback/insert")
    public ResultDO feedbackInsert( @RequestBody FeedBackParam request) throws Exception{
        return ResultDO.buildSuccess (userService.feedbackInsert(request));
    }

    /**
     * 版本管理
     */
    @GetMapping("/check/version")
    public ResultDO checkVersion() throws Exception{
        return ResultDO.buildSuccess (versionMapper.selectVersion ());
    }


}
