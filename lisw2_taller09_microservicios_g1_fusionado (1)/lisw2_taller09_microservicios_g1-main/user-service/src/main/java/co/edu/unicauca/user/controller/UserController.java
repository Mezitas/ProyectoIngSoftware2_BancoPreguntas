package co.edu.unicauca.user.controller;

import co.edu.unicauca.user.dto.UserDTO;
import co.edu.unicauca.user.service.UserService;
import org.springframework.web.bind.annotation.*;

/** Consulta de usuarios y roles; la usan Question Service y Review Service (REST síncrono). */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserDTO findById(@PathVariable Long id) {
        return userService.findById(id);
    }
}
