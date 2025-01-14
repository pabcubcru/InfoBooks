package com.pabcubcru.bookdeal.controllers;

import java.security.Principal;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import com.pabcubcru.bookdeal.models.Authorities;
import com.pabcubcru.bookdeal.models.ProvinceEnum;
import com.pabcubcru.bookdeal.models.User;
import com.pabcubcru.bookdeal.services.AuthoritiesService;
import com.pabcubcru.bookdeal.services.BookService;
import com.pabcubcru.bookdeal.services.RequestService;
import com.pabcubcru.bookdeal.services.SearchService;
import com.pabcubcru.bookdeal.services.UserFavouriteBookService;
import com.pabcubcru.bookdeal.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private AuthoritiesService authoritiesService;

	@Autowired
	private BookService bookService;

	@Autowired
	private RequestService requestService;

	@Autowired
	private UserFavouriteBookService userFavouriteBookService;

	@Autowired
	private SearchService searchService;

	@GetMapping(value = { "/", "/profile", "/register", "/login", "/login-error", "/admin/dashboard" })
	public ModelAndView main() {
		ModelAndView model = new ModelAndView();
		model.setViewName("Main");
		return model;
	}

	@GetMapping(value = "/user/principal")
	public Map<String, Object> getPrincipal(Principal principal) {
		Map<String, Object> res = new HashMap<>();
		if (principal != null) {
			res.put("isLogged", true);
			Authorities auth = this.authoritiesService.findByUsername(principal.getName()).stream().findFirst()
					.orElse(null);
			if (auth != null && auth.getAuthority().equals("admin")) {
				res.put("isAdmin", true);
			} else {
				res.put("isAdmin", false);
			}
		} else {
			res.put("isLogged", false);
		}
		return res;
	}

	@GetMapping(value = "/provinces")
	public Map<String, Object> getProvinces() {
		Map<String, Object> res = new HashMap<>();

		res.put("provinces", ProvinceEnum.values());

		return res;
	}

	public Integer countCharacters(String cadena, String character) {
		int posicion, contador = 0;

		posicion = cadena.indexOf(character);
		while (posicion != -1) {
			contador++;
			posicion = cadena.indexOf(character, posicion + 1);
		}
		return contador;
	}

	private BindingResult validateUser(User user, BindingResult result, Boolean newUser) {
		if (newUser) {
			if (!user.getEmail().isEmpty()) {
				Boolean existEmail = this.userService.existUserWithSameEmail(user.getEmail());
				if (existEmail) {
					result.rejectValue("email", "Este correo electrónico ya está registrado.",
							"Este correo electrónico ya está registrado.");
				}
			}
			if (!user.getUsername().isEmpty()) {
				Boolean existUsername = this.userService.existUserWithSameUsername(user.getUsername());
				if (existUsername) {
					result.rejectValue("username", "El nombre de usuario no está disponible.",
							"El nombre de usuario no está disponible.");
				} else if (user.getUsername().length() < 6 || user.getUsername().length() > 18) {
					result.rejectValue("username", "El nombre de usuario debe contener entre 6 y 18 carácteres.",
							"El nombre de usuario debe contener entre 6 y 18 carácteres.");
				}
			}

			if (!user.getAccept()) {
				result.rejectValue("accept", "Debe aceptar las condiciones.", "Debe aceptar las condiciones.");
			}

			if (user.getPassword().isEmpty()) {
				result.rejectValue("password", "La contraseña es un campo requerido.",
						"La contraseña es un campo requerido.");
			}
		}

		if (this.countCharacters(user.getGenres(), ",") <= 1) {
			result.rejectValue("genres", "Debe seleccionar al menos 3 géneros.",
					"Debe seleccionar al menos 3 géneros.");
		}

		if (!user.getPassword().isEmpty()) {
			if (user.getPassword().length() < 8 || user.getPassword().length() > 20) {
				result.rejectValue("password", "La contraseña debe contener entre 8 y 20 carácteres.",
						"La contraseña debe contener entre 8 y 20 carácteres.");
			}
			if (!user.getPassword().equals(user.getConfirmPassword())) {
				result.rejectValue("confirmPassword", "Las contraseñas no coinciden.", "Las contraseñas no coinciden.");
			}
		}

		if (user.getBirthDate() != null) {
			if (Period.between(user.getBirthDate(), LocalDate.now()).getYears() < 18) {
				result.rejectValue("birthDate", "Debe ser mayor de edad para registrarse.",
						"Debe ser mayor de 18 años.");
			}
		}

		return result;
	}

	@PostMapping(value = "/register")
	public Map<String, Object> register(@RequestBody @Valid User user, BindingResult result) {
		Map<String, Object> res = new HashMap<>();

		result = this.validateUser(user, result, true);
		if (!result.hasErrors()) {
			this.userService.save(user, true);
			res.put("success", true);
		} else {
			res.put("errors", result.getAllErrors());
			res.put("success", false);
		}

		return res;
	}

	@GetMapping(value = "/user/get-username")
	public Map<String, Object> getIdPrincipal(Principal principal) {
		Map<String, Object> res = new HashMap<>();

		try {
			res.put("username", principal.getName());
		} catch (Exception e) {
			res.put("username", null);
		}

		return res;
	}

	@GetMapping(value = "/user/{username}")
	public Map<String, Object> getUser(@PathVariable("username") String username) {
		Map<String, Object> res = new HashMap<>();
		if (!username.isEmpty()) {
			User user = this.userService.findByUsername(username);
			if (user != null) {
				res.put("user", user);
				res.put("success", true);
			} else {
				res.put("success", false);
			}
		} else {
			res.put("success", false);
		}

		return res;
	}

	@PutMapping(value = "/user/{username}/edit")
	public Map<String, Object> edit(@RequestBody @Valid User user, BindingResult result,
			@PathVariable("username") String username) {
		Map<String, Object> res = new HashMap<>();

		User userOld = this.userService.findByUsername(username);

		if (!user.getEmail().isEmpty()) {
			Boolean existEmail = this.userService.existUserWithSameEmail(user.getEmail());
			if (existEmail && !userOld.getEmail().equals(user.getEmail())) {
				result.rejectValue("email", "Este correo electrónico ya está registrado.",
						"Este correo electrónico ya está registrado.");
			}
		}
		result = this.validateUser(user, result, false);
		if (!result.hasErrors()) {
			user.setId(userOld.getId());
			Boolean newPassword = true;
			if (user.getPassword().isEmpty()) {
				user.setPassword(userOld.getPassword());
				newPassword = false;
			}
			this.userService.save(user, newPassword);
			res.put("success", true);
		} else {
			res.put("errors", result.getAllErrors());
			res.put("success", false);
		}

		return res;

	}

	@GetMapping(value = "/admin/get-dashboard")
	public Map<String, Object> getDashboardData() {
		Map<String, Object> res = new HashMap<>();
		res.put("numBooks", this.bookService.countBooks());
		res.put("numRequests", this.requestService.countRequests());
		res.put("numFavourites", this.userFavouriteBookService.countFavouritesBooks());
		res.put("numUsers", this.userService.countUsers());
		res.put("numSearchs", this.searchService.countSearchs());

		return res;

	}
}
