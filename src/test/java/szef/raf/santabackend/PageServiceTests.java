package szef.raf.santabackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import szef.raf.santabackend.dto.CreatePageRequest;
import szef.raf.santabackend.entity.Gift;
import szef.raf.santabackend.entity.Letter;
import szef.raf.santabackend.entity.LetterType;
import szef.raf.santabackend.entity.Page;
import szef.raf.santabackend.service.PageService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PageServiceTests {

    @Autowired
    private PageService pageService;

    @Test
    void testCreatePageWithPasswordAndVerify() {
        String code = "kowalscy2026";
        String password = "tajne-haslo-123";

        // 1. Create page with password
        Page created = pageService.createPage(new CreatePageRequest(code, password));
        assertNotNull(created);
        assertEquals(code, created.getCode());
        assertNotNull(created.getPasswordHash());
        assertNotEquals(password, created.getPasswordHash()); // Must be hashed!

        // 2. Duplicate code should fail
        assertThrows(ResponseStatusException.class, () ->
                pageService.createPage(new CreatePageRequest(code, "inne-haslo")));

        // 3. Verify with correct password
        assertTrue(pageService.verifyPassword(code, password));
        assertTrue(pageService.verifyPassword("  KOWALSCY2026  ", password)); // Case-insensitive code check

        // 4. Verify with wrong password
        assertFalse(pageService.verifyPassword(code, "zle-haslo"));
        assertFalse(pageService.verifyPassword("nieznany-kod", password));
    }

    @Test
    void testCreatePageAndLettersWithGifts() {
        String testCode = "test-letters-page";
        pageService.createPage(new CreatePageRequest(testCode, "secret123"));
        Page page = pageService.getPage(testCode);
        assertNotNull(page);
        assertEquals(testCode, page.getCode());

        Letter letter = Letter.builder()
                .name("Zosia i Kuba")
                .description("Drogi Mikołaju, chcemy klocki i kolejkę!")
                .type(LetterType.KID)
                .build();

        Gift gift1 = Gift.builder().name("Klocki").link("https://example.com/lego").build();
        Gift gift2 = Gift.builder().name("Kolejka").link("https://example.com/train").build();

        letter.addGift(gift1);
        letter.addGift(gift2);

        pageService.createLetter(page, letter);

        Page fetched = pageService.getPage(testCode);
        assertNotNull(fetched);
        assertEquals(1, fetched.getLetterList().size());

        Letter savedLetter = fetched.getLetterList().get(0);
        assertEquals("Zosia i Kuba", savedLetter.getName());
        assertEquals(LetterType.KID, savedLetter.getType());
        assertEquals(2, savedLetter.getGiftList().size());

        // Test claiming gift
        Gift savedGift = savedLetter.getGiftList().get(0);
        assertFalse(savedGift.getIsClaimed());
        pageService.updateGiftClaim(savedGift.getId(), true);

        Letter refreshedLetter = pageService.getLetter(page, savedLetter.getId());
        Gift updatedGift = refreshedLetter.getGiftList().stream()
                .filter(g -> g.getId().equals(savedGift.getId()))
                .findFirst()
                .orElseThrow();
        assertTrue(updatedGift.getIsClaimed());

        // Test updating letter (e.g. adding 3rd gift, updating name and selection)
        Letter updatePayload = Letter.builder()
                .name("Zosia, Kuba i Staś")
                .description("Drogi Mikołaju, zaktualizowaliśmy naszą listę!")
                .type(LetterType.KID)
                .build();
        Gift retainedGift = Gift.builder()
                .id(savedGift.getId())
                .name("Super Klocki")
                .link("https://example.com/lego-deluxe")
                .isSelected(true)
                .isClaimed(true)
                .build();
        Gift newGift3 = Gift.builder()
                .name("Pluszowy Smok")
                .link("https://example.com/dragon")
                .isSelected(true)
                .build();
        updatePayload.addGift(retainedGift);
        updatePayload.addGift(newGift3);

        Letter updatedResult = pageService.updateLetter(testCode, savedLetter.getId(), updatePayload);
        assertNotNull(updatedResult);
        assertEquals("Zosia, Kuba i Staś", updatedResult.getName());
        assertEquals(2, updatedResult.getGiftList().size());
        assertTrue(updatedResult.getGiftList().stream().anyMatch(g -> "Pluszowy Smok".equals(g.getName())));
    }

    @Test
    void testPageExistsAndNotFound() {
        assertFalse(pageService.existsByCode("brak-takiej-strony"));
        assertThrows(ResponseStatusException.class, () -> pageService.getPage("brak-takiej-strony"));
    }
}
