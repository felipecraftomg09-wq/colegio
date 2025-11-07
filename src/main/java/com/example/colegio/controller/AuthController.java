package com.example.colegio.controller;

import com.example.colegio.dto.LoginRequest;
import com.example.colegio.model.Usuario;
import com.example.colegio.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*()_+=-]).{8,}$");

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{3,}$");

    // login
    @GetMapping("/login")
    public String loginForm() {
        return "sesiones/login";
    }

    @PostMapping("/login")
    public String procesarLogin(@ModelAttribute LoginRequest request, Model model, HttpSession session) {
        if (!request.getEmail().endsWith("@colegio.ejemplar")) {
            model.addAttribute("error", "Debe usar un correo institucional (@colegio.ejemplar)");
            return "sesiones/login";
        }

        Usuario u = authService.login(request.getEmail(), request.getPassword());
        if (u == null) {
            model.addAttribute("error", "Correo o contraseña incorrectos");
            return "sesiones/login";
        }

        session.setAttribute("usuarioLogueado", u);
        return (u.getRol() == Usuario.Rol.ALUMNO)
                ? "redirect:/login/alumno"
                : "redirect:/login/docente";
    }

    // reguistro
    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("roles", Usuario.Rol.values());
        return "sesiones/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @RequestParam String email,
            @RequestParam String contrasena,
            @RequestParam String rol,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false) String grado,
            @RequestParam(required = false) String especialidad,
            Model model,
            HttpSession session) {

        model.addAttribute("roles", Usuario.Rol.values());

        // Validar correo
        if (!email.endsWith("@colegio.ejemplar")) {
            model.addAttribute("error", "El correo debe terminar en @colegio.ejemplar");
            return "sesiones/registro";
        }

        // Validar nombre/apellido
        if (nombre == null || !NAME_PATTERN.matcher(nombre).matches()) {
            model.addAttribute("error", "El nombre debe tener al menos 3 letras y solo caracteres válidos");
            return "sesiones/registro";
        }
        if (apellido == null || !NAME_PATTERN.matcher(apellido).matches()) {
            model.addAttribute("error", "El apellido debe tener al menos 3 letras y solo caracteres válidos");
            return "sesiones/registro";
        }

        // Validar contraseña
        if (!PASSWORD_PATTERN.matcher(contrasena).matches()) {
            model.addAttribute("error",
                    "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un símbolo");
            return "sesiones/registro";
        }

        // Validar rol
        Usuario.Rol usuarioRol;
        try {
            usuarioRol = Usuario.Rol.valueOf(rol);
        } catch (Exception ex) {
            model.addAttribute("error", "Rol inválido");
            return "sesiones/registro";
        }

        // Evitar duplicados
        if (authService.existeEmail(email)) {
            model.addAttribute("error", "Ya existe un usuario con ese correo");
            return "sesiones/registro";
        }

        // Crear usuario
        Usuario nuevo = authService.registrarUsuario(email, contrasena, usuarioRol);

        if (usuarioRol == Usuario.Rol.ALUMNO) {
            if (grado == null || grado.trim().isEmpty()) {
                model.addAttribute("error", "Debe indicar el grado del alumno");
                return "sesiones/registro";
            }
            authService.crearAlumno(nombre, apellido, grado, nuevo);
        } else {
            if (especialidad == null || especialidad.trim().isEmpty()) {
                model.addAttribute("error", "Debe indicar la especialidad del docente");
                return "sesiones/registro";
            }
            authService.crearDocente(nombre, apellido, especialidad, nuevo);
        }

        // Sesión automática
        session.setAttribute("usuarioLogueado", nuevo);
        return (usuarioRol == Usuario.Rol.ALUMNO)
                ? "redirect:/login/alumno"
                : "redirect:/login/docente";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
