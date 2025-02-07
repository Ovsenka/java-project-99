package hexlet.code.util;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class UserUtils {
    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        var email = authentication.getName();
        return userRepository.findByEmail(email).get();
    }

    public boolean isOwner(Long id, String email) {
        var maybeUser = userRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        return maybeUser.getEmail().equals(email);
    }

    public boolean isExists(String email) {
        var maybeUser = userRepository.findByEmail(email);
        return maybeUser.isPresent();
    }
}
