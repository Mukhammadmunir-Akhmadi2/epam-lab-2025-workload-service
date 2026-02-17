package com.epam.infrastructure.controllers.advice;

import com.epam.application.exceptions.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/__test")
class AdviceTestController {

    @GetMapping("/not-found")
    void notFound() {
        throw new ResourceNotFoundException("Trainer not found id=1");
    }

    @GetMapping("/illegal-arg")
    void illegalArg() {
        throw new IllegalArgumentException("bad arg");
    }

    @GetMapping("/bad-credentials")
    void badCredentials() {
        throw new org.springframework.security.authentication.BadCredentialsException("bad creds");
    }

    @PostMapping(value = "/not-readable", consumes = MediaType.APPLICATION_JSON_VALUE)
    void notReadable(@RequestBody String body) {
        // never reached if JSON invalid
    }

    @GetMapping("/generic")
    void generic() {
        throw new RuntimeException("boom");
    }

    @GetMapping("/constraint-violation")
    void constraintViolation() {
        // Build a minimal ConstraintViolationException without full Validator:
        ConstraintViolation<?> v = new FakeConstraintViolation("username", "must not be blank");
        throw new ConstraintViolationException(Set.of(v));
    }

    // Minimal fake ConstraintViolation used only for tests
    static class FakeConstraintViolation implements ConstraintViolation<Object> {
        private final String path;
        private final String msg;

        FakeConstraintViolation(String path, String msg) {
            this.path = path;
            this.msg = msg;
        }

        @Override public String getMessage() { return msg; }
        @Override public Path getPropertyPath() { return new FakePath(path); }

        // --- Everything else not needed for your handler logic ---
        @Override public String getMessageTemplate() { return null; }
        @Override public Object getRootBean() { return null; }
        @Override public Class<Object> getRootBeanClass() { return null; }
        @Override public Object getLeafBean() { return null; }
        @Override public Object[] getExecutableParameters() { return new Object[0]; }
        @Override public Object getExecutableReturnValue() { return null; }
        @Override public Object getInvalidValue() { return null; }
        @Override public jakarta.validation.metadata.ConstraintDescriptor<?> getConstraintDescriptor() { return null; }
        @Override public <U> U unwrap(Class<U> type) { throw new UnsupportedOperationException(); }
    }

    static class FakePath implements Path {
        private final String s;
        FakePath(String s) { this.s = s; }
        @Override public String toString() { return s; }
        @Override public java.util.Iterator<Node> iterator() { return java.util.Collections.emptyIterator(); }
    }
}
