package hexlet.code.service;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import hexlet.code.dto.user.UserDTO;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserDTO> getAll() {
        return userRepository.findAll().stream()
                .map(mapper::map)
                .toList();
    }


    public UserDTO getUserById(Long id) {
        var maybeUser = userRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        return mapper.map(maybeUser);
    }


    public UserDTO createUser(UserCreateDTO dto) {
        var user = mapper.map(dto);
        userRepository.save(user);
        return mapper.map(user);
    }


    public UserDTO updateUser(UserUpdateDTO dto, long id) {
        var maybeUser = userRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        mapper.update(dto, maybeUser);
        userRepository.save(maybeUser);
        return mapper.map(maybeUser);
    }

    public void destroyUser(long id) {
        userRepository.deleteById(id);
    }
}
