package com.termine.api.controller;

import com.termine.api.model.Deadline;
import com.termine.api.model.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

	public static List<User> users = new ArrayList<>();

	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@RequestBody User newUser, @RequestParam Long createdBy) {
		User createdByUser = getUserById(createdBy);
		if (createdByUser != null && createdByUser.getRoles().contains("programmer")) {
			newUser.setId(getMaxUserId() + 1L);
			users.add(newUser);
			return ResponseEntity.ok("User registered successfully");
		} else {
			return ResponseEntity.badRequest().body("User not authorized to register users");
		}
	}

	@PutMapping("/edit/{id}")
	public ResponseEntity<String> editUser(@PathVariable Long id, @RequestBody User updatedUser,
			@RequestParam Long createdBy) {
		User createdByUser = getUserById(createdBy);
		if (createdByUser != null && createdByUser.getRoles().contains("programmer")) {

			for (User user : users) {
				if (user.getId().equals(id)) {
					user.setName(updatedUser.getName());
					return ResponseEntity.ok("User updated successfully");
				}
			}
		} else {
			return ResponseEntity.badRequest().body("User not authorized to edit users");
		}
		return ResponseEntity.notFound().build();
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable Long id, @RequestParam Long createdBy) {
		User createdByUser = getUserById(createdBy);
		if (createdByUser != null && createdByUser.getRoles().contains("programmer")) {
			for (Iterator<User> iterator = users.iterator(); iterator.hasNext();) {
				User user = iterator.next();
				if (user.getId().equals(id)) {
					iterator.remove();
					return ResponseEntity.ok("User deleted successfully");
				}
			}
	        Iterator<Deadline> iterator = DeadlineController.deadlines.iterator();
	        while (iterator.hasNext()) {
	            Deadline deadline = iterator.next();
	            if (deadline.getUserIds().contains(id)) {
	                deadline.getUserIds().remove(id);
	                deadline.getUsers().removeIf(user -> user.getId().equals(id));

	                if (deadline.getUserIds().isEmpty()) {
	                    iterator.remove();
	                }
	            }
	        }
		} else {
			return ResponseEntity.badRequest().body("User not authorized to delete users");
		}
		return ResponseEntity.notFound().build();
	}

	@GetMapping("/list")
	public List<User> listUsers() {
		return users;
	}

	@GetMapping("/{id}")
	public User getUserById(@PathVariable Long id) {
		for (User user : users) {
			if (user.getId().equals(id)) {
				return user;
			}
		}
		return null;
	}
	
    public Long getMaxUserId() {
        Long maxId = users.stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0L);
        return maxId;
    }

}