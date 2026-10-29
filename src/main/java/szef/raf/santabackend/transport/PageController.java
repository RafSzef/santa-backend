package szef.raf.santabackend.transport;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import szef.raf.santabackend.dto.CreatePageRequest;
import szef.raf.santabackend.dto.VerifyPasswordRequest;
import szef.raf.santabackend.entity.Gift;
import szef.raf.santabackend.entity.Letter;
import szef.raf.santabackend.entity.Page;
import szef.raf.santabackend.service.PageService;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Page createPage(@RequestBody CreatePageRequest request) {
        return pageService.createPage(request);
    }

    @PostMapping("/api/pages")
    @ResponseStatus(HttpStatus.CREATED)
    public Page createPageViaApi(@RequestBody CreatePageRequest request) {
        return pageService.createPage(request);
    }

    @GetMapping("/api/pages/{code}/exists")
    public Map<String, Boolean> checkPageExists(@PathVariable String code) {
        return Map.of("exists", pageService.existsByCode(code));
    }

    @PostMapping("/api/pages/{code}/verify")
    public ResponseEntity<?> verifyPassword(@PathVariable String code, @RequestBody VerifyPasswordRequest request) {
        if (!pageService.existsByCode(code)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Strona o podanym kodzie nie istnieje!", "notFound", true));
        }

        boolean valid = pageService.verifyPassword(code, request.password());
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Niepoprawne hasło!", "valid", false));
        }
        Page page = pageService.getPage(code);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/api/pages/{code}")
    public Page getPage(@PathVariable String code) {
        return pageService.getPage(code);
    }

    @GetMapping("/api/pages/{code}/letters/{id}")
    public Letter getLetter(@PathVariable String code, @PathVariable Long id) {
        return pageService.getLetter(code, id);
    }

    @PostMapping("/api/pages/{code}/letters")
    @ResponseStatus(HttpStatus.CREATED)
    public Letter createLetter(@PathVariable String code, @RequestBody Letter letter) {
        return pageService.createLetter(code, letter);
    }

    @PutMapping("/api/pages/{code}/letters/{id}")
    public Letter updateLetter(@PathVariable String code, @PathVariable Long id, @RequestBody Letter letter) {
        return pageService.updateLetter(code, id, letter);
    }

    @DeleteMapping("/api/pages/{code}/letters/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLetter(@PathVariable String code, @PathVariable Long id) {
        pageService.deleteLetter(id);
    }

    @PatchMapping("/api/gifts/{id}/claim")
    public Gift updateGiftClaim(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean claimed = body.getOrDefault("claimed", true);
        return pageService.updateGiftClaim(id, claimed);
    }

    @PatchMapping("/api/gifts/{id}/select")
    public Gift updateGiftSelect(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean selected = body.getOrDefault("selected", true);
        return pageService.updateGiftSelection(id, selected);
    }
}
