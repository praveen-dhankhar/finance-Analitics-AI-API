package com.financeapp.dto.mapper;

import com.financeapp.dto.UserProfileCreateDto;
import com.financeapp.dto.UserProfileDto;
import com.financeapp.dto.UserProfileUpdateDto;
import com.financeapp.entity.UserProfile;
import org.springframework.stereotype.Component;

/**
 * Mapper for UserProfile entity and DTOs
 * Compatible with both H2 and PostgreSQL databases
 */
@Component
public class UserProfileMapper {

    /**
     * Convert UserProfile entity to UserProfileDto
     */
    public UserProfileDto toDto(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }

        return new UserProfileDto(
            userProfile.getId(),
            userProfile.getFirstName(),
            userProfile.getLastName(),
            userProfile.getEmail(),
            userProfile.getPhoneNumber(),
            userProfile.getDateOfBirth(),
            userProfile.getLocation(),
            userProfile.getBio(),
            userProfile.getProfilePictureUrl(),
            userProfile.getTimezone(),
            userProfile.getLanguage(),
            userProfile.getIsPublic(),
            userProfile.getEmailNotifications(),
            userProfile.getSmsNotifications(),
            userProfile.getMarketingEmails(),
            userProfile.getSettings(),
            userProfile.getLastLoginAt(),
            userProfile.getLoginCount(),
            userProfile.getIsActive(),
            userProfile.getCreatedAt(),
            userProfile.getUpdatedAt()
        );
    }

    /**
     * Convert UserProfileCreateDto to UserProfile entity
     */
    public UserProfile toEntity(UserProfileCreateDto dto) {
        if (dto == null) {
            return null;
        }

        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName(dto.firstName());
        userProfile.setLastName(dto.lastName());
        userProfile.setEmail(dto.email());
        userProfile.setPhoneNumber(dto.phoneNumber());
        userProfile.setDateOfBirth(dto.dateOfBirth());
        userProfile.setLocation(dto.location());
        userProfile.setBio(dto.bio());
        userProfile.setTimezone(dto.timezone());
        userProfile.setLanguage(dto.language());
        userProfile.setIsPublic(dto.isPublic() != null ? dto.isPublic() : false);
        userProfile.setEmailNotifications(dto.emailNotifications() != null ? dto.emailNotifications() : true);
        userProfile.setSmsNotifications(dto.smsNotifications() != null ? dto.smsNotifications() : false);
        userProfile.setMarketingEmails(dto.marketingEmails() != null ? dto.marketingEmails() : false);
        userProfile.setSettings(dto.settings());
        userProfile.setIsActive(true);
        userProfile.setLoginCount(0);

        return userProfile;
    }

    /**
     * Update UserProfile entity with UserProfileUpdateDto
     */
    public void updateEntity(UserProfile userProfile, UserProfileUpdateDto dto) {
        if (userProfile == null || dto == null) {
            return;
        }

        if (dto.firstName() != null) {
            userProfile.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null) {
            userProfile.setLastName(dto.lastName());
        }
        if (dto.email() != null) {
            userProfile.setEmail(dto.email());
        }
        if (dto.phoneNumber() != null) {
            userProfile.setPhoneNumber(dto.phoneNumber());
        }
        if (dto.dateOfBirth() != null) {
            userProfile.setDateOfBirth(dto.dateOfBirth());
        }
        if (dto.location() != null) {
            userProfile.setLocation(dto.location());
        }
        if (dto.bio() != null) {
            userProfile.setBio(dto.bio());
        }
        if (dto.timezone() != null) {
            userProfile.setTimezone(dto.timezone());
        }
        if (dto.language() != null) {
            userProfile.setLanguage(dto.language());
        }
        if (dto.isPublic() != null) {
            userProfile.setIsPublic(dto.isPublic());
        }
        if (dto.emailNotifications() != null) {
            userProfile.setEmailNotifications(dto.emailNotifications());
        }
        if (dto.smsNotifications() != null) {
            userProfile.setSmsNotifications(dto.smsNotifications());
        }
        if (dto.marketingEmails() != null) {
            userProfile.setMarketingEmails(dto.marketingEmails());
        }
        if (dto.settings() != null) {
            userProfile.setSettings(dto.settings());
        }
    }

    /**
     * Convert UserProfileUpdateDto to UserProfile entity (for partial updates)
     */
    public UserProfile toEntity(UserProfileUpdateDto dto) {
        if (dto == null) {
            return null;
        }

        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName(dto.firstName());
        userProfile.setLastName(dto.lastName());
        userProfile.setEmail(dto.email());
        userProfile.setPhoneNumber(dto.phoneNumber());
        userProfile.setDateOfBirth(dto.dateOfBirth());
        userProfile.setLocation(dto.location());
        userProfile.setBio(dto.bio());
        userProfile.setTimezone(dto.timezone());
        userProfile.setLanguage(dto.language());
        userProfile.setIsPublic(dto.isPublic());
        userProfile.setEmailNotifications(dto.emailNotifications());
        userProfile.setSmsNotifications(dto.smsNotifications());
        userProfile.setMarketingEmails(dto.marketingEmails());
        userProfile.setSettings(dto.settings());

        return userProfile;
    }
}
