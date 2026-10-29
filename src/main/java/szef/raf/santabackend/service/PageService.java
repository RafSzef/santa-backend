package szef.raf.santabackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import szef.raf.santabackend.dto.CreatePageRequest;
import szef.raf.santabackend.entity.Gift;
import szef.raf.santabackend.entity.Letter;
import szef.raf.santabackend.entity.Page;
import szef.raf.santabackend.repository.GiftRepository;
import szef.raf.santabackend.repository.LetterRepository;
import szef.raf.santabackend.repository.PageRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class PageService {

    private final PageRepository pageRepository;
    private final LetterRepository letterRepository;
    private final GiftRepository giftRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Page createPage(CreatePageRequest request) {
        if (request == null || request.code() == null || request.code().trim().length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kod strony musi mieć co najmniej 3 znaki.");
        }
        if (request.password() == null || request.password().trim().length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hasło musi mieć co najmniej 3 znaki.");
        }

        String normalizedCode = request.code().trim().toLowerCase();

        if (pageRepository.existsById(normalizedCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Strona o takim kodzie już istnieje!");
        }

        Page page = new Page();
        page.setCode(normalizedCode);
        page.setPasswordHash(passwordEncoder.encode(request.password().toLowerCase(Locale.ROOT).trim()));
        page.setUpdatedAt(LocalDateTime.now());
        page.setLetterList(new ArrayList<>());

        return pageRepository.save(page);
    }

    public Page createPage(Page page) {
        if (page == null || page.getCode() == null || page.getCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page code is required");
        }
        page.setCode(page.getCode().trim().toLowerCase());

        if (page.getLetterList() != null) {
            for (Letter letter : page.getLetterList()) {
                letter.setPage(page);
                if (letter.getGiftList() != null) {
                    for (Gift gift : letter.getGiftList()) {
                        gift.setLetter(letter);
                    }
                }
            }
        }
        page.setUpdatedAt(LocalDateTime.now());
        return pageRepository.save(page);
    }

    @Transactional(readOnly = true)
    public boolean verifyPassword(String code, String rawPassword) {
        if (code == null || rawPassword == null) {
            return false;
        }

        String normalizedCode = code.trim().toLowerCase();
        Page page = pageRepository.findById(normalizedCode).orElse(null);
        if (page == null) {
            return false;
        }

        // If page has no password set, treat as open
        if (page.getPasswordHash() == null || page.getPasswordHash().isBlank()) {
            return true;
        }

        String normalizedPassword = rawPassword.trim().toLowerCase(Locale.ROOT);
        return passwordEncoder.matches(normalizedPassword, page.getPasswordHash())
                || passwordEncoder.matches(rawPassword.trim(), page.getPasswordHash());
    }

    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return pageRepository.existsById(code.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public Page getPage(String code) {
        String normalizedCode = code.trim().toLowerCase();
        Page page = pageRepository.findByCodeWithLetters(normalizedCode)
                .orElseGet(() -> pageRepository.findById(normalizedCode).orElse(null));

        if (page == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Strona o kodzie " + normalizedCode + " nie istnieje");
        }

        // Initialize nested lazy gift collections within transaction
        if (page.getLetterList() != null) {
            for (Letter letter : page.getLetterList()) {
                if (letter.getGiftList() != null) {
                    letter.getGiftList().size();
                }
            }
        }

        return page;
    }

    @Transactional(readOnly = true)
    public Letter getLetter(Page page, Long id) {
        if (page == null || page.getCode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page cannot be null");
        }
        return getLetter(page.getCode(), id);
    }

    @Transactional(readOnly = true)
    public Letter getLetter(String pageCode, Long id) {
        return letterRepository.findByIdAndPageCode(id, pageCode.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Letter not found"));
    }

    public Page createLetter(Page page, Letter letter) {
        if (page == null || page.getCode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page cannot be null");
        }

        String normalizedCode = page.getCode().trim().toLowerCase();
        Page managedPage = pageRepository.findById(normalizedCode)
                .orElseGet(() -> {
                    page.setCode(normalizedCode);
                    page.setUpdatedAt(LocalDateTime.now());
                    return pageRepository.save(page);
                });

        letter.setPage(managedPage);
        if (letter.getGiftList() != null) {
            for (Gift gift : letter.getGiftList()) {
                gift.setLetter(letter);
            }
        }

        managedPage.addLetter(letter);
        managedPage.setUpdatedAt(LocalDateTime.now());
        letterRepository.save(letter);
        pageRepository.save(managedPage);
        return managedPage;
    }

    public Letter createLetter(String pageCode, Letter letter) {
        Page page = getPage(pageCode);
        createLetter(page, letter);
        return letter;
    }

    public Letter updateLetter(String pageCode, Long letterId, Letter incoming) {
        if (pageCode == null || letterId == null || incoming == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page code, letter id and body are required");
        }
        String normalizedCode = pageCode.trim().toLowerCase();
        Letter letter = letterRepository.findByIdAndPageCode(letterId, normalizedCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Letter not found"));

        if (incoming.getName() != null && !incoming.getName().isBlank()) {
            letter.setName(incoming.getName().trim());
        }
        if (incoming.getDescription() != null) {
            letter.setDescription(incoming.getDescription());
        }
        if (incoming.getType() != null) {
            letter.setType(incoming.getType());
        }

        // Synchronize gifts
        if (incoming.getGiftList() != null) {
            java.util.Map<Long, Gift> existingGifts = new java.util.HashMap<>();
            if (letter.getGiftList() != null) {
                for (Gift g : letter.getGiftList()) {
                    if (g.getId() != null) {
                        existingGifts.put(g.getId(), g);
                    }
                }
            }

            java.util.List<Gift> updatedGifts = new java.util.ArrayList<>();
            for (Gift incGift : incoming.getGiftList()) {
                if (incGift.getId() != null && existingGifts.containsKey(incGift.getId())) {
                    Gift existing = existingGifts.get(incGift.getId());
                    existing.setName(incGift.getName());
                    existing.setLink(incGift.getLink());
                    if (incGift.getIsSelected() != null) {
                        existing.setIsSelected(incGift.getIsSelected());
                    }
                    if (incGift.getIsClaimed() != null) {
                        existing.setIsClaimed(incGift.getIsClaimed());
                    }
                    updatedGifts.add(existing);
                } else {
                    Gift newGift = new Gift();
                    newGift.setName(incGift.getName());
                    newGift.setLink(incGift.getLink());
                    newGift.setIsSelected(Boolean.TRUE.equals(incGift.getIsSelected()));
                    newGift.setIsClaimed(Boolean.TRUE.equals(incGift.getIsClaimed()));
                    newGift.setLetter(letter);
                    updatedGifts.add(newGift);
                }
            }

            if (letter.getGiftList() == null) {
                letter.setGiftList(new java.util.ArrayList<>());
            } else {
                letter.getGiftList().clear();
            }
            letter.getGiftList().addAll(updatedGifts);
        }

        if (letter.getPage() != null) {
            letter.getPage().setUpdatedAt(LocalDateTime.now());
            pageRepository.save(letter.getPage());
        }

        return letterRepository.save(letter);
    }

    public void deleteLetter(Long id) {
        Letter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Letter not found"));
        if (letter.getPage() != null) {
            letter.getPage().setUpdatedAt(LocalDateTime.now());
            pageRepository.save(letter.getPage());
        }
        letterRepository.delete(letter);
    }

    public Gift updateGiftClaim(Long giftId, Boolean claimed) {
        Gift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gift not found"));
        gift.setIsClaimed(claimed);
        gift.setIsSelected(false);
        Gift saved = giftRepository.save(gift);
        if (gift.getLetter() != null && gift.getLetter().getPage() != null) {
            String pageCode = gift.getLetter().getPage().getCode();
            pageRepository.touchPage(pageCode, LocalDateTime.now());
        }
        return saved;
    }

    public Gift updateGiftSelection(Long giftId, Boolean selected) {
        Gift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gift not found"));
        gift.setIsSelected(selected);
        Gift saved = giftRepository.save(gift);
        if (gift.getLetter() != null && gift.getLetter().getPage() != null) {
            gift.getLetter().getPage().setUpdatedAt(LocalDateTime.now());
            pageRepository.save(gift.getLetter().getPage());
        }
        return saved;
    }
}
