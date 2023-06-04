package com.example.springatt.services;

import com.example.springatt.models.Person;
import com.example.springatt.repositories.PersonRepository;
import com.example.springatt.security.PersonDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonDetailsServices implements UserDetailsService {
    private final PersonRepository personRepository;

    @Autowired
    public PersonDetailsServices(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Получаем пользователя из таблицы по логину
        Optional<Person> person = personRepository.findByLogin(username);

        if(person.isEmpty()){
            throw new UsernameNotFoundException("Пользователь с такими даннными не найден");
        }
        return new PersonDetails(person.get());
    }
}
