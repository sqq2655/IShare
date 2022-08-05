package com.sqqone.code.controller.admin;

import com.sqqone.code.entity.User;
import com.sqqone.code.service.UserService;
import com.sqqone.code.util.Consts;
import com.sqqone.code.util.CryptographyUtil;
import com.sqqone.code.util.StringUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/28 10:20
 */
@Controller
@RequestMapping("/admin/user")
public class UserAdminController {
    @Autowired
    private UserService userService;


    private void setCurrentUser(Integer userId,HttpSession session){
        User user = (User) session.getAttribute(Consts.CURRENT_USER);
        if(userId==user.getUserId().intValue()){
            session.setAttribute(Consts.CURRENT_USER,userService.getUserById(userId));
        }
    }
    /**
     * 根据条件分页查询用户信息
     */
    @ResponseBody
    @RequestMapping(value = "/list")
    @RequiresPermissions(value = "分页查询用户信息")
    public Map<String, Object> list(User s_user, @RequestParam(value = "latelyLoginTimes", required = false) String latelyLoginTimes,
                                    @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        String s_blatelyLoginTime = null;           //开始时间
        String s_elatelyLoginTime = null;           //结束时间
        if (StringUtil.isNotEmpty(latelyLoginTimes)) {
            String[] strs = latelyLoginTimes.split(" - ");      //拆分时间段
            s_blatelyLoginTime = strs[0];
            s_elatelyLoginTime = strs[1];
        }
        Map<String, Object> map = new HashMap<>();
        map.put("data", userService.list(s_user, s_blatelyLoginTime, s_elatelyLoginTime, page, pageSize, Sort.Direction.DESC, "registrationDate"));
        map.put("total", userService.getCount(s_user, s_blatelyLoginTime, s_elatelyLoginTime));
        map.put("errorNo", 0);
        return map;
    }

    /**
     * 修改用户VIP状态
     */
    @ResponseBody
    @RequestMapping(value = "/updateVipState")
    @RequiresPermissions(value = "修改用户VIP状态")
    public Map<String, Object> updateVipState(Integer userId, boolean isVip,HttpSession session) {

        User oldUser = userService.getUserById(userId);
        oldUser.setVip(isVip);
        userService.save(oldUser);
        /*判断是否是当前用户*/
        setCurrentUser(userId,session);
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        return map;
    }

    /**
     * 修改用户状态
     */
    @ResponseBody
    @RequestMapping(value = "/updateUserState")
    @RequiresPermissions(value = "修改用户状态")
    public Map<String, Object> updateUserState(Integer userId, boolean isOff) {
        User oldUser = userService.getUserById(userId);
        oldUser.setOff(isOff);
        userService.save(oldUser);
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        return map;
    }

    /**
     * 重置用户密码
     */
    @ResponseBody
    @RequestMapping(value = "/resetPassword")
    @RequiresPermissions(value = "重置用户密码")
    public Map<String, Object> resetPassword(Integer userId) {
        User oldUser = userService.getUserById(userId);
        oldUser.setPassword(CryptographyUtil.md5("123456", CryptographyUtil.SALT));          //重置密码为123456
        userService.save(oldUser);
        Map<String, Object> map = new HashMap<>();
        map.put("errorNo", 0);
        return map;
    }

    /**
     * 用户vip等级修改
     */
    @ResponseBody
    @RequestMapping(value = "/updateVipGrade")
    @RequiresPermissions(value = "用户vip等级修改")
    public Map<String, Object> updateVipGrade(User user,HttpSession session) {
        User oldUser = userService.getUserById(user.getUserId());
        oldUser.setVipGrade(user.getVipGrade());
        userService.save(oldUser);
        setCurrentUser(oldUser.getUserId(),session);
        Map<String, Object> map = new HashMap<>();
        map.put("errorNo", 0);
        return map;
    }

    /**
     * 用户积分充值
     */
    @ResponseBody
    @RequestMapping(value = "/addPoints")
    @RequiresPermissions(value = "用户积分充值")
    public Map<String, Object> addPoints(User user,HttpSession session) {
        User oldUser = userService.getUserById(user.getUserId());
        oldUser.setPoints(oldUser.getPoints() + user.getPoints());
        userService.save(oldUser);
        setCurrentUser(oldUser.getUserId(),session);
        Map<String, Object> map = new HashMap<>();
        map.put("errorNo", 0);
        return map;
    }

    /**
     * 管理员自己的修改密码
     */
    @ResponseBody
    @RequiresPermissions(value = "修改管理员密码")
    @PostMapping("/modifyPassword")
    public Map<String, Object> modifyPassword(String oldPassword, String newPassword, HttpSession session) {
        User user = (User) session.getAttribute(Consts.CURRENT_USER);
        Map<String, Object> map = new HashMap<>();
        if (!user.getPassword().equals(CryptographyUtil.md5(oldPassword, CryptographyUtil.SALT))) {
            map.put("success", false);
            map.put("errorInfo", "原密码错误！");
            return map;
        }
        User oldUser = userService.getUserById(user.getUserId());
        oldUser.setPassword(CryptographyUtil.md5(newPassword, CryptographyUtil.SALT));
        userService.save(oldUser);
        map.put("success", true);
        return map;
    }

    /**
     * 安全退出
     */
    @GetMapping("/logout")
    @RequiresPermissions(value = "安全退出")
    public String logout() {
        SecurityUtils.getSubject().logout();
        return "redirect:/admin/login.html";
    }
}