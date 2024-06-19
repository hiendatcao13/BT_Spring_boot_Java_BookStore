package com.bookstore.controller;

import com.bookstore.entity.Book;
import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.services.EmailSenderService;
import com.bookstore.services.RoleServices;
import com.bookstore.services.UserServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
public class UserController {
    @Autowired
    private UserServices userServices;

    @Autowired
    private EmailSenderService senderService;

    @Autowired
    private RoleServices roleServices;

    @GetMapping("/users")
    public String userList(Model model){
        List<User> users = userServices.findAll();
        model.addAttribute("users", users);
        return "user/list";
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable("id") Long id, Model model){
        User user = userServices.findUserByUserId(id);
        if(user != null){
            model.addAttribute("user", user);
            model.addAttribute("roles", roleServices.getAllRoles());
            return "user/edit";
        }
        return "redirect:/users";
    }
    @PostMapping("/users/edit/{id}")
    public String handleEditUser(@PathVariable("id") Long id,@Valid @ModelAttribute("user") User user
            ,@RequestParam("roles")List<Long> roleIds, BindingResult result, Model model){
        if (result.hasErrors()) {
            model.addAttribute("roles", roleServices.getAllRoles());
            return "user/edit";
        }

        List<Role> roles = roleServices.getRoleByIds(roleIds);
        user.setRoles(roles);
        userServices.save(user);
        return "redirect:/users";
    }

    @GetMapping("/login")
    public String login(){
        return "user/login";
    }

    @GetMapping("registerGoogle")
    public String userDetails(@AuthenticationPrincipal OAuth2User oAuth2User, Model model) {
        String name = oAuth2User.getAttribute("name");
        System.out.println("Name: "+ name);
        String email = oAuth2User.getAttribute("email");
        User user = userServices.findUserByEmail(email);
        if(user != null){
            model.addAttribute("message", "Tài khoản email đã được đăng kí trước đó. Vui lòng nhấn đăng nhập");
            return "user/notification";
        }
        user = new User();
        user.setName(name);
        user.setEmail(email);
        model.addAttribute("user", user);
        return "user/register";
    }

    @GetMapping("/register")
    public String register(Model model){
        model.addAttribute("user", new User());
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            List<FieldError> errors = bindingResult.getFieldErrors();
            for(FieldError error : errors){
                model.addAttribute(error.getField() + "_error", error.getDefaultMessage());
            }
            return "user/register";
        }
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userServices.save(user);
        return "redirect:/login";
    }
    @GetMapping("/recovery")
    public String getRecoveryPass(){
        return "user/recovery";
    }
    @PostMapping("/recovery")
    public String checkRecovery(@RequestParam("email") String email, Model model){
        User user = userServices.findUserByEmail(email);
        if(user == null){
            model.addAttribute("message", "Tài khoản chưa được đăng kí. Nếu bạn cảm thấy điều này thật điên rồ " +
                    "vì đã đăng kí trước đó. Vui lòng gửi gmail về chúng tôi hiendatcao13@gmail.com");
            return "user/notification";
        }
        sendMail(email, user.getId().toString());
        model.addAttribute("message", "1 liên kết được gửi qua email bạn đăng kí. Vui lòng kiểm tra hộp thư" +
                "  hoặc có thể nằm trong thư mục spam :)))))");
        return "user/notification";
    }
    @GetMapping("/changePass/{token}")
    public String getChangePass(@PathVariable("token") String token, Model model){
        Long userId = getUserIdFromToken(token);
        User user = userServices.findUserByUserId(userId);
        if(user == null){
            model.addAttribute("message", "Yêu cầu trang lỗi. " +
                    "Vui lòng xem lại đường dẫn hoặc liên hệ quản trị viên Pino Dat");
            return "user/notification";
        }
        model.addAttribute("tokenCode", token);
        return "user/changePass";
    }
    @PostMapping("/changePass/{token}")
    public String postChangePass(@PathVariable("token") String token,
                                 @RequestParam("password") String password){
        Long userId = getUserIdFromToken(token);
        User user = userServices.findUserByUserId(userId);
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        userServices.save(user);
        return "redirect:/login";
    }

    private void sendMail(String toEmail, String userId){
        String token = createFunnyToken(userId);
        getUserIdFromToken(token);
        String http = "http://localhost:8080/changePass/" + token;
        senderService.sendEmail(toEmail,
                "Khôi phục mật khẩu bằng link.",
                "Đường link khôi phục của bạn là: " + http +
                        ". Vui lòng không cung cấp đường link cho bất kì ai!!!");
    }
    // Tạo token cho vui vui theo ý mình (vị trí thứ 3 lấy tới khi gặp F?U/PinowrdbDat)
    private String createFunnyToken(String userId){
        Random random = new Random();
        int length = 25; // Độ dài của chuỗi ngẫu nhiên
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String randomText = "";
        for(int i = 0; i < length; i++){
            int index = random.nextInt(characters.length());
            char randomChar = characters.charAt(index);
            randomText = randomText.concat(String.valueOf(randomChar));
        }
        randomText = randomText.substring(0, 3) + userId + "FUPinowrdbDat" + randomText.substring(3);
        return randomText;
    }
    // lấy mã userId từ mã token
    private Long getUserIdFromToken(String token){
        try{
            token = token.substring(3);
            int index = token.indexOf("FUPinowrdbDat");
            if(index == -1)
                return (long) -1;
            token = token.substring(0, index);
            return Long.parseLong(token);
        } catch (Exception ex){
            return (long) -1;
        }
    }
}
