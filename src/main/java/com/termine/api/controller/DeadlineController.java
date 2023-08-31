package com.termine.api.controller;

import com.termine.api.model.Deadline;
import com.termine.api.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/deadlines")
public class DeadlineController {

	public static List<Deadline> deadlines = new ArrayList<>();

	@Autowired
	private UserController userController;

	@PostMapping("/create")
	public ResponseEntity<String> createDeadline(@RequestBody Deadline newDeadline) {
		newDeadline.setId(getMaxDeadlineId() + 1L);

	    if (newDeadline.getUserIds() == null || newDeadline.getUserIds().isEmpty() ||
	            newDeadline.getDescription() == null || newDeadline.getDescription().isEmpty() ||
	            newDeadline.getStartDate() == null || newDeadline.getEndDate() == null ||
	            newDeadline.getCreatedBy() == null) {
	            return ResponseEntity.badRequest().body("Required fields are missing");
	        }

		
		Long createdByUserId = newDeadline.getCreatedBy();
		User createdByUser = userController.getUserById(createdByUserId);
		Date startDate = newDeadline.getStartDate();
		Date endDate = newDeadline.getEndDate();

		if (createdByUser != null && createdByUser.getRoles().contains("user")) {
			String overlappingDeadlineDescription = null;

			Date overlappingStartDate = null;
			Date overlappingEndDate = null;

			List<User> users = new ArrayList<>();
			for (Long userId : newDeadline.getUserIds()) {
				User user = userController.getUserById(userId);
				if (user != null) {
					users.add(user);
				}
			}
			newDeadline.setUsers(users);

			for (User user : newDeadline.getUsers()) {
				for (Deadline existingDeadline : deadlines) {
					if (startDate.before(existingDeadline.getEndDate())
							&& endDate.after(existingDeadline.getStartDate())
							&& existingDeadline.getUserIds().contains(user.getId())) {
						overlappingDeadlineDescription = existingDeadline.getDescription();

						overlappingStartDate = startDate.after(existingDeadline.getStartDate()) ? startDate
								: existingDeadline.getStartDate();
						overlappingEndDate = endDate.before(existingDeadline.getEndDate()) ? endDate
								: existingDeadline.getEndDate();

						break;
					}
				}
				if (overlappingDeadlineDescription != null) {
					break;
				}
			}

			if (overlappingDeadlineDescription != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
				String overlappingMessage = "The new deadline overlaps with " + overlappingDeadlineDescription
						+ " from " + dateFormat.format(overlappingStartDate) + " to "
						+ dateFormat.format(overlappingEndDate);
				return ResponseEntity.badRequest().body(overlappingMessage);
			}

			deadlines.add(newDeadline);
			return ResponseEntity.ok("Deadline created successfully");
		} else {
			return ResponseEntity.badRequest().body("User not authorized to create this deadline");
		}
	}

	@GetMapping("/list")
	public List<Deadline> listDeadlines() {
		return deadlines;
	}

	@PutMapping("/edit/{id}")
	public ResponseEntity<String> editDeadline(@PathVariable Long id, @RequestBody Deadline updatedDeadline) {
	    for (Deadline deadline : deadlines) {
	        if (deadline.getId().equals(id)) {
	            Long userId = updatedDeadline.getCreatedBy();
	            User user = userController.getUserById(userId);
	            if (user != null && user.getRoles().contains("user")) {
	                List<User> users = new ArrayList<>();
	                for (Long userId1 : updatedDeadline.getUserIds()) {
	                    User user1 = userController.getUserById(userId1);
	                    if (user1 != null) {
	                        users.add(user1);
	                    }
	                }
	                deadline.setUserIds(updatedDeadline.getUserIds());
	                deadline.setUsers(users);

	                deadline.setDescription(updatedDeadline.getDescription());
	                deadline.setStartDate(updatedDeadline.getStartDate());
	                deadline.setEndDate(updatedDeadline.getEndDate());
	                deadline.setHoliday(updatedDeadline.isHoliday());

	                List<String> overlappingDeadlines = new ArrayList<>();
	                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
	                for (User updatedUser : users) {
	                    for (Deadline existingDeadline : deadlines) {
	                        if (deadline.getId().equals(existingDeadline.getId())) {
	                            continue; 
	                        }

	                        if (deadline.getStartDate().before(existingDeadline.getEndDate()) &&
	                            deadline.getEndDate().after(existingDeadline.getStartDate()) &&
	                            existingDeadline.getUserIds().contains(updatedUser.getId())) {

	                            Date overlappingStartDate = deadline.getStartDate().after(existingDeadline.getStartDate()) ?
	                                    deadline.getStartDate() : existingDeadline.getStartDate();
	                            Date overlappingEndDate = deadline.getEndDate().before(existingDeadline.getEndDate()) ?
	                                    deadline.getEndDate() : existingDeadline.getEndDate();

	                            String overlappingMessage = existingDeadline.getDescription() +
	                                    " from " + dateFormat.format(overlappingStartDate) +
	                                    " to " + dateFormat.format(overlappingEndDate);

	                            overlappingDeadlines.add(overlappingMessage);
	                        }
	                    }
	                }

	                if (!overlappingDeadlines.isEmpty()) {
	                    return ResponseEntity.badRequest().body("The edited deadline overlaps with: " +
	                            String.join(", ", overlappingDeadlines));
	                }

	                return ResponseEntity.ok("Deadline updated successfully");
	            } else {
	                return ResponseEntity.badRequest().body("User not authorized to edit this deadline");
	            }
	        }
	    }
	    return ResponseEntity.notFound().build();
	}

	@GetMapping("/user/{userId}")
	public List<Deadline> getDeadlinesForUser(@PathVariable Long userId) {
		List<Deadline> userDeadlines = new ArrayList<>();
		for (Deadline deadline : deadlines) {
			if (deadline.getUserIds().contains(userId)) {
				userDeadlines.add(deadline);
			}
		}
		return userDeadlines;
	}

	@GetMapping("/users/{deadlineId}")
	public List<User> getUsersForDeadline(@PathVariable Long deadlineId) {
		for (Deadline deadline : deadlines) {
			if (deadline.getId().equals(deadlineId)) {
				return deadline.getUsers();
			}
		}
		return new ArrayList<>();
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteDeadline(@PathVariable Long id) {
		Iterator<Deadline> iterator = deadlines.iterator();
		while (iterator.hasNext()) {
			Deadline deadline = iterator.next();
			if (deadline.getId().equals(id)) {
				iterator.remove();
				return ResponseEntity.ok("Deadline deleted successfully");
			}
		}
		return ResponseEntity.notFound().build();
	}
	
	public Long getMaxDeadlineId() {
        Long maxId = deadlines.stream()
                .mapToLong(Deadline::getId)
                .max()
                .orElse(0L);
        return maxId;
    }

}