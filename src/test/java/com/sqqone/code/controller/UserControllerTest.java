import com.sqqone.code.controller.UserController;
import com.sqqone.code.entity.User;
import com.sqqone.code.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Before
    public void setUp() {
        mockMvc = standaloneSetup(userController).build();
    }

    @Test
    public void testRegister() throws Exception {
        User user = new User();
        user.setUserName("testuser");
        user.setNickName("aaaaaaaaaaaaaaaaa");
        user.setPassword("testpassword");
        user.setEmail("sqq2655@163.com");
        user.setSex("男");
        user.setRegistrationDate(new Date());
        user.setLatelyLoginTime(new Date());
        // 模拟 userService.findByUserName() 和 userService.findByEmail() 方法
        when(userService.findByUserName(user.getUserName())).thenReturn(null);
        when(userService.findByEmail(user.getEmail())).thenReturn(null);

        // 模拟 userService.save() 方法
        doNothing().when(userService).save(any(User.class));
        // 发起 POST 请求
        mockMvc.perform(MockMvcRequestBuilders.post("/user/register")
                .param("nickName",user.getNickName())
                .param("sex",user.getSex())
                .param("userName", user.getUserName())
                .param("password", user.getPassword())
                .param("email", user.getEmail()))

                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andDo(print());

        // 验证 userService.findByUserName() 和 userService.findByEmail() 方法被调用了一次
        verify(userService, times(1)).findByUserName(user.getUserName());
        verify(userService, times(1)).findByEmail(user.getEmail());

        // 验证 userService.save() 方法被调用了一次，并且参数为 user 对象
        verify(userService, times(1)).save(eq(user));
    }
}