        package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.User;
import com.example.expo.Model.UserHealthCard;
import com.example.expo.Repository.UserHealthCardRepository;
import com.example.expo.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import java.util.List;

@Service
@AllArgsConstructor
public class UserHealthCardService {

    private final UserHealthCardRepository userHealthCardRepository;
    private final UserRepository userRepository;


    public List<UserHealthCard> getAllHealthCards() {
        return userHealthCardRepository.findAll();
    }

    @Transactional
    public void addHealthCard(Integer userId, UserHealthCard healthCard) {

        User user = userRepository.findUserById(userId);

        if (user == null) {
            throw new ApiException("User not found");
        }

        if (userHealthCardRepository.findUserHealthCardByUserId(userId) != null) {
            throw new ApiException("User already has a health card");
        }
        // نستبدل أي قيمة واردة من كود العميل بقيمة يصدرها السيرفر.
        healthCard.setPublicToken(newPublicToken());
        HealthCardPatchService.normalize(healthCard);
        if (healthCard.getCountryCode() == null) healthCard.setCountryCode("SA");
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
    // تجهيز البطاقات القديمة عند طلب QR فقط؛ لا ننشئ بطاقة طبية من العدم.
    @Transactional
    public UserHealthCard getHealthCardForQr(Integer userId) {
        if (userRepository.findUserById(userId) == null) throw new ApiException("User not found");
        UserHealthCard card = userHealthCardRepository.findByUserIdForQr(userId)
                .orElseThrow(() -> new ApiException("Health card not found"));
        if (card.getPublicToken() == null || card.getPublicToken().isBlank()) {
            card.setPublicToken(newPublicToken());
            userHealthCardRepository.save(card);
        }
        return card;
    }

    @Transactional(readOnly = true)
    public UserHealthCard getHealthCardByToken(String token) {
        if (token == null || !token.matches("[a-f0-9]{32}")) throw new ApiException("Health card not found");
        return userHealthCardRepository.findByPublicToken(token)
                .orElseThrow(() -> new ApiException("Health card not found"));
    }

    private String newPublicToken() { return UUID.randomUUID().toString().replace("-", ""); }
}
