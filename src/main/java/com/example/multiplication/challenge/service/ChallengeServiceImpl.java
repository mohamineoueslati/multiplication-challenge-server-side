package com.example.multiplication.challenge.service;

import com.example.multiplication.challenge.domain.ChallengeAttempt;
import com.example.multiplication.challenge.dto.ChallengeAttemptDTO;
import com.example.multiplication.challenge.repository.ChallengeAttemptRepository;
import com.example.multiplication.serviceclients.GamificationServiceClient;
import com.example.multiplication.user.domain.User;
import com.example.multiplication.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeServiceImpl implements ChallengeService {

    private final UserRepository userRepository;
    private final ChallengeAttemptRepository attemptRepository;
    private final GamificationServiceClient gameClient;

    @Override
    public ChallengeAttempt verifyAttempt(ChallengeAttemptDTO attemptDTO) {
        // Check if the user already exists for that alias, otherwise create it
        User user = userRepository.findByAlias(attemptDTO.userAlias()).orElseGet(() -> {
            log.info("Creating new user with alias {}", attemptDTO.userAlias());
            return userRepository.save(new User(attemptDTO.userAlias()));
        });
        // Check if the attempt is correct
        boolean isCorrect = attemptDTO.guess() == attemptDTO.factorA() * attemptDTO.factorB();
        ChallengeAttempt challengeAttempt = new ChallengeAttempt(
                null,
                user,
                attemptDTO.factorA(),
                attemptDTO.factorB(),
                attemptDTO.guess(),
                isCorrect
        );
        // Stores the attempt
        var storedAttempt = attemptRepository.save(challengeAttempt);
        boolean status = gameClient.sendAttempt(storedAttempt);
        log.info("Gamification service response: {}", status);
        return storedAttempt;
    }

    @Override
    public List<ChallengeAttempt> getStatsForUser(String userAlias) {
        return attemptRepository.lastAttempts(userAlias);
    }
}
