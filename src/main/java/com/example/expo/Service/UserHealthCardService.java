        package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.User;
import com.example.expo.Model.UserHealthCard;
import com.example.expo.Repository.UserHealthCardRepository;
import com.example.expo.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserHealthCardService {

    private final UserHealthCardRepository userHealthCardRepository;
    private final UserRepository userRepository;


    public List<UserHealthCard> getAllHealthCards() {
        return userHealthCardRepository.findAll();
    }

    public void addHealthCard(Integer userId, UserHealthCard healthCard) {

        User user = userRepository.findUserById(userId);

        if (user == null) {
            throw new ApiException("User not found");
        }

        if (userHealthCardRepository.findUserHealthCardByUserId(userId) != null) {
            throw new ApiException("User already has a health card");
        }
        healthCard.setUser(user);
        userHealthCardRepository.save(healthCard);
    }


    public void updateHealthCard(Integer userId, Integer healthCardId, UserHealthCard healthCard) {

        User user = userRepository.findUserById(userId);

        if (user == null) {
            throw new ApiException("User not found");
        }

        UserHealthCard oldHealthCard = userHealthCardRepository.findUserHealthCardById(healthCardId);

        if (oldHealthCard == null) {
            throw new ApiException("Health card not found");
        }

        if (!oldHealthCard.getUser().getId().equals(userId)) {
            throw new ApiException("Health card does not belong to this user");
        }

        oldHealthCard.setBloodType(healthCard.getBloodType());
        oldHealthCard.setAllergies(healthCard.getAllergies());
        oldHealthCard.setChronicDiseases(healthCard.getChronicDiseases());
        oldHealthCard.setEmergencyContactName(healthCard.getEmergencyContactName());
        oldHealthCard.setEmergencyContactPhone(healthCard.getEmergencyContactPhone());
        oldHealthCard.setNotes(healthCard.getNotes());

        userHealthCardRepository.save(oldHealthCard);
    }

    public void deleteHealthCard(Integer userId, Integer healthCardId) {
        User user = userRepository.findUserById(userId);

        if (user == null) {
            throw new ApiException("User not found");
        }
        UserHealthCard healthCard =
                userHealthCardRepository.findUserHealthCardById(healthCardId);

        if (healthCard == null) {
            throw new ApiException("Health card not found");
        }

        if (!healthCard.getUser().getId().equals(userId)) {
            throw new ApiException("Health card does not belong to this user");
        }

        userHealthCardRepository.delete(healthCard);
    }
}
